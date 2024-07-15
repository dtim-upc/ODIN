import sys

from rdflib.collection import Collection

from common import *
from implementations.knime import implementations, components


def init_cbox() -> Graph:
    cbox = get_graph_xp()

    cbox.add((URIRef(str(cb)), RDF.type, OWL.Ontology))
    cbox.add((URIRef(str(cb)), RDFS.label, Literal("ExtremeXP Ontology CBox")))

    return cbox


def add_problems(cbox):
    problems = [
        cb.Description,
        cb.Explanation,
        cb.Prediction,
        cb.DataCleaning,
        cb.DataManagement,

        cb.Classification,
        cb.Clustering,
        cb.AnomalyDetection,

        cb.MissingValueManagement,
        cb.DuplicationRemoval,
        cb.Normalization,
    ]
    subproblems = [
        (cb.Description, [cb.Classification, cb.Clustering, cb.AnomalyDetection],),
        (cb.DataCleaning, [cb.MissingValueManagement, cb.DuplicationRemoval, cb.Normalization],),
    ]

    for p in problems:
        cbox.add((p, RDF.type, tb.Problem))

    for p, sps in subproblems:
        for sp in sps:
            cbox.add((sp, tb.subProblemOf, p))


def add_algorithms(cbox):
    algorithms = [
        # Clustering
        (cb.KMeans, cb.Clustering),
        (cb.DBSCAN, cb.Clustering),
        (cb.HierarchicalClustering, cb.Clustering),

        # Classification
        (cb.DecisionTree, cb.Classification),
        (cb.RandomForest, cb.Classification),
        (cb.NaiveBayes, cb.Classification),
        (cb.SVM, cb.Classification),
        (cb.KNN, cb.Classification),
        (cb.NN, cb.Classification),

        # Anomaly Detection
        (cb.OneClassSVM, cb.AnomalyDetection),
        (cb.IsolationForest, cb.AnomalyDetection),
        (cb.LocalOutlierFactor, cb.AnomalyDetection),

        # Missing Value Management
        (cb.MeanImputation, cb.MissingValueManagement),
        (cb.MedianImputation, cb.MissingValueManagement),
        (cb.ModeImputation, cb.MissingValueManagement),
        (cb.KNNImputation, cb.MissingValueManagement),
        (cb.MissingValueRemoval, cb.MissingValueManagement),

        # Duplication Removal
        (cb.DuplicateRemoval, cb.DuplicationRemoval),

        # Normalization
        (cb.MinMaxScaling, cb.Normalization),
        (cb.ZScoreScaling, cb.Normalization),
        (cb.RobustNormalization, cb.Normalization),

        # Data Management
        (cb.TrainTestSplit, cb.DataManagement),
        (cb.LabelExtraction, cb.DataManagement),
    ]

    for algorithm, problem in algorithms:
        cbox.add((algorithm, RDF.type, tb.Algorithm))
        cbox.add((algorithm, tb.solves, problem))


def add_implementations(cbox):
    for implementation in implementations:
        print(f'Adding implementation {implementation.name}')
        implementation.add_to_graph(cbox)

    for implementation in implementations:
        implementation.add_counterpart_relationship(cbox)

    for component in components:
        print(f'Adding component {component.name}')
        component.add_to_graph(cbox)

    for component in components:
        component.add_counterpart_relationship(cbox)


def add_models(cbox):
    models = [
        'SVMModel',
        'DecisionTreeModel',
        'NormalizerModel',
        'MissingValueModel',
        'NNModel',
    ]

    cbox.add((cb.Model, RDFS.subClassOf, tb.Data))
    for model in models:
        cbox.add((cb.term(model), RDFS.subClassOf, cb.Model))

        cbox.add((cb.term(model + 'Shape'), RDF.type, SH.NodeShape))
        cbox.add((cb.term(model + 'Shape'), RDF.type, tb.DataTag))
        cbox.add((cb.term(model + 'Shape'), SH.targetClass, cb.term(model)))


def add_shapes(cbox):
    # NonNullNumericFeatureColumnShape
    column_shape = cb.NonNullNumericFeatureColumnShape
    # column_shape = BNode()
    cbox.add((column_shape, RDF.type, SH.NodeShape))

    numeric_column_property = cb.NumericColumnProperty
    # numeric_column_property = BNode()
    cbox.add((numeric_column_property, SH.path, dmop.hasDataPrimitiveTypeColumn))
    cbox.add((numeric_column_property, SH['in'],
              Collection(cbox, BNode(), seq=[dmop.Integer, dmop.Float]).uri))

    non_null_column_property = cb.NonNullColumnProperty
    # non_null_column_property = BNode()
    cbox.add((non_null_column_property, SH.path, dmop.containsNulls))
    cbox.add((non_null_column_property, SH.datatype, XSD.boolean))
    cbox.add((non_null_column_property, SH.hasValue, Literal(False)))

    feature_column_property = cb.FeatureColumnProperty
    # feature_column_property = BNode()
    cbox.add((feature_column_property, SH.path, dmop.isFeature))
    cbox.add((feature_column_property, SH.datatype, XSD.boolean))
    cbox.add((feature_column_property, SH.hasValue, Literal(True)))

    feature_column = cb.FeatureColumnShape
    # feature_column = BNode()
    cbox.add((feature_column, RDF.type, SH.NodeShape))
    cbox.add((feature_column, SH.targetClass, dmop.Column))
    cbox.add((feature_column, SH.property, feature_column_property))

    cbox.add((column_shape, SH.property, numeric_column_property))
    cbox.add((column_shape, SH.property, non_null_column_property))
    cbox.add((column_shape, SH.targetClass, feature_column))

    # NonNullNumericFeatureTabularDatasetShape
    non_null_numeric_tabular_dataset_shape = cb.NonNullNumericFeatureTabularDatasetShape
    cbox.add((non_null_numeric_tabular_dataset_shape, RDF.type, SH.NodeShape))
    cbox.add((non_null_numeric_tabular_dataset_shape, SH.targetClass, dmop.TabularDataset))

    bnode = BNode()
    cbox.add((bnode, SH.path, dmop.hasColumn))
    cbox.add((bnode, SH.node, column_shape))

    cbox.add((non_null_numeric_tabular_dataset_shape, SH.property, bnode))

    # LabeledTabularDatasetShape

    label_column_property = cb.LabelColumnProperty
    cbox.add((label_column_property, SH.path, dmop.isLabel))
    cbox.add((label_column_property, SH.datatype, XSD.boolean))
    cbox.add((label_column_property, SH.hasValue, Literal(True)))

    label_column_shape = cb.LabelColumnShape
    cbox.add((label_column_shape, RDF.type, SH.NodeShape))
    cbox.add((label_column_shape, SH.targetClass, dmop.Column))
    cbox.add((label_column_shape, SH.property, label_column_property))

    labeled_dataset_shape = cb.LabeledTabularDatasetShape
    cbox.add((labeled_dataset_shape, RDF.type, SH.NodeShape))
    cbox.add((labeled_dataset_shape, SH.targetClass, dmop.TabularDataset))

    bnode_qualified = BNode()
    cbox.add((bnode_qualified, SH.path, dmop.isLabel))
    cbox.add((bnode_qualified, SH.hasValue, Literal(True)))

    bnode_column = BNode()
    cbox.add((bnode_column, SH.path, dmop.hasColumn))
    cbox.add((bnode_column, SH.qualifiedValueShape, bnode_qualified))
    cbox.add((bnode_column, SH.qualifiedMinCount, Literal(1)))
    cbox.add((bnode_column, SH.minCount, Literal(1)))

    cbox.add((labeled_dataset_shape, SH.property, bnode_column))

    non_null_column_shape = cb.NonNullColumnShape
    cbox.add((non_null_column_shape, RDF.type, SH.NodeShape))
    cbox.add((non_null_column_shape, SH.targetClass, dmop.Column))
    cbox.add((non_null_column_shape, SH.property, non_null_column_property))

    bnode = BNode()
    cbox.add((bnode, SH.path, dmop.hasColumn))
    cbox.add((bnode, SH.node, non_null_column_shape))

    non_null_tabular_dataset_shape = cb.NonNullTabularDatasetShape
    cbox.add((non_null_tabular_dataset_shape, RDF.type, SH.NodeShape))
    cbox.add((non_null_tabular_dataset_shape, SH.targetClass, dmop.TabularDataset))
    cbox.add((non_null_tabular_dataset_shape, SH.property, bnode))

    cbox.add((cb.TabularDataset, RDF.type, SH.NodeShape))
    cbox.add((cb.TabularDataset, RDF.type, tb.DataTag))
    cbox.add((cb.TabularDataset, SH.targetClass, dmop.TabularDataset))

    numeric_column_shape = cb.NumericColumnShape
    cbox.add((numeric_column_shape, RDF.type, SH.NodeShape))
    cbox.add((numeric_column_shape, SH.targetClass, dmop.Column))
    cbox.add((numeric_column_shape, SH.property, numeric_column_property))

    bnode = BNode()
    cbox.add((bnode, SH.path, dmop.hasColumn))
    cbox.add((bnode, SH.node, numeric_column_shape))

    numeric_tabular_dataset_shape = cb.NumericTabularDatasetShape
    cbox.add((numeric_tabular_dataset_shape, RDF.type, SH.NodeShape))
    cbox.add((numeric_tabular_dataset_shape, SH.targetClass, dmop.TabularDataset))
    cbox.add((numeric_tabular_dataset_shape, SH.property, bnode))

    cbox.add((cb.isNormalizedConstraint, RDF.type, SH.PropertyConstraintComponent))
    cbox.add((cb.isNormalizedConstraint, SH.path, dmop.isNormalized))
    cbox.add((cb.isNormalizedConstraint, SH.datatype, XSD.boolean))
    cbox.add((cb.isNormalizedConstraint, SH.hasValue, Literal(True)))

    cbox.add((cb.NormalizedTabularDatasetShape, RDF.type, SH.NodeShape))
    cbox.add((cb.NormalizedTabularDatasetShape, RDF.type, tb.DataTag))
    cbox.add((cb.NormalizedTabularDatasetShape, SH.property, cb.isNormalizedConstraint))
    cbox.add((cb.NormalizedTabularDatasetShape, SH.targetClass, dmop.TabularDataset))


def main(dest='../ontologies/cbox.ttl'):
    cbox = init_cbox()
    add_problems(cbox)
    add_algorithms(cbox)
    add_implementations(cbox)
    add_models(cbox)
    add_shapes(cbox)

    cbox.serialize(dest, format='turtle')


if __name__ == '__main__':
    if len(sys.argv) > 1:
        main(sys.argv[1])
    else:
        main()
