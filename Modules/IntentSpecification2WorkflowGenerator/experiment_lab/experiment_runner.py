import os
import re
import sys

from natsort import os_sorted

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

from pipeline_generator.pipeline_generator import *


def build_workflow(workflow_name: str, ontology: Graph, dataset: URIRef, main_component: URIRef,
                   transformations: List[URIRef]) -> Tuple[Graph, URIRef]:
    workflow_graph = get_graph_xp()
    workflow = ab.term(workflow_name)
    workflow_graph.add((workflow, RDF.type, tb.Workflow))
    task_order = 0

    dataset_node = ab.term(f'{workflow_name}-original_dataset')

    copy_subgraph(ontology, dataset, workflow_graph, dataset_node)

    previous_step = None

    for component in [*transformations, main_component]:
        step_name = get_step_name(workflow_name, task_order, component)

        input_specs = get_implementation_input_specs(ontology, get_component_implementation(ontology, component))
        input_data_index = identify_data_io(ontology, input_specs, return_index=True)
        transformation_inputs = [ab[f'{step_name}-input_{i}'] for i in range(len(input_specs))]
        transformation_inputs[input_data_index] = dataset_node
        annotate_ios_with_specs(ontology, workflow_graph, transformation_inputs, input_specs)

        output_specs = get_implementation_output_specs(ontology, get_component_implementation(ontology, component))
        output_model_index = identify_model_io(ontology, output_specs, return_index=True)
        output_data_index = identify_data_io(ontology, output_specs, return_index=True)
        transformation_outputs = [ab[f'{step_name}-output_{i}'] for i in range(len(output_specs))]
        annotate_ios_with_specs(ontology, workflow_graph, transformation_outputs, output_specs)

        parameters = get_component_parameters(ontology, component)
        parameters = perform_param_substitution(workflow_graph, parameters, transformation_inputs)
        step = add_step(workflow_graph, workflow,
                        step_name,
                        component, parameters, task_order, previous_step,
                        transformation_inputs,
                        transformation_outputs)

        previous_step = step

        run_component_transformation(ontology, workflow_graph, component, transformation_inputs, transformation_outputs,
                                     parameters)

        if output_data_index is not None:
            dataset_node = transformation_outputs[output_data_index]

        task_order += 1

    return workflow_graph, workflow


def build_workflows(ontology: Graph, intent_graph: Graph, destination_folder: str) -> int:
    dataset, problem, intent_params, intent_iri = get_intent_info(intent_graph)

    impls = get_potential_implementations(ontology, problem, [x['param'] for x in intent_params])
    components = [
        (c, impl, inputs)
        for impl, inputs in impls
        for c in get_implementation_components(ontology, impl)
    ]

    workflow_order = 0

    for component, implementation, inputs in tqdm(components, desc='Components', position=1, leave=False):
        shapes_to_satisfy = identify_data_io(ontology, inputs)
        assert shapes_to_satisfy is not None and len(shapes_to_satisfy) > 0

        unsatisfied_shapes = [shape for shape in shapes_to_satisfy if
                              not satisfies_shape(ontology, ontology, shape, dataset)]

        available_transformations = {
            shape: find_components_to_satisfy_shape(ontology, shape, only_learners=False)
            for shape in unsatisfied_shapes
        }

        transformation_combinations = list(
            enumerate(itertools.product(*available_transformations.values())))

        for i, transformation_combination in tqdm(transformation_combinations, desc='Transformations', position=0,
                                                  leave=False):
            workflow_name = f'workflow_{workflow_order}_{intent_iri.fragment}_{uuid.uuid4()}'.replace('-', '_')
            wg, w = build_workflow(workflow_name, ontology, dataset, component, transformation_combination)

            wg.add((w, tb.createdFor, intent_iri))
            wg.add((intent_iri, RDF.type, tb.Intent))

            wg.serialize(os.path.join(destination_folder, f'{workflow_name}.ttl'), format='turtle')
            workflow_order += 1

    tqdm.write(f'Generated {workflow_order} workflows')
    return workflow_order


def generate_intent(file: str) -> Graph:
    intent_graph = get_graph_xp()
    intent = ab.term(f'Intent_{file[:-4]}')
    intent_graph.add((intent, RDF.type, tb.Intent))
    intent_graph.add((intent, tb.overData, ab.term('penguins.csv')))
    intent_graph.add((intent, tb.tackles, cb.MainProblem))

    return intent_graph


def run_experiment(file: str, destination_folder: str) -> Tuple[float, int]:
    cbox = Graph()
    cbox.parse(file, format='turtle')
    cbox.parse('../dataset_annotator/penguins_annotated.ttl', format='turtle')

    intent_graph = generate_intent(os.path.basename(file))

    start = time.time()
    num_worflows = build_workflows(cbox, intent_graph, destination_folder)
    end = time.time()

    tqdm.write(f'Experiment took {end - start} seconds')
    return end - start, num_worflows


def run_experiment_suite(source_folder: str, destination_folder: str):
    cboxes = [f for f in os.listdir(source_folder) if f.endswith('.ttl')]
    cboxes = os_sorted(cboxes)
    regex = r'cbox_(\d+)c_(\d+)rpc_(\d+)cpr_(\d+)o.ttl'

    result_file = open(os.path.join(destination_folder, 'results.csv'), 'w')
    result_file.write('comps,reqs_per_comp,comps_per_req,overlap,time,num_workflows\n')

    for cbox in tqdm(cboxes, desc='Experiments', position=2):
        experiment_folder = os.path.join(destination_folder, 'workflows',  cbox.split('.')[0])
        if not os.path.exists(experiment_folder):
            os.makedirs(experiment_folder)

        match = re.match(regex, cbox)
        comps = match.group(1)
        reqs_per_comp = match.group(2)
        comps_per_req = match.group(3)
        overlap = match.group(4)

        tqdm.write(f'Running experiment for {comps}/{reqs_per_comp}/{comps_per_req}/{overlap}')
        tqdm.write(f'\t# Components: {comps}')
        tqdm.write(f'\t# Requirements per component: {reqs_per_comp}')
        tqdm.write(f'\t# Components per requirement: {comps_per_req}')
        tqdm.write(f'\tOverlap: {overlap}')

        t, nw = run_experiment(os.path.join(source_folder, cbox), experiment_folder)
        tqdm.write(f'--------------------------------------------------------------------------------------------')

        result_file.write(f'{comps},{reqs_per_comp},{comps_per_req},{overlap},{t},{nw}\n')


if __name__ == '__main__':
    run_experiment_suite('./fake_cboxes', './results')
