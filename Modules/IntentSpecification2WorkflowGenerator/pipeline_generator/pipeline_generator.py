import itertools
import os
import sys
import time
import uuid
from datetime import datetime
from typing import Tuple, Any, List, Dict, Optional, Union, Set, Type

from pyshacl import validate
from tqdm import tqdm

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

from common import *


def get_intent_iri(intent_graph: Graph) -> URIRef:
    intent_iri_query = f"""
PREFIX tb: <{tb}>
SELECT ?iri
WHERE {{
    ?iri a tb:Intent .
}}
"""
    result = intent_graph.query(intent_iri_query).bindings
    assert len(result) == 1
    return result[0]['iri']


def get_intent_dataset_problem(intent_graph: Graph, intent_iri: URIRef) -> Tuple[URIRef, URIRef]:
    dataset_problem_query = f"""
    PREFIX tb: <{tb}>
    SELECT ?dataset ?problem
    WHERE {{
        {intent_iri.n3()} a tb:Intent .
        {intent_iri.n3()} tb:overData ?dataset .
        {intent_iri.n3()} tb:tackles ?problem .
    }}
"""
    result = intent_graph.query(dataset_problem_query).bindings[0]
    return result['dataset'], result['problem']


def get_intent_params(intent_graph: Graph, intent_iri: URIRef) -> List[Dict[str, Any]]:
    params_query = f"""
    PREFIX tb: <{tb}>
    SELECT ?param ?value
    WHERE {{
        {intent_iri.n3()} a tb:Intent .
        {intent_iri.n3()} tb:usingParameter ?param_value .
        ?param_value tb:forParameter ?param .
        ?param_value tb:has_value ?value .
    }}
"""
    result = intent_graph.query(params_query).bindings
    return result


def get_intent_info(intent_graph: Graph, intent_iri: Optional[URIRef] = None) -> \
        Tuple[URIRef, URIRef, List[Dict[str, Any]], URIRef]:
    if not intent_iri:
        intent_iri = get_intent_iri(intent_graph)

    dataset, problem = get_intent_dataset_problem(intent_graph, intent_iri)
    params = get_intent_params(intent_graph, intent_iri)

    return dataset, problem, params, intent_iri


def get_implementation_input_specs(ontology: Graph, implementation: URIRef) -> List[List[URIRef]]:
    input_spec_query = f"""
        PREFIX tb: <{tb}>
        SELECT ?shape
        WHERE {{
            {implementation.n3()} tb:specifiesInput ?spec .
            ?spec a tb:IOSpec ;
                tb:hasTag ?shape ;
                tb:has_position ?position .
            ?shape a tb:DataTag .
        }}
        ORDER BY ?position
    """
    results = ontology.query(input_spec_query).bindings
    shapes = [flatten_shape(ontology, result['shape']) for result in results]
    return shapes


def get_implementation_output_specs(ontology: Graph, implementation: URIRef) -> List[List[URIRef]]:
    output_spec_query = f"""
        PREFIX tb: <{tb}>
        SELECT ?shape
        WHERE {{
            {implementation.n3()} tb:specifiesOutput ?spec .
            ?spec a tb:IOSpec ;
                tb:hasTag ?shape ;
                tb:has_position ?position .
            ?shape a tb:DataTag .
        }}
        ORDER BY ?position
    """
    results = ontology.query(output_spec_query).bindings
    shapes = [flatten_shape(ontology, result['shape']) for result in results]
    return shapes


def flatten_shape(graph: Graph, shape: URIRef) -> List[URIRef]:
    if (shape, SH['and'], None) in graph:
        subshapes_query = f"""
            PREFIX sh: <{SH}>
            PREFIX rdf: <{RDF}>

            SELECT ?subshape
            WHERE {{
                {shape.n3()} sh:and ?andNode .
                ?andNode rdf:rest*/rdf:first ?subshape .
            }}
        """
        subshapes = graph.query(subshapes_query).bindings

        return [x for subshape in subshapes for x in flatten_shape(graph, subshape['subshape'])]
    else:
        return [shape]


def get_potential_implementations(ontology: Graph, problem_iri: URIRef, intent_parameters=None) -> \
        List[Tuple[URIRef, List[URIRef]]]:
    if intent_parameters is None:
        intent_parameters = []
    intent_params_match = [f'tb:hasParameter {param.n3()} ;' for param in intent_parameters]
    intent_params_separator = '            \n'
    main_implementation_query = f"""
    PREFIX tb: <{tb}>
    SELECT ?implementation
    WHERE {{
        ?implementation a tb:Implementation ;
            {intent_params_separator.join(intent_params_match)}
            tb:implements ?algorithm .
        ?algorithm a tb:Algorithm ;
            tb:solves ?problem .
        ?problem tb:subProblemOf* {problem_iri.n3()} .
        FILTER NOT EXISTS{{
            ?implementation a tb:ApplierImplementation.
        }}
    }}
"""
    results = ontology.query(main_implementation_query).bindings
    implementations = [result['implementation'] for result in results]

    implementations_with_shapes = [
        (implementation, get_implementation_input_specs(ontology, implementation))
        for implementation in implementations]

    return implementations_with_shapes


def get_component_implementation(ontology: Graph, component: URIRef) -> URIRef:
    implementation_query = f"""
        PREFIX tb: <{tb}>
        PREFIX cb: <{cb}>
        SELECT ?implementation
        WHERE {{
            {component.n3()} tb:hasImplementation ?implementation .
        }}
    """
    result = ontology.query(implementation_query).bindings
    assert len(result) == 1
    return result[0]['implementation']


def get_implementation_components(ontology: Graph, implementation: URIRef) -> List[URIRef]:
    components_query = f"""
        PREFIX tb: <{tb}>
        SELECT ?component
        WHERE {{
            ?component tb:hasImplementation {implementation.n3()} .
        }}
    """
    results = ontology.query(components_query).bindings
    return [result['component'] for result in results]


def find_components_to_satisfy_shape(ontology: Graph, shape: URIRef, only_learners: bool = True) -> List[URIRef]:
    implementation_query = f"""
        PREFIX tb: <{tb}>
        SELECT ?implementation
        WHERE {{
            ?implementation a tb:{'Learner' if only_learners else ''}Implementation ;
                tb:specifiesOutput ?spec .
            ?spec tb:hasTag {shape.n3()} .
        }}
    """
    result = ontology.query(implementation_query).bindings
    implementations = [x['implementation'] for x in result]
    components = [c
                  for implementation in implementations
                  for c in get_implementation_components(ontology, implementation)]
    return components


def identify_data_io(ontology: Graph, ios: List[List[URIRef]], return_index: bool = False) -> Union[int, List[URIRef]]:
    for i, io_shapes in enumerate(ios):
        for io_shape in io_shapes:
            if (io_shape, SH.targetClass, dmop.TabularDataset) in ontology:
                return i if return_index else io_shapes


def identify_model_io(ontology: Graph, ios: List[List[URIRef]], return_index: bool = False) -> Union[int, List[URIRef]]:
    for i, io_shapes in enumerate(ios):
        for io_shape in io_shapes:
            query = f'''
    PREFIX sh: <{SH}>
    PREFIX rdfs: <{RDFS}>
    PREFIX cb: <{cb}>

    ASK {{
      {{
        {io_shape.n3()} sh:targetClass ?targetClass .
        ?targetClass rdfs:subClassOf* cb:Model .
      }}
      UNION
      {{
        {io_shape.n3()} rdfs:subClassOf* cb:Model .
      }}
    }}
'''
            if ontology.query(query).askAnswer:
                return i if return_index else io_shapes


def satisfies_shape(data_graph: Graph, shacl_graph: Graph, shape: URIRef, focus: URIRef) -> bool:
    conforms, g, report = validate(data_graph, shacl_graph=shacl_graph, validate_shapes=[shape], focus=focus)
    return conforms


def get_shape_target_class(ontology: Graph, shape: URIRef) -> URIRef:
    return ontology.query(f"""
        PREFIX sh: <{SH}>
        SELECT ?targetClass
        WHERE {{
            <{shape}> sh:targetClass ?targetClass .
        }}
    """).bindings[0]['targetClass']


def get_implementation_parameters(ontology: Graph, implementation: URIRef) -> Dict[
    URIRef, Tuple[Literal, Literal, Literal]]:
    parameters_query = f"""
        PREFIX tb: <{tb}>
        SELECT ?parameter ?value ?order ?condition
        WHERE {{
            <{implementation}> tb:hasParameter ?parameter .
            ?parameter tb:hasDefaultValue ?value ;
                       tb:has_condition ?condition ;
                       tb:has_position ?order .
        }}
        ORDER BY ?order
    """
    results = ontology.query(parameters_query).bindings
    return {param['parameter']: (param['value'], param['order'], param['condition']) for param in results}


def get_component_overriden_parameters(ontology: Graph, component: URIRef) -> Dict[
    URIRef, Tuple[Literal, Literal, Literal]]:
    parameters_query = f"""
        PREFIX tb: <{tb}>
        SELECT ?parameter ?value ?position ?condition
        WHERE {{
            {component.n3()} tb:overridesParameter ?parameterValue .
            ?parameterValue tb:forParameter ?parameter ;
                       tb:has_value ?value .
            ?parameter tb:has_position ?position ;
                       tb:has_condition ?condition .
        }}
    """
    results = ontology.query(parameters_query).bindings
    return {param['parameter']: (param['value'], param['position'], param['condition']) for param in results}


def get_component_parameters(ontology: Graph, component: URIRef) -> Dict[URIRef, Tuple[Literal, Literal, Literal]]:
    implementation = get_component_implementation(ontology, component)
    implementation_params = get_implementation_parameters(ontology, implementation)
    component_params = get_component_overriden_parameters(ontology, component)
    implementation_params.update(component_params)
    return implementation_params


def perform_param_substitution(graph: Graph, parameters: Dict[URIRef, Tuple[Literal, Literal, Literal]],
                               inputs: List[URIRef]) -> Dict[URIRef, Tuple[Literal, Literal, Literal]]:
    keys = list(parameters.keys())
    for param in keys:
        value, order, condition = parameters[param]
        if condition.value is not None and condition.value != '':
            feature_types = get_inputs_feature_types(graph, inputs)
            if condition.value == '$$INTEGER_COLUMN$$' and int not in feature_types:
                parameters.pop(param)
                continue
            if condition.value == '$$STRING_COLUMN$$' and str not in feature_types:
                parameters.pop(param)
                continue
            if condition.value == '$$FLOAT_COLUMN$$' and float not in feature_types:
                parameters.pop(param)
                continue
        if isinstance(value.value, str) and '$$LABEL$$' in value.value:
            new_value = value.replace('$$LABEL$$', f'{get_inputs_label_name(graph, inputs)}')
            parameters[param] = (Literal(new_value), order, condition)
        if isinstance(value.value, str) and '$$NUMERIC_COLUMNS$$' in value.value:
            new_value = value.replace('$$NUMERIC_COLUMNS$$', f'{get_inputs_numeric_columns(graph, inputs)}')
            parameters[param] = (Literal(new_value), order, condition)
        if isinstance(value.value, str) and '$$CSV_PATH$$' in value.value:
            new_value = value.replace('$$CSV_PATH$$', f'{get_csv_path(graph, inputs)}')
            parameters[param] = (Literal(new_value), order, condition)
        if isinstance(value.value, str) and '&amp;' in value.value:
            new_value = value.replace('&amp;', '&')
            parameters[param] = (Literal(new_value), order, condition)

    return parameters


def add_step(graph: Graph, pipeline: URIRef, task_name: str, component: URIRef,
             parameters: Dict[URIRef, Tuple[Literal, Literal, Literal]], order: int,
             previous_task: Union[None, list, URIRef] = None, inputs: Optional[List[URIRef]] = None,
             outputs: Optional[List[URIRef]] = None) -> URIRef:
    if outputs is None:
        outputs = []
    if inputs is None:
        inputs = []
    step = ab.term(task_name)
    graph.add((pipeline, tb.hasStep, step))
    graph.add((step, RDF.type, tb.Step))
    graph.add((step, tb.runs, component))
    graph.add((step, tb.has_position, Literal(order)))
    for i, input in enumerate(inputs):
        in_node = BNode()
        graph.add((in_node, RDF.type, tb.IO))
        graph.add((in_node, tb.hasData, input))
        graph.add((in_node, tb.has_position, Literal(i)))
        graph.add((step, tb.hasInput, in_node))
    for o, output in enumerate(outputs):
        out_node = BNode()
        graph.add((out_node, RDF.type, tb.IO))
        graph.add((out_node, tb.hasData, output))
        graph.add((out_node, tb.has_position, Literal(o)))
        graph.add((step, tb.hasOutput, out_node))
    for parameter, (value, _, _) in parameters.items():
        param_value = BNode()
        graph.add((step, tb.hasParameterValue, param_value))
        graph.add((param_value, tb.forParameter, parameter))
        graph.add((param_value, tb.has_value, value))
    if previous_task:
        if isinstance(previous_task, list):
            for previous in previous_task:
                graph.add((previous, tb.followedBy, step))
        else:
            graph.add((previous_task, tb.followedBy, step))
    return step


def get_component_transformations(ontology: Graph, component: URIRef) -> List[URIRef]:
    transformation_query = f'''
        PREFIX tb: <{tb}>
        SELECT ?transformation
        WHERE {{
            <{component}> tb:hasTransformation ?transformation_list .
            ?transformation_list rdf:rest*/rdf:first ?transformation .
        }}
    '''
    transformations = ontology.query(transformation_query).bindings
    return [x['transformation'] for x in transformations]


def get_inputs_label_name(graph: Graph, inputs: List[URIRef]) -> str:
    data_input = next(i for i in inputs if (i, RDF.type, dmop.TabularDataset) in graph)
    label_query = f"""
        PREFIX rdfs: <{RDFS}>
        PREFIX dmop: <{dmop}>

        SELECT ?label
        WHERE {{
            {data_input.n3()} dmop:hasColumn ?column .
            ?column dmop:isLabel true ;
                    dmop:hasColumnName ?label .

        }}
    """
    return graph.query(label_query).bindings[0]['label'].value


def get_inputs_numeric_columns(graph: Graph, inputs: List[URIRef]) -> str:
    data_input = next(i for i in inputs if (i, RDF.type, dmop.TabularDataset) in graph)
    columns_query = f"""
        PREFIX rdfs: <{RDFS}>
        PREFIX dmop: <{dmop}>

        SELECT ?label
        WHERE {{
            {data_input.n3()} dmop:hasColumn ?column .
            ?column dmop:isFeature true ;
                    dmop:hasDataPrimitiveTypeColumn ?type ;
                    dmop:hasColumnName ?label .
            FILTER(?type IN (dmop:Float, dmop:Int, dmop:Number, dmop:Double, dmop:Long, dmop:Short, dmop:Integer))
        }}
    """
    columns = graph.query(columns_query).bindings
    return ','.join([x['label'].value for x in columns])


def get_csv_path(graph: Graph, inputs: List[URIRef]) -> str:
    data_input = next(i for i in inputs if (i, RDF.type, dmop.TabularDataset) in graph)
    path = next(graph.objects(data_input, dmop.path), True)
    return path.value


def get_inputs_feature_types(graph: Graph, inputs: List[URIRef]) -> Set[Type]:
    data_input = next(i for i in inputs if (i, RDF.type, dmop.TabularDataset) in graph)
    columns_query = f"""
        PREFIX rdfs: <{RDFS}>
        PREFIX dmop: <{dmop}>

        SELECT ?type
        WHERE {{
            {data_input.n3()} dmop:hasColumn ?column .
            ?column dmop:isFeature true ;
                    dmop:hasDataPrimitiveTypeColumn ?type .
        }}
    """
    columns = graph.query(columns_query).bindings
    mapping = {
        dmop.Float: float,
        dmop.Int: int,
        dmop.Integer: int,
        dmop.Number: float,
        dmop.Double: float,
        dmop.String: str,
    }
    return set([mapping[x['type']] for x in columns])


def copy_subgraph(source_graph: Graph, source_node: URIRef, destination_graph: Graph, destination_node: URIRef,
                  replace_nodes: bool = True) -> None:
    visited_nodes = set()
    nodes_to_visit = [source_node]
    mappings = {source_node: destination_node}

    while nodes_to_visit:
        current_node = nodes_to_visit.pop()
        visited_nodes.add(current_node)
        for predicate, object in source_graph.predicate_objects(current_node):
            if predicate == OWL.sameAs:
                continue
            if replace_nodes and isinstance(object, IdentifiedNode):
                if predicate == RDF.type or object in dmop:
                    mappings[object] = object
                else:
                    if object not in visited_nodes:
                        nodes_to_visit.append(object)
                    if object not in mappings:
                        mappings[object] = BNode()
                destination_graph.add((mappings[current_node], predicate, mappings[object]))
            else:
                destination_graph.add((mappings[current_node], predicate, object))


def annotate_io_with_spec(ontology: Graph, workflow_graph: Graph, io: URIRef, io_spec: List[URIRef]) -> None:
    for spec in io_spec:
        io_spec_class = next(ontology.objects(spec, SH.targetClass, True), None)
        if io_spec_class is None or (io, RDF.type, io_spec_class) in workflow_graph:
            continue
        workflow_graph.add((io, RDF.type, io_spec_class))


def annotate_ios_with_specs(ontology: Graph, workflow_graph: Graph, io: List[URIRef],
                            specs: List[List[URIRef]]) -> None:
    assert len(io) == len(specs), 'Number of IOs and specs must be the same'
    for io, spec in zip(io, specs):
        annotate_io_with_spec(ontology, workflow_graph, io, spec)


def run_copy_transformation(ontology: Graph, workflow_graph: Graph, transformation: URIRef, inputs: List[URIRef],
                            outputs: List[URIRef]):
    input_index = next(ontology.objects(transformation, tb.copy_input, True)).value
    output_index = next(ontology.objects(transformation, tb.copy_output, True)).value
    input = inputs[input_index - 1]
    output = outputs[output_index - 1]

    copy_subgraph(workflow_graph, input, workflow_graph, output)


def run_component_transformation(ontology: Graph, workflow_graph: Graph, component: URIRef, inputs: List[URIRef],
                                 outputs: List[URIRef],
                                 parameters: Dict[URIRef, Tuple[Literal, Literal, Literal]]) -> None:
    transformations = get_component_transformations(ontology, component)
    for transformation in transformations:
        if (transformation, RDF.type, tb.CopyTransformation) in ontology:
            run_copy_transformation(ontology, workflow_graph, transformation, inputs, outputs)
        elif (transformation, RDF.type, tb.LoaderTransformation) in ontology:
            continue
        else:
            prefixes = f'''
PREFIX tb: <{tb}>
PREFIX ab: <{ab}>
PREFIX rdf: <{RDF}>
PREFIX rdfs: <{RDFS}>
PREFIX owl: <{OWL}>
PREFIX xsd: <{XSD}>
PREFIX dmop: <{dmop}>
'''
            query = next(ontology.objects(transformation, tb.transformation_query, True)).value
            query = prefixes + query
            for i in range(len(inputs)):
                query = query.replace(f'$input{i + 1}', f'{inputs[i].n3()}')
            for i in range(len(outputs)):
                query = query.replace(f'$output{i + 1}', f'{outputs[i].n3()}')
            for param, (value, order, _) in parameters.items():
                query = query.replace(f'$param{order + 1}', f'{value.n3()}')
                query = query.replace(f'$parameter{order + 1}', f'{value.n3()}')
            workflow_graph.update(query)


def get_step_name(workflow_name: str, task_order: int, implementation: URIRef) -> str:
    return f'{workflow_name}-step_{task_order}_{implementation.fragment.replace("-", "_")}'


def add_loader_step(ontology: Graph, workflow_graph: Graph, workflow: URIRef, dataset_node: URIRef) -> URIRef:
    loader_component = cb.term('component-csv_local_reader')
    loader_step_name = get_step_name(workflow.fragment, 0, loader_component)
    loader_parameters = get_component_parameters(ontology, loader_component)
    loader_parameters = perform_param_substitution(workflow_graph, loader_parameters, [dataset_node])
    return add_step(workflow_graph, workflow, loader_step_name, loader_component, loader_parameters, 0, None, None,
                    [dataset_node])


def build_workflow_train_test(workflow_name: str, ontology: Graph, dataset: URIRef, main_component: URIRef,
                              split_component: URIRef, transformations: List[URIRef]) -> Tuple[Graph, URIRef]:
    workflow_graph = get_graph_xp()
    workflow = ab.term(workflow_name)
    workflow_graph.add((workflow, RDF.type, tb.Workflow))
    task_order = 0

    dataset_node = ab.term(f'{workflow_name}-original_dataset')

    copy_subgraph(ontology, dataset, workflow_graph, dataset_node)

    loader_step = add_loader_step(ontology, workflow_graph, workflow, dataset_node)
    task_order += 1

    split_step_name = get_step_name(workflow_name, task_order, split_component)
    split_outputs = [ab[f'{split_step_name}-output_train'], ab[f'{split_step_name}-output_test']]
    split_parameters = get_component_parameters(ontology, split_component)
    split_step = add_step(workflow_graph, workflow,
                          split_step_name,
                          split_component,
                          split_parameters,
                          task_order,
                          loader_step,
                          [dataset_node],
                          split_outputs)
    run_component_transformation(ontology, workflow_graph, split_component,
                                 [dataset_node], split_outputs,
                                 split_parameters)

    task_order += 1

    train_dataset_node = split_outputs[0]
    test_dataset_node = split_outputs[1]

    previous_train_step = split_step
    previous_test_step = split_step

    for train_component in [*transformations, main_component]:
        test_component = next(ontology.objects(train_component, tb.hasApplier, True), train_component)
        same = train_component == test_component

        train_step_name = get_step_name(workflow_name, task_order, train_component)
        test_step_name = get_step_name(workflow_name, task_order + 1, test_component)

        train_input_specs = get_implementation_input_specs(ontology,
                                                           get_component_implementation(ontology, train_component))
        train_input_data_index = identify_data_io(ontology, train_input_specs, return_index=True)
        train_transformation_inputs = [ab[f'{train_step_name}-input_{i}'] for i in range(len(train_input_specs))]
        train_transformation_inputs[train_input_data_index] = train_dataset_node
        annotate_ios_with_specs(ontology, workflow_graph, train_transformation_inputs,
                                train_input_specs)

        train_output_specs = get_implementation_output_specs(ontology,
                                                             get_component_implementation(ontology, train_component))
        train_output_model_index = identify_model_io(ontology, train_output_specs, return_index=True)
        train_output_data_index = identify_data_io(ontology, train_output_specs, return_index=True)
        train_transformation_outputs = [ab[f'{train_step_name}-output_{i}'] for i in range(len(train_output_specs))]
        annotate_ios_with_specs(ontology, workflow_graph, train_transformation_outputs,
                                train_output_specs)

        train_parameters = get_component_parameters(ontology, train_component)
        train_parameters = perform_param_substitution(workflow_graph, train_parameters, train_transformation_inputs)
        train_step = add_step(workflow_graph, workflow,
                              train_step_name,
                              train_component, train_parameters, task_order, previous_train_step,
                              train_transformation_inputs,
                              train_transformation_outputs)

        previous_train_step = train_step

        run_component_transformation(ontology, workflow_graph, train_component, train_transformation_inputs,
                                     train_transformation_outputs, train_parameters)

        if train_output_data_index is not None:
            train_dataset_node = train_transformation_outputs[train_output_data_index]

        task_order += 1

        test_input_specs = get_implementation_input_specs(ontology,
                                                          get_component_implementation(ontology, test_component))
        test_input_data_index = identify_data_io(ontology, test_input_specs, return_index=True)
        test_input_model_index = identify_model_io(ontology, test_input_specs, return_index=True)
        test_transformation_inputs = [ab[f'{test_step_name}-input_{i}'] for i in range(len(test_input_specs))]
        test_transformation_inputs[test_input_data_index] = test_dataset_node
        test_transformation_inputs[test_input_model_index] = train_transformation_outputs[train_output_model_index]
        annotate_ios_with_specs(ontology, workflow_graph, test_transformation_inputs,
                                test_input_specs)

        test_output_specs = get_implementation_output_specs(ontology,
                                                            get_component_implementation(ontology, test_component))
        test_output_data_index = identify_data_io(ontology, test_output_specs, return_index=True)
        test_transformation_outputs = [ab[f'{test_step_name}-output_{i}'] for i in range(len(test_output_specs))]
        annotate_ios_with_specs(ontology, workflow_graph, test_transformation_outputs,
                                test_output_specs)

        previous_test_steps = [previous_test_step, train_step] if not same else [previous_test_step]
        test_parameters = get_component_parameters(ontology, test_component)
        test_parameters = perform_param_substitution(workflow_graph, test_parameters, test_transformation_inputs)
        test_step = add_step(workflow_graph, workflow,
                             test_step_name,
                             test_component, test_parameters, task_order, previous_test_steps,
                             test_transformation_inputs,
                             test_transformation_outputs)

        run_component_transformation(ontology, workflow_graph, test_component, test_transformation_inputs,
                                     test_transformation_outputs, test_parameters)

        test_dataset_node = test_transformation_outputs[test_output_data_index]
        previous_test_step = test_step
        task_order += 1

    saver_component = cb.term('component-csv_local_writer')
    saver_step_name = get_step_name(workflow_name, task_order + 1, saver_component)
    saver_parameters = get_component_parameters(ontology, saver_component)
    saver_parameters = perform_param_substitution(workflow_graph, saver_parameters, [test_dataset_node])
    add_step(workflow_graph, workflow, saver_step_name, saver_component, saver_parameters, task_order,
             previous_test_step, [test_dataset_node], [])

    return workflow_graph, workflow


def build_workflows(ontology: Graph, intent_graph: Graph, destination_folder: str, log: bool = False) -> None:
    dataset, problem, intent_params, intent_iri = get_intent_info(intent_graph)

    if log:
        tqdm.write(f'Dataset: {dataset.fragment}')
        tqdm.write(f'Problem: {problem.fragment}')
        tqdm.write(f'Intent params: {intent_params}')
        tqdm.write('-------------------------------------------------')

    impls = get_potential_implementations(ontology, problem, [x['param'] for x in intent_params])
    components = [
        (c, impl, inputs)
        for impl, inputs in impls
        for c in get_implementation_components(ontology, impl)
    ]
    if log:
        for component, implementation, inputs in components:
            tqdm.write(f'Component: {component.fragment} ({implementation.fragment})')
            for im_input in inputs:
                tqdm.write(f'\tInput: {[x.fragment for x in im_input]}')
        tqdm.write('-------------------------------------------------')

    workflow_order = 0

    split_components = [
        cb.term('component-random_absolute_train_test_split'),
        cb.term('component-random_relative_train_test_split'),
        cb.term('component-top_k_absolute_train_test_split'),
        cb.term('component-top_k_relative_train_test_split'),
    ]

    for component, implementation, inputs in tqdm(components, desc='Components', position=1):
        if log:
            tqdm.write(f'Component: {component.fragment} ({implementation.fragment})')
        shapes_to_satisfy = identify_data_io(ontology, inputs)
        assert shapes_to_satisfy is not None and len(shapes_to_satisfy) > 0
        if log:
            tqdm.write(f'\tData input: {[x.fragment for x in shapes_to_satisfy]}')

        unsatisfied_shapes = [shape for shape in shapes_to_satisfy if
                              not satisfies_shape(ontology, ontology, shape, dataset)]

        available_transformations = {
            shape: find_components_to_satisfy_shape(ontology, shape, only_learners=True)
            for shape in unsatisfied_shapes
        }

        if log:
            tqdm.write(f'\tUnsatisfied shapes: ')
            for shape, transformations in available_transformations.items():
                tqdm.write(f'\t\t{shape.fragment}: {[x.fragment for x in transformations]}')

        transformation_combinations = list(
            enumerate(itertools.product(split_components, *available_transformations.values())))
        # TODO - check if the combination is valid and whether further transformations are needed

        if log:
            tqdm.write(f'\tTotal combinations: {len(transformation_combinations)}')

        for i, transformation_combination in tqdm(transformation_combinations, desc='Transformations', position=0,
                                                  leave=False):
            if log:
                tqdm.write(
                    f'\t\tCombination {i + 1} / {len(transformation_combinations)}: {[x.fragment for x in transformation_combination]}')

            workflow_name = f'workflow_{workflow_order}_{intent_iri.fragment}_{uuid.uuid4()}'.replace('-', '_')
            wg, w = build_workflow_train_test(workflow_name, ontology, dataset, component,
                                              transformation_combination[0],
                                              transformation_combination[1:])

            wg.add((w, tb.createdFor, intent_iri))
            wg.add((intent_iri, RDF.type, tb.Intent))

            if log:
                tqdm.write(f'\t\tWorkflow {workflow_order}: {w.fragment}')
            wg.serialize(os.path.join(destination_folder, f'{workflow_name}.ttl'), format='turtle')
            workflow_order += 1


def interactive():
    intent_graph = get_graph_xp()
    intent = input('Introduce the intent name [DescriptionIntent]: ') or 'DescriptionIntent'
    data = input('Introduce the data name [titanic.csv]: ') or 'titanic.csv'
    problem = input('Introduce the problem name [Description]: ') or 'Description'

    intent_graph.add((ab.term(intent), RDF.type, tb.Intent))
    intent_graph.add((ab.term(intent), tb.overData, ab.term(data)))
    intent_graph.add((ab.term(intent), tb.tackles, cb.term(problem)))

    ontology = get_ontology_graph()

    folder = input('Introduce the folder to save the workflows: ')
    if folder == '':
        folder = f'./workflows/{datetime.now().strftime("%Y-%m-%d %H-%M-%S")}/'
        tqdm.write(f'No folder introduced, using default ({folder})')
    if not os.path.exists(folder):
        tqdm.write('Directory does not exist, creating it')
        os.makedirs(folder)

    t = time.time()
    build_workflows(ontology, intent_graph, folder, log=True)
    t = time.time() - t

    print(f'Workflows built in {t} seconds')
    print(f'Workflows saved in {folder}')


if __name__ == '__main__':
    if len(sys.argv) > 1:
        pass
    else:
        interactive()
