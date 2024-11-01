from common import *
from .knime_implementation import KnimeImplementation, KnimeBaseBundle, KnimeParameter
from ..core import *

svm_learner_implementation = KnimeImplementation(
    name='SVM Learner',
    algorithm=cb.SVM,
    parameters=[
        KnimeParameter("Class column", XSD.string, "$$LABEL$$", 'classcol'),
        KnimeParameter("Overlapping Penalty", XSD.double, 1.0, 'c_parameter'),
        KnimeParameter("Bias", XSD.double, 1.0, 'kernel_param_Bias'),
        KnimeParameter("Power", XSD.double, 1.0, 'kernel_param_Power'),
        KnimeParameter("Gamma", XSD.double, 1.0, 'kernel_param_Gamma'),
        KnimeParameter("Kappa", XSD.double, 0.1, 'kernel_param_kappa'),
        KnimeParameter("Delta", XSD.double, 0.5, 'kernel_param_delta'),
        KnimeParameter("Sigma", XSD.double, 0.1, 'kernel_param_sigma'),
        KnimeParameter("Kernel type", XSD.string, None, 'kernel_type'),
    ],
    input=[
        [cb.LabeledTabularDatasetShape, cb.NormalizedTabularDatasetShape, cb.NonNullTabularDatasetShape],
    ],
    output=[
        cb.SVMModel,
    ],
    implementation_type=tb.LearnerImplementation,
    knime_node_factory='org.knime.base.node.mine.svm.learner.SVMLearnerNodeFactory2',
    knime_bundle=KnimeBaseBundle,
)

polynomial_svm_learner_component = Component(
    name='Polynomial SVM Learner',
    implementation=svm_learner_implementation,
    overriden_parameters=[
        ('Kernel type', 'Polynomial'),
    ],
    exposed_parameters=[
        'Class column',
        'Overlapping Penalty',
        'Bias',
        'Power',
        'Gamma',
    ],
    transformations=[
        Transformation(
            query='''
INSERT {
    $output1 cb:setsClassColumnName "Prediction (?label)" .
}
WHERE {
    $input1 dmop:hasColumn ?column .
    ?column dmop:isLabel true ;
            dmop:hasColumnName ?label .
}
            ''',
        ),
    ],
)

hypertangent_svm_learner_component = Component(
    name='HyperTangent SVM Learner',
    implementation=svm_learner_implementation,
    overriden_parameters=[
        ('Kernel type', 'HyperTangent'),
    ],
    exposed_parameters=[
        'Class column',
        'Overlapping Penalty',
        'Kappa',
        'Delta',
    ],
    transformations=[
        Transformation(
            query='''
INSERT DATA{
    $output1 cb:setsClassColumnName $parameter1 .
}
            ''',
        ),
    ],
)

rbf_svm_learner_component = Component(
    name='RBF SVM Learner',
    implementation=svm_learner_implementation,
    overriden_parameters=[
        ('Kernel type', 'RBF'),
    ],
    exposed_parameters=[
        'Class column',
        'Overlapping Penalty',
        'Sigma',
    ],
    transformations=[
        Transformation(
            query='''
INSERT DATA{
    $output1 cb:setsClassColumnName $parameter1 .
}
            ''',
        ),
    ],
)

svm_predictor_implementation = KnimeImplementation(
    name='SVM Predictor',
    algorithm=cb.SVM,
    parameters=[
        KnimeParameter("Prediction column name", XSD.string, "Prediction ($$LABEL$$)", 'prediction column name'),
        KnimeParameter("Change prediction", XSD.boolean, False, 'change prediction'),
        KnimeParameter("Add probabilities", XSD.boolean, False, 'add probabilities'),
        KnimeParameter("Class probability suffix", XSD.string, "", 'class probability suffix'),
    ],
    input=[
        cb.SVMModel,
        [cb.NormalizedTabularDatasetShape, cb.NonNullTabularDatasetShape]
    ],
    output=[
        cb.LabeledTabularDatasetShape,
    ],
    implementation_type=tb.ApplierImplementation,
    counterpart=svm_learner_implementation,
    knime_node_factory='org.knime.base.node.mine.svm.predictor2.SVMPredictorNodeFactory',
    knime_bundle=KnimeBaseBundle,
)

svm_predictor_component = Component(
    name='SVM Predictor',
    implementation=svm_predictor_implementation,
    transformations=[
        CopyTransformation(2, 1),
        Transformation(
            query='''
INSERT {
    $output1 dmop:hasColumn _:labelColumn .
    _:labelColumn a dmop:Column ;
        dmop:isLabel true;
      dmop:hasName $parameter1.
}
WHERE {
    $input1 cb:setsClassColumnName ?classColumnName .
}
            ''',
        ),
    ],
    counterpart=[
        polynomial_svm_learner_component,
        hypertangent_svm_learner_component,
        rbf_svm_learner_component,
    ],
)
