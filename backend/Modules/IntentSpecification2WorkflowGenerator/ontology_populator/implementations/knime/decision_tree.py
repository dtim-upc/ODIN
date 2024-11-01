from ontology_populator.implementations.core import *
from common import *
from ontology_populator.implementations.knime import KnimeImplementation, KnimeParameter, KnimeBaseBundle

decision_tree_learner_implementation = KnimeImplementation(
    name='Decision Tree Learner',
    algorithm=cb.DecisionTree,
    parameters=[
        KnimeParameter("Class column", XSD.string, '$$LABEL$$', 'classifyColumn'),
        KnimeParameter("Number of records to store for view", XSD.int, 10000, 'numverRecordsToView'),
        KnimeParameter("Min number records per node", XSD.int, 10, 'minNumberRecordsPerNode'),
        KnimeParameter("Pruning method", XSD.string, "No pruning", 'pruningMethod'),
        KnimeParameter("Reduced error pruning", XSD.boolean, True, 'enableReducedErrorPruning'),
        KnimeParameter("Quality Measure", XSD.string, "Gini index", 'splitQualityMeasure'),
        KnimeParameter("Average split point", XSD.boolean, True, 'splitAverage'),
        KnimeParameter("Number of threads", XSD.int, 1, 'numProcessors'),
        KnimeParameter("Max number of nominal values", XSD.int, 10, 'maxNumNominalValues'),
        KnimeParameter("Binary nominal splits", XSD.boolean, False, 'binaryNominalSplit'),
        KnimeParameter("Filter invalid", XSD.boolean, False, 'FilterNominalValuesFromParent'),
        KnimeParameter("Skip nominal columns without domain information", XSD.boolean, False,
                       'skipColumnsWithoutDomain'),
        KnimeParameter("No true child strategy", XSD.string, "returnNullPrediction", 'CFG_NOTRUECHILD'),
        KnimeParameter("Missing value strategy", XSD.string, "lastPrediction", 'CFG_MISSINGSTRATEGY'),
        KnimeParameter("Force root split columns", XSD.boolean, False, 'useFirstSplitColumn'),
        KnimeParameter("Root split column", XSD.string, None, 'firstSplitColumn'),
    ],
    input=[
        cb.LabeledTabularDatasetShape,
    ],
    output=[
        cb.DecisionTreeModel,
    ],
    implementation_type=tb.LearnerImplementation,
    knime_node_factory='org.knime.base.node.mine.decisiontree2.learner2.DecisionTreeLearnerNodeFactory3',
    knime_bundle=KnimeBaseBundle,
)

decision_tree_learner_component = Component(
    name='Decision Tree Learner',
    implementation=decision_tree_learner_implementation,
    transformations=[
    ],
    exposed_parameters=[
        'Class column',
        'Number of records to store for view',
        'Min number records per node',
        'Pruning method',
        'Reduced error pruning',
        'Quality Measure',
        'Average split point',
        'Number of threads',
        'Max number of nominal values',
        'Binary nominal splits',
        'Filter invalid',
        'Skip nominal columns without domain information',
        'No true child strategy',
        'Missing value strategy',
        'Force root split columns',
        'Root split column',
    ],
)

decision_tree_predictor_implementation = KnimeImplementation(
    name='Decision Tree Predictor',
    algorithm=cb.DecisionTree,
    parameters=[
        KnimeParameter("Use Gain Ratio", XSD.int, 20000, "UseGainRatio"),
        KnimeParameter("Show distribution", XSD.boolean, True, "ShowDistribution"),
        KnimeParameter("Prediction column name", XSD.string, "Prediction ($$LABEL$$)", "prediction column name"),
        KnimeParameter("Change Prediction", XSD.boolean, False, "change prediction"),
        KnimeParameter("Class Probability Suffix", XSD.string, "", "class probability suffix"),
    ],
    input=[
        cb.DecisionTreeModel,
        cb.TabularDataset,
    ],
    output=[
        cb.LabeledTabularDatasetShape,
    ],
    implementation_type=tb.ApplierImplementation,
    counterpart=decision_tree_learner_implementation,
    knime_node_factory='org.knime.base.node.mine.decisiontree2.predictor2.DecTreePredictorNodeFactory',
    knime_bundle=KnimeBaseBundle,
)

decision_tree_predictor_component = Component(
    name='Decision Tree Predictor',
    implementation=decision_tree_predictor_implementation,

    transformations=[
        CopyTransformation(2, 1),
        Transformation(
            query='''
INSERT DATA {
    $output1 dmop:hasColumn _:labelColumn.
    _:labelColumn a dmop:Column;
                  dmop:isLabel true;
                  dmop:hasName $parameter3.
}
            ''',
        ),
    ],
    counterpart=decision_tree_learner_component,
)
