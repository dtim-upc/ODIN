from common import *
from .knime_implementation import KnimeImplementation, KnimeBaseBundle, KnimeParameter
from ..core import *

nn_learner_implementation = KnimeImplementation(
    name='NN Learner',
    algorithm=cb.NN,
    parameters=[
        KnimeParameter("Class column", XSD.string, "$$LABEL$$", 'classcol'),
        KnimeParameter("NN type", XSD.string, None, 'nn_type'),
    ],
    input=[
        [cb.LabeledTabularDatasetShape, cb.NormalizedTabularDatasetShape, cb.NonNullTabularDatasetShape],
    ],
    output=[
        cb.NNModel,
    ],
    implementation_type=tb.LearnerImplementation,
    knime_node_factory='org.knime.base.node.mine.svm.predictor2.SVMPredictorNodeFactory',
    knime_bundle=KnimeBaseBundle,
)

feedforward_learner_component = Component(
    name='FeedForward NN Learner',
    implementation=nn_learner_implementation,
    overriden_parameters=[
        ('NN type', 'FeedForward'),
    ],
    exposed_parameters=[
        'Class column'
    ],
    transformations=[
    ],
)

recurrent_learner_component = Component(
    name='Recurrent NN Learner',
    implementation=nn_learner_implementation,
    overriden_parameters=[
        ('NN type', 'Recurrent'),
    ],
    exposed_parameters=[
        'Class column'
    ],
    transformations=[
    ],
)

convolutional_learner_component = Component(
    name='Convolutional NN Learner',
    implementation=nn_learner_implementation,
    overriden_parameters=[
        ('NN type', 'Convolutional'),
    ],
    exposed_parameters=[
        'Class column'
    ],
    transformations=[
    ],
)

lstm_learner_component = Component(
    name='LSTM NN Learner',
    implementation=nn_learner_implementation,
    overriden_parameters=[
        ('NN type', 'LSTM'),
    ],
    exposed_parameters=[
        'Class column'
    ],
    transformations=[
    ],
)

nn_predictor_implementation = KnimeImplementation(
    name='NN Predictor',
    algorithm=cb.NN,
    parameters=[
        KnimeParameter("Prediction column name", XSD.string, "Prediction ($$LABEL$$)", 'prediction column name'),
        KnimeParameter("Change prediction", XSD.boolean, False, 'change prediction'),
        KnimeParameter("Add probabilities", XSD.boolean, False, 'add probabilities'),
        KnimeParameter("Class probability suffix", XSD.string, "", 'class probability suffix'),
    ],
    input=[
        cb.NNModel,
        [cb.NormalizedTabularDatasetShape, cb.NonNullTabularDatasetShape]
    ],
    output=[
        cb.LabeledTabularDatasetShape,
    ],
    implementation_type=tb.ApplierImplementation,
    counterpart=nn_learner_implementation,
    knime_node_factory='org.knime.base.node.mine.svm.predictor2.SVMPredictorNodeFactory',
    knime_bundle=KnimeBaseBundle,
)

nn_predictor_component = Component(
    name='NN Predictor',
    implementation=nn_predictor_implementation,
    transformations=[

    ],
    counterpart=[
        feedforward_learner_component,
        recurrent_learner_component,
        convolutional_learner_component,
        lstm_learner_component
    ],
)
