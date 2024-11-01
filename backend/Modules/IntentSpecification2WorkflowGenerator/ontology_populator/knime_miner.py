import json
import os
from typing import List, Tuple


def get_nodes(data, path):
    nodes = []
    if 'nodes' in data:
        nodes += [(path + node['name'], node) for node in data['nodes']]
    if 'children' in data:
        for child in data['children']:
            nodes += get_nodes(child, path + '/' + data['name'])
    return nodes


def get_name(node):
    substitutions = [
        (' ', '_'),
        ('-', '_'),
        ('/', '_'),
        ('(', ''),
        (')', ''),
    ]
    name = node['name'].lower().strip()
    for old, new in substitutions:
        name = name.replace(old, new)
    return name


def get_implementation_code(node):
    name = get_name(node)

    io_class_translation = {
        'org.knime.core.node.BufferedDataTable': 'ds.TabularDataset',
        'org.knime.base.data.normalize.NormalizerPortObject': 'ds.NormalizerModel',
    }

    params = '[\n'
    for parameter in node['options'] if 'options' in node else []:
        params += f"        Parameter('{parameter['name']}', None, None),  # TODO: check parameter\n"
    params += '    ]'
    inputs = '[\n'
    for input in node['inPorts'] if 'inPorts' in node else []:
        input_class = io_class_translation.get(input['portObjectClass'])
        inputs += f"        {input_class},  # TODO: check input, original: '{input['portObjectClass']}'\n"
    inputs += '    ]'

    outputs = '[\n'
    for output in node['outPorts'] if 'outPorts' in node else []:
        output_class = io_class_translation.get(output['portObjectClass'])
        outputs += f"        {output_class},  # TODO: check output, original: '{output['portObjectClass']}'\n"
    outputs += '    ]'

    model_output = next((True for output in node['outPorts'] if 'model' in output['name'].lower()), False)
    model_input = next((True for input in node['inPorts'] if 'model' in input['name'].lower()), False)

    implementation = 'do.LearnerImplementation' if model_output else 'do.ApplierImplementation' if model_input else 'do.Implementation'
    factory = node['identifier']

    return name, f'''
{name} = KnimeImplementation(
    name='{node['name']}',
    algorithm=None,  # TODO: check algorithm
    parameters={params},
    input={inputs},
    output={outputs},
    implementation_type={implementation},  # TODO: check implementation type
    knime_node_factory='{factory}',
    knime_bundle=KnimeBaseBundle,
)
'''


def get_component_code(node):
    return '', ''


def build_file_from_nodes(nodes, path) -> Tuple[List[str], List[str]]:
    file_path = f'{path}.py'
    implementations = []
    components = []
    with open(file_path, 'w') as f:
        f.write('from ontology_populator.implementations.core import *\n')
        f.write('from ontology_populator.implementations.knime.knime_implementation import *\n')
        f.write('from common import *\n')

        for node in nodes['nodes']:
            implementation, implementation_code = get_implementation_code(node)
            implementations.append(implementation)
            f.write(implementation_code)
            component, component_code = get_component_code(node)
            components.append(component)
            f.write(component_code)
    return implementations, components


def create_package(path):
    os.makedirs(path.lower(), exist_ok=True)
    open(os.path.join(path.lower(), '__init__.py'), 'a').close()


def update_package_init(path, paths, implementations, components):
    with open(os.path.join(path.lower(), '__init__.py'), 'w') as f:
        for p in paths:
            p_fixed = os.path.relpath(p, path).replace('\\', '.').replace('/', '.').replace('.py', '')
            f.write(f'from .{p_fixed} import *\n')
        f.write('implementations = [\n')
        for implementation in implementations:
            for i in implementation:
                f.write(f"    '{i}',\n")
        f.write(']\n')
        f.write('components = [\n')
        for component in components:
            for c in component:
                f.write(f"    '{c}',\n")
        f.write(']\n')

    return [i for impl in implementations for i in impl], [c for comp in components for c in comp]


def build_source_files(source, current_path):
    print('Current path', current_path)
    url_name = get_name(source)
    new_path = os.path.join(current_path, url_name)
    if 'nodes' in source:
        print('\t Create file')
        implementations, components = build_file_from_nodes(source, new_path)
        return new_path, implementations, components
    if 'children' in source:
        print('\t Create package')
        create_package(new_path)
        elements = []
        for child in source['children']:
            result = build_source_files(child, new_path)
            if result is not None:
                elements.append(result)
        implementations, components = update_package_init(new_path, *zip(*elements))
        return new_path, implementations, components
    print('Skipping', source['name'])
    return None


def main():
    with open('sources/nodeDocumentation.json', encoding='utf8') as f:
        data = json.load(f)

    build_source_files(data, './test')


if __name__ == '__main__':
    main()
