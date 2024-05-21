import csv
import os
import sys
from os import path

import pandas as pd

sys.path.append(os.path.abspath('..'))

from common import *


def add_dataset_info(dataset_path, graph, label):
    dataset_node = ab.term(path.basename(dataset_path))
    graph.add((dataset_node, RDF.type, dmop.TabularDataset))
<<<<<<< HEAD
    dataset = pd.read_csv(dataset_path, encoding='latin', delimiter=";")
=======
    dataset = pd.read_csv(dataset_path, on_bad_lines='skip')
>>>>>>> 218cd58 (Updating the ontology along with creating the new pipeline generation algorithm)
    add_csv_info(dataset_path, dataset, dataset_node, graph)
    add_column_info(dataset_path, dataset, dataset_node, graph, label)


def add_csv_info(dataset_path, dataset, dataset_node, graph):
    print('\tAdding CSV info ... ', end='')
    csvfile = open(dataset_path, 'r')
    encoding = csvfile.encoding
    lines = [csvfile.readline() for _ in range(50)]
    csvfile.close()
    dialect = csv.Sniffer().sniff(''.join(lines))
    headers = csv.Sniffer().has_header(''.join(lines))

    num_rows = len(dataset.index)
    num_cols = len(dataset.columns)

    graph.add((dataset_node, dmop.fileFormat, Literal('csv')))
    graph.add((dataset_node, dmop.delimiter, Literal(dialect.delimiter)))
    graph.add((dataset_node, dmop.doubleQuote, Literal(dialect.doublequote)))
    graph.add((dataset_node, dmop.encoding, Literal(encoding)))
    graph.add((dataset_node, dmop.hasHeader, Literal(headers)))
    graph.add((dataset_node, dmop.lineDelimiter, Literal(dialect.lineterminator)))
    graph.add((dataset_node, dmop.numberOfRows, Literal(num_rows)))
    graph.add((dataset_node, dmop.numberOfColumns, Literal(num_cols)))
    graph.add((dataset_node, dmop.path, Literal(path.abspath(dataset_path))))
    graph.add((dataset_node, dmop.quoteChar, Literal(dialect.quotechar)))
    graph.add((dataset_node, dmop.skipInitialSpace, Literal(dialect.skipinitialspace)))

    print('Done!')


def get_column_type(column_type, column):
    if column_type == 'int64':
        return dmop.Integer
    elif column_type == 'float64':
        return dmop.Float
    elif column_type == 'object':
        if column.str.isnumeric().all():
            return dmop.Integer
        else:
            return dmop.String
    else:
        return dmop.String


def has_nulls(column):
    return bool(column.isnull().values.any() or column.isna().values.any())


def is_categorical(column_type, column):
    if column_type != 'object' and column_type != 'int64':
        return False
    else:
        return column.nunique() < column.size / 3


def is_unique(column_type, column):
    if column_type != 'object' and column_type != 'int64':
        return False
    else:
        return column.nunique() == column.size


def add_column_info(dataset_path, dataset, dataset_node, graph, label):
    print('\tAdding column info:')
    for col in dataset.columns:
        col_type = dataset[col].dtype.name
        col_node = ab.term(f'{path.basename(dataset_path)}/{col}')
        graph.add((dataset_node, dmop.hasColumn, col_node))
        graph.add((col_node, RDF.type, dmop.Column))
        graph.add((col_node, dmop.hasColumnName, Literal(col)))

        column_type = get_column_type(col_type, dataset[col])
        categorical = is_categorical(col_type, dataset[col])
        unique = is_unique(col_type, dataset[col])
        nulls = has_nulls(dataset[col])
        position = dataset.columns.get_loc(col)

        graph.add((col_node, dmop.hasDataPrimitiveTypeColumn, column_type))
        graph.add((col_node, dmop.isCategorical, Literal(categorical)))
        graph.add((col_node, dmop.isUnique, Literal(unique)))
        graph.add((col_node, dmop.containsNulls, Literal(nulls)))
        if label != "" and col == label:  # Detect label attribute (if indicated) and annotate it
            graph.add((col_node, dmop.isFeature, Literal(False)))
            graph.add((col_node, dmop.isLabel, Literal(True)))
        else:
            graph.add((col_node, dmop.isFeature, Literal(True)))
            graph.add((col_node, dmop.isLabel, Literal(False)))
        graph.add((col_node, dmop.hasPosition, Literal(position)))
        print(f'\t\t{col}: {column_type} - {categorical=} - {unique=} - {position=} - {nulls=}')


def read_graph(urls):
    ontology = Graph()
    for url in urls:
        ontology.parse(url)
    return ontology


def annotate_dataset(source_path, output_path, label=""):
    print(f'Annotating {source_path}')

    dataset_graph = get_graph_xp()

    add_dataset_info(source_path, dataset_graph, label)
    dataset_graph.serialize(destination=output_path)


def main():
    for file in os.listdir('./datasets'):
        if file.endswith('.csv'):
            annotate_dataset(f'./datasets/{file}', f'./annotated_datasets/{file[:-4]}_annotated.ttl')


if __name__ == '__main__':
    main()
