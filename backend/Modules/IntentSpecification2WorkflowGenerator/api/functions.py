import zipfile

from rdflib.term import Node

from pipeline_generator.pipeline_generator import *


def abstract_planner(ontology: Graph, intent: Graph) -> Tuple[
    Dict[Node, Dict[Node, List[Node]]], Dict[Node, List[Node]]]:
    dataset, problem, intent_params, intent_iri = get_intent_info(intent)

    impls = get_potential_implementations(ontology, problem, [])

    algs = [(next(ontology.objects(impl[0], tb.implements)),
             (impl[0], RDF.type, tb.LearnerImplementation) in ontology) for impl in impls]

    plans = {}
    for alg, train in algs:
        if train:
            trainer = cb.term(alg.fragment + '-Train')
            plans[alg] = {
                cb.DataLoading: [cb.Partitioning],
                cb.Partitioning: [trainer, alg],
                trainer: [alg],
                alg: [cb.DataStoring],
                cb.DataStoring: []
            }
        else:
            plans[alg] = {
                cb.DataLoading: [alg],
                alg: [cb.DataStoring],
                cb.DataStoring: []
            }

    alg_plans = {alg: [] for alg, _ in algs}
    for impl in impls:
        alg_plans[next(ontology.objects(impl[0], tb.implements))].append(impl)

    return plans, alg_plans


def workflow_planner(ontology: Graph, implementations: List, intent: Graph, log: bool = False):
    dataset, problem, intent_params, intent_iri = get_intent_info(intent)

    if log:
        print(f'Dataset: {dataset.fragment}')
        print(f'Problem: {problem.fragment}')
        print(f'Intent params: {intent_params}')
        print('-------------------------------------------------')
        print(implementations)


    components = [
        (c, impl, inputs)
        for impl, inputs in implementations
        for c in get_implementation_components(ontology, impl)
    ]
    workflow_order = 0

    if log:
        for component, implementation, inputs in components:
            print(f'Component: {component.fragment} ({implementation.fragment})')
            for im_input in inputs:
                print(f'\tInput: {[x.fragment for x in im_input]}')
        print('-------------------------------------------------')

    split_components = [
        cb.term('component-random_absolute_train_test_split'),
        cb.term('component-random_relative_train_test_split'),
        cb.term('component-top_k_absolute_train_test_split'),
        cb.term('component-top_k_relative_train_test_split'),
    ]

    workflows = []

    for component, implementation, inputs in tqdm(components, desc='Components', position=1):
        if log:
            print(f'Component: {component.fragment} ({implementation.fragment})')

        shapes_to_satisfy = identify_data_io(ontology, inputs)
        assert shapes_to_satisfy is not None and len(shapes_to_satisfy) > 0

        if log:
            print(f'\tData input: {[x.fragment for x in shapes_to_satisfy]}')

        unsatisfied_shapes = [shape for shape in shapes_to_satisfy if
                              not satisfies_shape(ontology, ontology, shape, dataset)]

        available_transformations = {
            shape: find_components_to_satisfy_shape(ontology, shape, only_learners=True)
            for shape in unsatisfied_shapes
        }
        print("Variablity points:")
        print(f'\tSplitComponents: {[x.fragment for x in split_components]}')
        for shape, comps in available_transformations.items():
            print(f'\t{shape.fragment}: {[x.fragment for x in comps]}')


        if log:
            print(f'\tUnsatisfied shapes: ')
            for shape, comps in available_transformations.items():
                print(f'\t\t{shape.fragment}: {[x.fragment for x in comps]}')

        transformation_combinations = list(
            enumerate(itertools.product(split_components, *available_transformations.values())))
        # TODO - check if the combination is valid and whether further transformations are needed

        if log:
            print(f'\tTotal combinations: {len(transformation_combinations)}')

        for i, transformation_combination in tqdm(transformation_combinations, desc='Transformations', position=0,
                                                  leave=False):
            if log:
                print(
                    f'\t\tCombination {i + 1} / {len(transformation_combinations)}: {[x.fragment for x in transformation_combination]}')

            workflow_name = f'workflow_{workflow_order}_{intent_iri.fragment}_{uuid.uuid4()}'.replace('-', '_')
            wg, w = build_workflow_train_test(workflow_name, ontology, dataset, component,
                                              transformation_combination[0],
                                              transformation_combination[1:])

            wg.add((w, tb.createdFor, intent_iri))
            wg.add((intent_iri, RDF.type, tb.Intent))

            workflows.append(wg)
            workflow_order += 1
            if log:
                print(f'\t\tWorkflow {workflow_order}: {w.fragment}')
    return workflows


def logical_planner(ontology: Graph, workflow_plans: List[Graph]):
    logical_plans = {}
    counter = {}
    for workflow_plan in workflow_plans:
        steps = list(workflow_plan.subjects(RDF.type, tb.Step))
        step_components = {step: next(workflow_plan.objects(step, tb.runs)) for step in steps}
        step_next = {step: list(workflow_plan.objects(step, tb.followedBy)) for step in steps}
        logical_plan = {
            step_components[step]: [step_components[s] for s in nexts] for step, nexts in step_next.items()
        }
        main_component = next(
            comp for comp in logical_plan.keys() if logical_plan[comp] == [cb.term('component-csv_local_writer')])
        if (main_component, RDF.type, tb.ApplierImplementation) in ontology:
            options = list(ontology.objects(main_component, tb.hasLearner))
            main_component = next(o for o in options if (None, None, o) in workflow_plan)
        if main_component not in counter:
            counter[main_component] = 0
        plan_id = (f'{main_component.fragment.split("-")[1].replace("_", " ").replace(" learner", "").title()} '
                   f'{counter[main_component]}')
        counter[main_component] += 1
        logical_plans[plan_id] = {"logical_plan": logical_plan, "graph": workflow_plan.serialize(format="turtle")}

    return logical_plans


def compress(folder: str, destination: str) -> None:
    with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(folder):
            for file in files:
                file_path = os.path.join(root, file)
                archive_path = os.path.relpath(file_path, folder)
                zipf.write(file_path, arcname=os.path.join(os.path.basename(folder), archive_path))


def get_custom_ontology(path):
    graph = get_graph_xp()
    ontologies = [
        r'ontologies/tbox.ttl',
        r'ontologies/cbox.ttl',
        r'ontologies/abox.ttl',
        path
    ]
    for o in ontologies:
        graph.parse(o, format="turtle")

    DeductiveClosure(OWLRL_Semantics).expand(graph)
    return graph

def get_custom_ontology_only_problems():
    graph = get_graph_xp()
    ontologies = [
        r'ontologies/tbox.ttl',
        r'ontologies/cbox.ttl',
        r'ontologies/abox.ttl',
    ]
    for o in ontologies:
        graph.parse(o, format="turtle")

    DeductiveClosure(OWLRL_Semantics).expand(graph)
    return graph


def logical_planner_extremexp(ontology: Graph, workflow_plans: List[Graph]):
    logical_plans = {}
    counter = {}
    tasks = []
    extremexp_workflows = []
    workflow_counter = 0
    for workflow_plan in workflow_plans:
        steps = list(workflow_plan.subjects(RDF.type, tb.Step))
        step_components = {step: next(workflow_plan.objects(step, tb.runs)) for step in steps}
        step_next = {step: list(workflow_plan.objects(step, tb.followedBy)) for step in steps}
        logical_plan = {
            step_components[step]: [step_components[s] for s in nexts] for step, nexts in step_next.items()
        }

        workflow_name = "Workflow_" + str(workflow_counter)
        workflow_counter += 1
        extremexp_workflow = generate_extremexp_workflow(ontology, logical_plan, tasks, workflow_name)
        extremexp_workflows.append(extremexp_workflow)

        main_component = next(
            comp for comp in logical_plan.keys() if logical_plan[comp] == [cb.term('component-csv_local_writer')])
        if (main_component, RDF.type, tb.ApplierImplementation) in ontology:
            options = list(ontology.objects(main_component, tb.hasLearner))
            main_component = next(o for o in options if (None, None, o) in workflow_plan)
        if main_component not in counter:
            counter[main_component] = 0
        plan_id = (f'{main_component.fragment.split("-")[1].replace("_", " ").replace(" learner", "").title()} '
                   f'{counter[main_component]}')
        counter[main_component] += 1
        logical_plans[plan_id] = {"logical_plan": logical_plan, "graph": workflow_plan.serialize(format="turtle")}

    tasks = list(dict.fromkeys(tasks))  # remove duplicates

    return logical_plans, extremexp_workflows, tasks


def generate_extremexp_workflow(ontology, logical_plan, tasks, workflow_name):
    workflow = {}
    workflow["workflow_name"] = workflow_name
    task_implementations = {}
    query_template = """ PREFIX tb: <https://extremexp.eu/ontology/tbox#>
                                SELECT ?implementation ?implements
                                WHERE {{
                                    <{key}> tb:hasImplementation ?implementation .
                                    ?implementation tb:implements ?implements .
                                }} """

    if len(tasks) == 0: # Build task array
        for key in logical_plan.keys():
            query_execute = query_template.format(key=key)
            results = ontology.query(query_execute)
            for row in results:
                if "learner" in row.implementation:
                    tasks.append("ModelTrain")
                elif "predictor" in row.implementation:
                    tasks.append("ModelPredict")
                else:
                    tasks.append(row.implements[row.implements.find('#') + 1:])

    for key in logical_plan.keys():
        query_execute = query_template.format(key=key)
        results = ontology.query(query_execute)
        for row in results:
            if "applier" not in key:
                task = row.implements[row.implements.find('#') + 1:]
                if "learner" in row.implementation:
                    task = "ModelTrain"
                elif "predictor" in row.implementation:
                    task = "ModelPredict"
                task_implementations[task] = "tasks/intent_name/" + key[key.find('-') + 1:] + ".py"
            # print(f"Key: {key}, implementation: {row.implementation}, implements: {row.implements}")

    workflow["task_implementations"] = task_implementations
    workflow["experiment_space_name"] = "S_" + workflow_name
    workflow["experiment_space"] = []
    return workflow

