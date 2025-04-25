from common import *
from .knime_implementation import KnimeBaseBundle, KnimeParameter, KnimeImplementation
from ..core import *

partitioning_implementation = KnimeImplementation(
    name='Partitioning',
    algorithm=cb.Partitioning,
    parameters=[
        KnimeParameter("Size of First Partition", XSD.string, "Relative", "method"),
        KnimeParameter("Sampling Method", XSD.string, "Random", "samplingMethod"),
        KnimeParameter("Fraction (Relative size)", XSD.double, 0.8, "fraction"),
        KnimeParameter("Count (Absolute size)", XSD.int, 100, "count"),
        KnimeParameter("Random seed", XSD.string, None, "random_seed"),
        KnimeParameter("Class columns", XSD.string, None, "class_column"),
    ],
    input=[
        cb.TabularDataset,
    ],
    output=[
        cb.TrainDataset,
        cb.TestDataset,
    ],
    knime_node_factory='org.knime.base.node.preproc.partition.PartitionNodeFactory',
    knime_bundle=KnimeBaseBundle,
)

random_relative_train_test_split_component = Component(
    name='Random Relative Train-Test Split',
    implementation=partitioning_implementation,
    overriden_parameters=[
        ("Size of First Partition", "Relative"),
        ("Sampling Method", "Random"),
    ],
    exposed_parameters=[
        "Fraction (Relative size)",
        "Random seed",
    ],
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(1, 2),
        Transformation(
            query='''
DELETE {
    $output1 dmop:numberOfRows ?rows1.
    $output2 dmop:numberOfRows ?rows1.
}
INSERT {
    $output1 dmop:numberOfRows ?newRows1 .
    $output2 dmop:numberOfRows ?newRows2 .
}
WHERE {
    $output1 dmop:numberOfRows ?rows1.
    BIND(ROUND(?rows1 * (1 - $parameter3)) AS ?newRows1)
    BIND(?rows1 - ?newRows1 AS ?newRows2)
}
''',
        ),
    ],
)

random_absolute_train_test_split_component = Component(
    name='Random Absolute Train-Test Split',
    implementation=partitioning_implementation,
    overriden_parameters=[
        ("Size of First Partition", "Absolute"),
        ("Sampling Method", "Random"),
    ],
    exposed_parameters=[
        "Count (Absolute size)",
        "Random seed",
    ],
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(1, 2),
        Transformation(
            query='''
DELETE {
    $output1 dmop:numberOfRows ?rows1.
    $output2 dmop:numberOfRows ?rows1.
}
INSERT {
    $output1 dmop:numberOfRows ?newRows1 .
    $output2 dmop:numberOfRows ?newRows2 .
}
WHERE {
    $output1 dmop:numberOfRows ?rows1.
    BIND(IF( ?rows1 - $parameter4>0, ?rows1 - $parameter4, 0 ) AS ?newRows1)
    BIND(?rows1 - ?newRows1 AS ?newRows2)
}
''',
        ),
    ],
)

top_relative_train_test_split_component = Component(
    name='Top K Relative Train-Test Split',
    implementation=partitioning_implementation,
    overriden_parameters=[
        ("Size of First Partition", "Relative"),
        ("Sampling Method", "First"),
    ],
    exposed_parameters=[
        "Fraction (Relative size)",
    ],
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(1, 2),
        Transformation(
            query='''
DELETE {
    $output1 dmop:numberOfRows ?rows1.
    $output2 dmop:numberOfRows ?rows1.
}
INSERT {
    $output1 dmop:numberOfRows ?newRows1 .
    $output2 dmop:numberOfRows ?newRows2 .
}
WHERE {
    $output1 dmop:numberOfRows ?rows1.
    BIND(ROUND(?rows1 * (1 - $parameter3)) AS ?newRows1)
    BIND(?rows1 - ?newRows1 AS ?newRows2)
}
''',
        ),
    ],
)

top_absolute_train_test_split_component = Component(
    name='Top K Absolute Train-Test Split',
    implementation=partitioning_implementation,
    overriden_parameters=[
        ("Size of First Partition", "Absolute"),
        ("Sampling Method", "First"),
    ],
    exposed_parameters=[
        "Count (Absolute size)",
    ],
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(1, 2),
        Transformation(
            query='''
DELETE {
    $output1 dmop:numberOfRows ?rows1.
    $output2 dmop:numberOfRows ?rows1.
}
INSERT {
    $output1 dmop:numberOfRows ?newRows1 .
    $output2 dmop:numberOfRows ?newRows2 .
}
WHERE {
    $output1 dmop:numberOfRows ?rows1.
    BIND(IF( ?rows1 - $parameter4>0, ?rows1 - $parameter4, 0 ) AS ?newRows1)
    BIND(?rows1 - ?newRows1 AS ?newRows2)
}
''',
        ),
    ],
)
