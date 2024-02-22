import os
import sys
import tempfile
import zipfile
import shutil
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from typing import Tuple, Dict, List

from tqdm import tqdm

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))

from common import *

try:
    import easygui
except ImportError:
    easygui = None


def load_workflow(path: str) -> Graph:
    graph = get_graph_xp()
    graph.parse(path, format='turtle')
    return graph


def get_workflow_steps(graph: Graph) -> List[URIRef]:
    steps = list(graph.subjects(RDF.type, tb.Step))

    connections = list(graph.subject_objects(tb.followedBy))
    disordered = True
    while disordered:
        disordered = False
        for source, target in connections:
            si = steps.index(source)
            ti = steps.index(target)
            if si > ti:
                disordered = True
                steps[si] = target
                steps[ti] = source
    return steps


def get_base_node_config():
    root = ET.Element('config', {'xmlns': 'http://www.knime.org/2008/09/XMLConfig',
                                 'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                                 'xsi:schemaLocation': 'http://www.knime.org/2008/09/XMLConfig http://www.knime.org/XMLConfig_2008_09.xsd',
                                 'key': 'settings.xml',
                                 })

    ET.SubElement(root, 'entry', {'key': 'node_file', 'type': 'xstring', 'value': 'settings.xml'})
    ET.SubElement(root, 'entry', {'key': 'customDescription', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(root, 'entry', {'key': 'state', 'type': 'xstring', 'value': 'CONFIGURED'})
    ET.SubElement(root, 'entry', {'key': 'hasContent', 'type': 'xboolean', 'value': 'false'})
    ET.SubElement(root, 'entry', {'key': 'isInactive', 'type': 'xboolean', 'value': 'false'})
    ET.SubElement(root, 'config', {'key': 'factory_settings'})
    ET.SubElement(root, 'entry', {'key': 'node-feature-name', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(root, 'entry',
                  {'key': 'node-feature-symbolic-name', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(root, 'entry', {'key': 'node-feature-vendor', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(root, 'entry', {'key': 'node-feature-version', 'type': 'xstring', 'value': '0.0.0'})

    ET.SubElement(root, 'config', {'key': 'flow_stack'})

    return ET.ElementTree(root)


def get_step_component_implementation(ontology: Graph, workflow_graph: Graph, step: URIRef) -> Tuple[URIRef, URIRef]:
    component = next(workflow_graph.objects(step, tb.runs, True))
    implementation = next(ontology.objects(component, tb.hasImplementation, True))
    return component, implementation


def get_knime_properties(ontology: Graph, implementation: URIRef) -> Dict[str, str]:
    results = {}
    for p, o in ontology.predicate_objects(implementation):
        if p.fragment.startswith('knime'):
            results[p.fragment[6:]] = o.value
    return results


def get_number_of_output_ports(ontology: Graph, workflow_graph: Graph, step: URIRef) -> int:
    _, implementation = get_step_component_implementation(ontology, workflow_graph, step)
    return sum(1 for _ in ontology.objects(implementation, tb.specifiesOutput))


def get_step_parameters(ontology: Graph, workflow_graph: Graph, step: URIRef) -> List[Tuple[str, str, str, URIRef]]:
    param_values = list(workflow_graph.objects(step, tb.hasParameterValue))
    parameters = [next(workflow_graph.objects(pv, tb.forParameter, True)) for pv in param_values]
    values = [next(workflow_graph.objects(pv, tb.has_value, True)).value for pv in param_values]
    keys = [next(ontology.objects(p, tb.knime_key, True)).value for p in parameters]
    paths = [next(ontology.objects(p, tb.knime_path, True)).value for p in parameters]
    types = [next(ontology.objects(p, tb.hasDatatype, True)) for p in parameters]
    return list(zip(keys, values, paths, types))


def update_hierarchy(path_elements: Dict[str, ET.Element], path: str) -> Dict[str, ET.Element]:
    if path in path_elements:
        return path_elements
    levels = path.split('/')
    current_path = ''
    previous = None
    for level in levels:
        current_path += level
        if current_path not in path_elements:
            path_elements[current_path] = ET.SubElement(previous, 'config', {'key': level})
        previous = path_elements[current_path]
        current_path += '/'


def get_step_model_config(ontology: Graph, workflow_graph: Graph, step: URIRef) -> ET.Element:
    path_elements = {
        'model': ET.Element('config', {'key': 'model'}),
    }

    types = {
        XSD.string: 'xstring',
        cb.term('char'): 'xchar',
        XSD.int: 'xint',
        XSD.integer: 'xint',
        XSD.long: 'xlong',
        XSD.float: 'xdouble',
        XSD.double: 'xdouble',
        XSD.boolean: 'xboolean',
    }

    parameters = get_step_parameters(ontology, workflow_graph, step)
    for key, value, path, value_type in parameters:
        update_hierarchy(path_elements, path)
        base = path_elements[path]
        if value_type == RDF.List:
            values = value.split(',')
            config = ET.SubElement(base, 'config', {'key': key})
            ET.SubElement(config, 'entry', {'key': 'array-size', 'type': 'xint', 'value': str(len(values))})
            for i, v in enumerate(values):
                ET.SubElement(config, 'entry', {'key': str(i), 'type': 'xstring', 'value': v})
        elif key == '$$SKIP$$':
            continue
        else:
            if value is None or (isinstance(value, str) and value.lower() == 'none'):
                ET.SubElement(base, 'entry', {'key': key, 'type': types[value_type], 'value': '', 'isnull': 'true'})
            else:
                ET.SubElement(base, 'entry', {'key': key, 'type': types[value_type], 'value': str(value)})

    return path_elements['model']


def create_step_file(ontology: Graph, workflow_graph: Graph, step: URIRef, folder, iterator: int) -> str:
    tree = get_base_node_config()
    root = tree.getroot()

    component, implementation = get_step_component_implementation(ontology, workflow_graph, step)
    properties = get_knime_properties(ontology, implementation)

    for key, value in properties.items():
        ET.SubElement(root, 'entry', {'key': key, 'type': 'xstring', 'value': value})

    path_name = properties["node-name"].replace('(', '_').replace(')', '_')

    model = get_step_model_config(ontology, workflow_graph, step)
    root.append(model)

    filestores = ET.SubElement(root, 'config', {'key': 'filestores'})
    ET.SubElement(filestores, 'entry', {'key': 'file_store_location', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(filestores, 'entry', {'key': 'file_store_id', 'type': 'xstring', 'isnull': 'true', 'value': ''})

    internal_node_subsettings = ET.SubElement(root, 'config', {'key': 'internal_node_subsettings'})
    ET.SubElement(internal_node_subsettings, 'entry',
                  {'key': 'memory_policy', 'type': 'xstring', 'value': 'CacheSmallInMemory'})

    ports = ET.SubElement(root, 'config', {'key': 'ports'})
    for i in range(get_number_of_output_ports(ontology, workflow_graph, step)):
        port = ET.SubElement(ports, 'config', {'key': f'port_{i + 1}'})
        ET.SubElement(port, 'entry', {'key': 'index', 'type': 'xint', 'value': str(i + 1)})
        ET.SubElement(port, 'entry', {'key': 'port_dir_location', 'type': 'xstring', 'isnull': 'true', 'value': ''})

    subfolder_name = f'{path_name} (#{iterator})'
    subfolder = os.path.join(folder, subfolder_name)
    os.mkdir(subfolder)

    ET.indent(tree, space='    ')
    tree.write(os.path.join(subfolder, 'settings.xml'), encoding='utf-8', xml_declaration=True)
    return subfolder_name


def get_workflow_intent_name(workflow_graph: Graph) -> str:
    return next(workflow_graph.subjects(RDF.type, tb.Intent, True)).fragment


def get_workflow_intent_number(workflow_graph: Graph) -> int:
    return int(next(workflow_graph.subjects(RDF.type, tb.Workflow, True)).fragment.split('_')[1])


def create_workflow_metadata_file(workflow_graph: Graph, folder: str) -> None:
    author = 'ODIN'
    date = datetime.today().strftime('%d/%m/%Y')
    workflow_name = next(workflow_graph.subjects(RDF.type, tb.Workflow, True)).fragment
    title = f'{get_workflow_intent_name(workflow_graph)} (Workflow {get_workflow_intent_number(workflow_graph)})'
    description = f'This workflow was automatically created from the logical workflow {workflow_name}.'
    url = 'ExtremeXP https://extremexp.eu/'
    tags = 'model training, training, testing'
    template = f'''<?xml version="1.0" encoding="UTF-8"?>
<KNIMEMetaInfo nrOfElements="3">
    <element form="text" read-only="false" name="Author">{author}</element>
    <element form="date" name="Creation Date" read-only="false">{date}</element>
    <element form="multiline" name="Comments" read-only="false">{title}&#13;
&#13;
{description}&#13;
&#13;
URL: {url}&#13;
TAG: {tags}&#13;
</element>
</KNIMEMetaInfo>
'''
    with open(os.path.join(folder, 'workflowset.meta'), 'w') as f:
        f.write(template)


def get_nodes_config(step_paths: List[str]) -> ET.Element:
    root = ET.Element('config', {'key': 'nodes'})
    for i, step in enumerate(step_paths):
        node_cofig = ET.SubElement(root, 'config', {'key': f'node_{i}'})
        ET.SubElement(node_cofig, 'entry', {'key': 'id', 'type': 'xint', 'value': str(i)})
        ET.SubElement(node_cofig, 'entry', {'key': 'node_settings_file', 'type': 'xstring',
                                            'value': f'{step}/settings.xml'})
        ET.SubElement(node_cofig, 'entry', {'key': 'node_is_meta', 'type': 'xboolean', 'value': 'false'})
        ET.SubElement(node_cofig, 'entry', {'key': 'node_type', 'type': 'xstring', 'value': 'NativeNode'})
        ET.SubElement(node_cofig, 'entry', {'key': 'ui_classname', 'type': 'xstring',
                                            'value': 'org.knime.core.node.workflow.NodeUIInformation'})
        ui_settings = ET.SubElement(node_cofig, 'config', {'key': 'ui_settings'})
        bounds = ET.SubElement(ui_settings, 'config', {'key': 'extrainfo.node.bounds'})

        applier = any(x in step.lower() for x in ['appl', 'predictor'])
        ET.SubElement(bounds, 'entry', {'key': 'array-size', 'type': 'xint', 'value': '4'})
        ET.SubElement(bounds, 'entry', {'key': '0', 'type': 'xint', 'value': str((i + 1) * 150)})  # x
        ET.SubElement(bounds, 'entry', {'key': '1', 'type': 'xint', 'value': '400' if applier else '200'})  # y
        ET.SubElement(bounds, 'entry', {'key': '2', 'type': 'xint', 'value': '75'})  # width
        ET.SubElement(bounds, 'entry', {'key': '3', 'type': 'xint', 'value': '80'})  # height

    return root


def get_workflow_connections(workflow_graph: Graph) -> List[Tuple[URIRef, URIRef, URIRef, URIRef]]:
    query = f'''
    PREFIX tb: <{tb}>
    SELECT ?source ?destination ?sourcePort ?destinationPort
    WHERE {{
        ?source a tb:Step ;
                tb:followedBy ?destination ;
                tb:hasOutput ?output .
        ?output tb:has_position ?sourcePort ;
                tb:hasData ?link .
        ?destination a tb:Step ;
                    tb:hasInput ?input .
        ?input tb:has_position ?destinationPort ;
                tb:hasData ?link .
    }}
    '''
    results = workflow_graph.query(query).bindings
    return [(r['source'], r['destination'], r['sourcePort'], r['destinationPort']) for r in results]


def get_connections_config(workflow_graph: Graph, steps: List[URIRef]) -> ET.Element:
    root = ET.Element('config', {'key': 'connections'})
    connections = get_workflow_connections(workflow_graph)
    for i, (source, destination, source_port, destination_port) in enumerate(connections):
        connection_config = ET.SubElement(root, 'config', {'key': f'connection_{i}'})
        ET.SubElement(connection_config, 'entry',
                      {'key': 'sourceID', 'type': 'xint', 'value': str(steps.index(source))})
        ET.SubElement(connection_config, 'entry', {'key': 'sourcePort', 'type': 'xint', 'value': str(source_port + 1)})
        ET.SubElement(connection_config, 'entry',
                      {'key': 'destID', 'type': 'xint', 'value': str(steps.index(destination))})
        ET.SubElement(connection_config, 'entry',
                      {'key': 'destPort', 'type': 'xint', 'value': str(destination_port + 1)})
        ET.SubElement(connection_config, 'entry', {'key': 'ui_classname', 'type': 'xstring',
                                                   'value': 'org.knime.core.node.workflow.ConnectionUIInformation'})
        ui_settings = ET.SubElement(connection_config, 'config', {'key': 'ui_settings'})
        ET.SubElement(ui_settings, 'entry', {'key': 'extrainfo.conn.bendpoints_size', 'type': 'xint', 'value': '0'})
    return root


def get_author_config() -> ET.Element:
    root = ET.Element('config', {'key': 'authorInformation'})
    ET.SubElement(root, 'entry', {'key': 'authored-by', 'type': 'xstring', 'value': 'Diviloper'})
    ET.SubElement(root, 'entry', {'key': 'authored-when', 'type': 'xstring',
                                  'value': datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S %z')})
    ET.SubElement(root, 'entry', {'key': 'lastEdited-by', 'type': 'xstring', 'value': 'Diviloper'})
    ET.SubElement(root, 'entry', {'key': 'lastEdited-when', 'type': 'xstring',
                                  'value': datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S %z')})
    return root


def create_workflow_file(workflow_graph: Graph, steps: List[URIRef], step_paths: List[str],
                         folder: str) -> None:
    node_config = get_nodes_config(step_paths)
    connections_config = get_connections_config(workflow_graph, steps)
    author_config = get_author_config()

    root = ET.Element('config', {'xmlns': 'http://www.knime.org/2008/09/XMLConfig',
                                 'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                                 'xsi:schemaLocation': 'http://www.knime.org/2008/09/XMLConfig http://www.knime.org/XMLConfig_2008_09.xsd',
                                 'key': 'workflow.knime'})

    ET.SubElement(root, 'entry', {'key': 'created_by', 'type': 'xstring', 'value': '4.7.3.v202305100921'})
    ET.SubElement(root, 'entry', {'key': 'created_by_nightly', 'type': 'xboolean', 'value': 'false'})
    ET.SubElement(root, 'entry', {'key': 'version', 'type': 'xstring', 'value': '4.1.0'})
    ET.SubElement(root, 'entry', {'key': 'name', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(root, 'entry', {'key': 'customDescription', 'type': 'xstring', 'isnull': 'true', 'value': ''})
    ET.SubElement(root, 'entry', {'key': 'state', 'type': 'xstring', 'value': 'IDLE'})
    ET.SubElement(root, 'config', {'key': 'workflow_credentials'})

    root.append(node_config)
    root.append(connections_config)
    root.append(author_config)

    tree = ET.ElementTree(root)
    ET.indent(tree, space='    ')
    tree.write(os.path.join(folder, 'workflow.knime'), encoding='UTF-8', xml_declaration=True)


def package_workflow(folder: str, destination: str) -> None:
    with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, _, files in os.walk(folder):
            for file in files:
                file_path = os.path.join(root, file)
                archive_path = os.path.relpath(file_path, folder)
                zipf.write(file_path, arcname=os.path.join(os.path.basename(folder), archive_path))


def translate_graph(ontology: Graph, source_path: str, destination_path: str, keep_folder=False) -> None:
    tqdm.write('Creating new workflow')

    tqdm.write('\tCreating temp folder: ', end='')
    temp_folder = tempfile.mkdtemp()
    tqdm.write(temp_folder)

    tqdm.write('\tLoading workflow:', end=' ')
    graph = load_workflow(source_path)
    tqdm.write(next(graph.subjects(RDF.type, tb.Workflow, True)).fragment)

    tqdm.write('\tCreating workflow metadata file')
    create_workflow_metadata_file(graph, temp_folder)

    tqdm.write('\tBuilding steps')
    steps = get_workflow_steps(graph)
    step_paths = []
    for i, step in enumerate(steps):
        step_paths.append(create_step_file(ontology, graph, step, temp_folder, i))

    tqdm.write('\tCreating workflow file')
    create_workflow_file(graph, steps, step_paths, temp_folder)

    tqdm.write('\tCreating zip file')
    package_workflow(temp_folder, destination_path)

    if keep_folder:
        tqdm.write('\tCopying temp folder')
        shutil.copytree(temp_folder, destination_path[:-4])

    tqdm.write('\tRemoving temp folder')
    shutil.rmtree(temp_folder)
    tqdm.write('Done')
    tqdm.write('-' * 50)


def translate_graph_folder(ontology: Graph, source_folder: str, destination_folder: str, keep_folder=False) -> None:
    if not os.path.exists(destination_folder):
        os.makedirs(destination_folder)
    assert os.path.exists(source_folder)

    workflows = [f for f in os.listdir(source_folder) if f.endswith('.ttl')]
    for workflow in tqdm(workflows):
        source_path = os.path.join(source_folder, workflow)
        destination_path = os.path.join(destination_folder, workflow[:-4] + '.knwf')
        translate_graph(ontology, source_path, destination_path, keep_folder)


def interactive():
    if easygui is None:
        source_folder = input('Source folder: ')
        destination_folder = input('Destination folder: ')
    else:
        source_folder = easygui.diropenbox('Source folder', 'Source folder', '.')
        print(f'Source folder: {source_folder}')
        destination_folder = easygui.diropenbox('Destination folder', 'Destination folder', '.')
        print(f'Destination folder: {destination_folder}')

    keep_folder = input('Keep workflows in folder format? [Y/n]: ').lower() not in ['n', 'no']

    translate_graph_folder(get_ontology_graph(), source_folder, destination_folder, keep_folder=keep_folder)


if __name__ == '__main__':
    if len(sys.argv) == 3:
        translate_graph_folder(get_ontology_graph(), sys.argv[1], sys.argv[2])
    elif len(sys.argv) == 4:
        translate_graph(get_ontology_graph(), sys.argv[2], sys.argv[3], keep_folder=True)
    else:
        print('Interactive usage.')
        print('For non-interactive usage, use:')
        print('\tpython workflow_translator.py <source_folder> <destination_folder>')
        print('\tpython workflow_translator.py --keep <source_folder> <destination_folder>')
        interactive()
