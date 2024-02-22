from .knime_implementation import KnimeImplementation, KnimeParameter, KnimeBaseBundle
from ..core import *
from common import *

normalizer_implementation = KnimeImplementation(
    name='Normalizer (PMML)',
    algorithm=cb.Normalization,
    parameters=[
        KnimeParameter('Normalization mode', XSD.int, None, 'mode'),
        KnimeParameter('New minimum', XSD.float, 0.0, 'newmin'),
        KnimeParameter('New maximum', XSD.float, 1.0, 'newmax'),
        KnimeParameter('Columns to normalize', RDF.List, '$$NUMERIC_COLUMNS$$', 'columns')
    ],
    input=[
        cb.TabularDataset,
    ],
    output=[
        cb.NormalizedTabularDatasetShape,
        cb.NormalizerModel,
    ],
    implementation_type=tb.LearnerImplementation,
    knime_node_factory='org.knime.base.node.preproc.pmml.normalize.NormalizerPMMLNodeFactory2',
    knime_bundle=KnimeBaseBundle,
)

min_max_scaling_component = Component(
    name='Min-Max Scaling',
    implementation=normalizer_implementation,
    overriden_parameters=[
        ('Normalization mode', 1),
    ],
    exposed_parameters=[
        'New minimum',
        'New maximum',
    ],
    transformations=[
        CopyTransformation(1, 1),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:hasMinValue $parameter2;
            dmop:hasMaxValue $parameter3;
            dmop:isNormalized true.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?column dmop:isFeature true .
}
            ''',
        ),
        Transformation(
            query='''
INSERT DATA {
    $output1 dmop:isNormalized true.
    $output2 cb:normalizationMode "MinMax";
             cb:newMin $parameter2;
             cb:newMax $parameter3.
}
            ''',
        ),
    ],
)

z_score_scaling_component = Component(
    name='Z-Score Scaling',
    implementation=normalizer_implementation,
    overriden_parameters=[
        ('Normalization mode', 2),
    ],
    transformations=[
        CopyTransformation(1, 1),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:hasMeanValue 0;
            dmop:hasStandardDeviation 1;
            dmop:isNormalized true.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?column dmop:isFeature true .
}
            ''',
        ),
        Transformation(
            query='''
INSERT DATA {
    $output1 dmop:isNormalized true.
    $output2 cb:normalizationMode "ZScore".
}
            ''',
        ),
    ],
)

decimal_scaling_component = Component(
    name='Decimal Scaling',
    implementation=normalizer_implementation,
    overriden_parameters=[
        ('Normalization mode', 3),
    ],
    transformations=[
        CopyTransformation(1, 1),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:isNormalized true.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?column dmop:isFeature true .
}
            ''',
        ),
        Transformation(
            query='''
INSERT DATA {
    $output1 dmop:isNormalized true.
    $output2 cb:normalizationMode "Decimal".
}
            ''',
        ),
    ],
)

normalizer_applier_implementation = KnimeImplementation(
    name='Normalizer Apply (PMML)',
    algorithm=cb.Normalization,
    parameters=[
    ],
    input=[
        cb.NormalizerModel,
        cb.TabularDataset,
    ],
    output=[
        cb.NormalizerModel,
        cb.NormalizedTabularDatasetShape,
    ],
    implementation_type=tb.ApplierImplementation,
    counterpart=normalizer_implementation,
    knime_node_factory='org.knime.base.node.preproc.pmml.normalize.NormalizerPMMLApplyNodeFactory',
    knime_bundle=KnimeBaseBundle,
)

normalizer_applier_component = Component(
    name='Normalizer Applier',
    implementation=normalizer_applier_implementation,
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(2, 2),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:hasMinValue $parameter2;
            dmop:hasMaxValue $parameter3;
            dmop:isNormalized true.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?column dmop:isFeature true .
    $input1 cb:normalizationMode "MinMax".
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:hasMeanValue 0;
            dmop:hasStandardDeviation 1;
            dmop:isNormalized true.
}
WHERE {
    $output2 dmop:hasColumn ?column .
    ?column dmop:isFeature true .
    $input1 cb:normalizationMode "ZScore".
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:isNormalized true.
}
WHERE {
    $output1 dmop:hasColumn ?column.
    ?column dmop:isFeature true .
    $input1 cb:normalizationMode "Decimal".
}
            ''',
        ),
    ],
    counterpart=[
        min_max_scaling_component,
        z_score_scaling_component,
        decimal_scaling_component,
    ]
)

"""
min_max_scaling_applier_component = Component(
    name='Min-Max Scaling Applier',
    implementation=normalizer_applier_implementation,
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(2, 2),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:hasMinValue ?newMin;
            dmop:hasMaxValue ?newMax;
            dmop:isNormalized true.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    $input1 cb:minValue ?newMin;
            cb:maxValue ?newMax.
    ?column dmop:isFeature true ;
}
            ''',
        ),
        Transformation(
            query='''
INSERT DATA {
    $output2 dmop:isNormalized true.
}
            ''',
        ),
    ],
    counterpart=min_max_scaling_component,
)

z_score_scaling_applier_component = Component(
    name='Z-Score Scaling Applier',
    implementation=normalizer_applier_implementation,
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(2, 2),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:hasMeanValue 0;
            dmop:hasStandardDeviation 1;
            dmop:isNormalized true.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?column dmop:isFeature true ;
}
            ''',
        ),
        Transformation(
            query='''
INSERT DATA {
    $output2 dmop:isNormalized true.
}
            ''',
        ),
    ],
    counterpart=z_score_scaling_component,
)

decimal_scaling_applier_component = Component(
    name='Decimal Scaling Applier',
    implementation=normalizer_applier_implementation,
    transformations=[
        CopyTransformation(1, 1),
        CopyTransformation(2, 2),
        Transformation(
            query='''
DELETE {
    ?column ?valueProperty ?value.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?valuePropetry rdfs:subPropertyOf dmop:ColumnValueInfo.
    ?column ?valueProperty ?value.
}
            ''',
        ),
        Transformation(
            query='''
INSERT {
    ?column dmop:isNormalized true.
}
WHERE {
    $output2 dmop:hasColumn ?column.
    ?column dmop:isFeature true ;
}
            ''',
        ),
        Transformation(
            query='''
INSERT DATA {
    $output2 dmop:isNormalized true.
}
            ''',
        ),
    ],
    counterpart=decimal_scaling_component,
)
"""
