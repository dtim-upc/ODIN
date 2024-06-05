import zipfile

from rdflib.term import Node
import os 
import sys
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
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


def workflow_planner(ontology: Graph, implementations: List, intent: Graph):
    dataset, problem, intent_params, intent_iri = get_intent_info(intent)
    components = [
        (c, impl, inputs)
        for impl, inputs in implementations
        for c in get_implementation_components(ontology, impl)
    ]
    workflow_order = 0

    split_components = [
        cb.term('component-random_absolute_train_test_split'),
        cb.term('component-random_relative_train_test_split'),
        cb.term('component-top_k_absolute_train_test_split'),
        cb.term('component-top_k_relative_train_test_split'),
    ]

    workflows = []

    for component, implementation, inputs in tqdm(components, desc='Components', position=1):
        shapes_to_satisfy = identify_data_io(ontology, inputs)
        assert shapes_to_satisfy is not None and len(shapes_to_satisfy) > 0

        unsatisfied_shapes = [shape for shape in shapes_to_satisfy if
                              not satisfies_shape(ontology, ontology, shape, dataset)]

        available_transformations = {
            shape: find_components_to_satisfy_shape(ontology, shape, only_learners=True)
            for shape in unsatisfied_shapes
        }

        transformation_combinations = list(
            enumerate(itertools.product(split_components, *available_transformations.values())))
        # TODO - check if the combination is valid and whether further transformations are needed

        for i, transformation_combination in tqdm(transformation_combinations, desc='Transformations', position=0,
                                                  leave=False):
            workflow_name = f'workflow_{workflow_order}_{intent_iri.fragment}_{uuid.uuid4()}'.replace('-', '_')
            wg, w = build_workflow_train_test(workflow_name, ontology, dataset, component,
                                              transformation_combination[0],
                                              transformation_combination[1:])

            wg.add((w, tb.createdFor, intent_iri))
            wg.add((intent_iri, RDF.type, tb.Intent))

            workflows.append(wg)
            workflow_order += 1
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

