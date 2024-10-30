from sklearn.model_selection import train_test_split
from hpsklearn import *
from sklearn.metrics import accuracy_score, confusion_matrix, mean_absolute_error
from hyperopt import tpe
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os
from graphviz import Digraph
from datetime import datetime
import sys
import shutil
from tpot import TPOTClassifier, TPOTRegressor
import nbformat as nbf
import json
from sklearn.metrics import fbeta_score
import numpy as np
import pandas as pd

## modified f1_score from sklearn.metrics
def f1_score(
    y_true,
    y_pred,
    *,
    labels=None,
    pos_label=1,
    average="micro", ## change
    sample_weight=None,
    zero_division="warn",
):
    return fbeta_score(
        y_true,
        y_pred,
        beta=1,
        labels=labels,
        pos_label=pos_label,
        average=average,
        sample_weight=sample_weight,
        zero_division=zero_division,
    )
def save_results_to_json(filename, dataset_name, intent, algorithm, hyperparameter_constraints,
                         preprocessing_constraint, time, metric_name, metric_value, pipeline):
    
    os.makedirs('results/hyperopt-results/pipelines', exist_ok=True)

    json_data = {
        'dataset': dataset_name,
        'intent': intent.capitalize(),
        'algorithm_constraint': algorithm,
        'hyperparam_constraints': hyperparameter_constraints,
        'preprocessor_constraint': preprocessing_constraint,
        'time': 100, # Default value
        'max_time': time,  
        'pipeline': pipeline,
        'metricName': metric_name,
        'metric_value': metric_value
    }

    with open(filename, 'w') as json_file:
        json.dump(json_data, json_file, indent=4)

    return json_data


## hyperopt_preprocessing_class_mapping, hyperopt_algorithm_class_mapping and hyperopt_metric_class_mapping should be futher expanded based on the Knowlegde Base

hyperopt_preprocessing_class_mapping = {
    'Binarizer': binarizer("my_pre"),
    'KBinsDiscretizer': k_bins_discretizer("my_pre"),
    'MaxAbsScaler': max_abs_scaler("my_pre"),
    'Normalizer': normalizer("my_pre"),
    'MinMaxScaler': min_max_scaler("my_pre"),
    'OneHotEncoder': one_hot_encoder("my_pre"),
    'OrdinalEncoder': ordinal_encoder("my_pre"),
    'PolynomialFeatures': polynomial_features("my_pre"),
    'PowerTransformer': power_transformer("my_pre"),
    'QuantileTransformer': quantile_transformer("my_pre"),
    'RobustScaler': robust_scaler("my_pre"),
    'SplineTransformer': spline_transformer("my_pre"),
    'StandardScaler': standard_scaler("my_pre"),
}

hyperopt_algorithm_class_mapping={
    # classification:
    "RandomForestClassifier" : random_forest_classifier("my-alg"),
    "ExtraTreesClassifier":extra_trees_classifier("my-alg"),
    "BaggingClassifier":bagging_classifier("my-alg"),
    "AdaBoostClassifier":ada_boost_classifier("my-alg"),
    "GradientBoostingClassifier":gradient_boosting_classifier("my-alg"),
    "HistGradientBoostingClassifier":hist_gradient_boosting_classifier("my-alg"),
    "BernoulliNB":bernoulli_nb("my-alg"),
    "CategoricalNB":categorical_nb("my-alg"),
    "ComplementNB":complement_nb("my-alg"),
    "GaussianNB":gaussian_nb("my-alg"),
    "MultinomialNB":multinomial_nb("my-alg"),
    "SGDClassifier":sgd_classifier("my-alg"),
    "SGDOneClassSVM":sgd_one_class_svm("my-alg"),
    "RidgeClassifier":ridge_classifier("my-alg"),
    "RidgeClassifierCV":ridge_classifier_cv("my-alg"),
    "PassiveAggressiveClassifier":passive_aggressive_classifier("my-alg"),
    "Perceptron":perceptron("my-alg"),
    "DummyClassifier":dummy_classifier("my-alg"),
    "GaussianProcessClassifier":gaussian_process_classifier("my-alg"),
    "MLPClassifier":mlp_classifier("my-alg"),
    "LinearSVC":linear_svc("my-alg"),
    "NuSVC":nu_svc("my-alg"),
    "SVC":svc("my-alg"),
    "DecisionTreeClassifier":decision_tree_classifier("my-alg"),
    "ExtraTreesClassifier":extra_tree_classifier("my-alg"),
    "LabelPropagation":label_propagation("my-alg"),
    "LabelSpreading":label_spreading("my-alg"),
    "EllipticEnvelope":elliptic_envelope("my-alg"),
    "LinearDiscriminantAnalysis":linear_discriminant_analysis("my-alg"),
    "QuadraticDiscriminantAnalysis":quadratic_discriminant_analysis("my-alg"),
    "BayesianGaussianMixture":bayesian_gaussian_mixture("my-alg"),
    "GaussianMixture":gaussian_mixture("my-alg"),
    "KNeighborsClassifier":k_neighbors_classifier("my-alg"),
    "RadiusNeighborsClassifier":radius_neighbors_classifier("my-alg"),
    "NearestCentroid":nearest_centroid("my-alg"),
    # regression:
    "RandomForestRegressor":random_forest_regressor("my-alg"),
    "ExtraTreesRegressor":extra_trees_regressor("my-alg"),
    "BaggingRegressor":bagging_regressor("my-alg"),
    "IsolationForest":isolation_forest("my-alg"),
    "AdaBoostRegressor":ada_boost_regressor("my-alg"),
    "GradientBoostingRegressor":gradient_boosting_regressor("my-alg"),
    "HistGradientBoostingRegressor":hist_gradient_boosting_regressor("my-alg"),
    "LinearRegression":linear_regression("my-alg"),
    "BayesianRidge":bayesian_ridge("my-alg"),
    "ARDRegression":ard_regression("my-alg"),
    "Lars":lars("my-alg"),
    "LassoLars":lasso_lars("my-alg"),
    "LarsCV":lars_cv("my-alg"),
    "LassoLarsCV":lasso_lars_cv("my-alg"),
    "LassoLarsIC":lasso_lars_ic("my-alg"),
    "Lasso":lasso("my-alg"),
    "ElasticNet":elastic_net("my-alg"),
    "LassoCV":lasso_cv("my-alg"),
    "ElasticNetCV":elastic_net_cv("my-alg"),
    "MultiTaskLasso":multi_task_lasso("my-alg"),
    "MultiTaskElasticNet":multi_task_elastic_net("my-alg"),
    "MultiTaskLassoCV":multi_task_lasso_cv("my-alg"),
    "MultiTaskElasticNetCV":multi_task_elastic_net_cv("my-alg"),
    "PoissonRegressor":poisson_regressor("my-alg"),
    "GammaRegressor":gamma_regressor("my-alg"),
    "TweedieRegressor":tweedie_regressor("my-alg"),
    "HuberRegressor":huber_regressor("my-alg"),
    "SGDRegressor":sgd_regressor("my-alg"),
    "Ridge":ridge("my-alg"),
    "RidgeCV":ridge_cv("my-alg"),
    "LogisticRegression":logistic_regression("my-alg"),
    "LogisticRegressionCV":logistic_regression_cv("my-alg"),
    "OrthogonalMatchingPursuit":orthogonal_matching_pursuit("my-alg"),
    "OrthogonalMatchingPursuitCV":orthogonal_matching_pursuit_cv("my-alg"),
    "PassiveAggressiveRegressor":passive_aggressive_regressor("my-alg"),
    "QuantileRegressor":quantile_regression("my-alg"),
    "RANSACRegressor":ransac_regression("my-alg"),
    "TheilSenRegressor":theil_sen_regressor("my-alg"),
    "DummyRegressor":dummy_regressor("my-alg"),
    "GaussianProcessRegressor":gaussian_process_regressor("my-alg"),
    "MLPRegressor":mlp_regressor("my-alg"),
    "CCA":cca("my-alg"),
    "PLSCanonical":pls_canonical("my-alg"),
    "PLSRegression":pls_regression("my-alg"),
    "NuSVR":nu_svr("my-alg"),
    "OneClassSVM":one_class_svm("my-alg"),
    "SVR":svr("my-alg"),
    "DecisionTreeRegressor":decision_tree_regressor("my-alg"),
    "ExtraTreesRegressor":extra_tree_regressor("my-alg"),
    "TransformedTargetRegressor":transformed_target_regressor("my-alg"),
    "KernelRidge":hp_sklearn_kernel_ridge("my-alg"),
    "BayesianGaussianMixture":bayesian_gaussian_mixture("my-alg"),
    "GaussianMixture":gaussian_mixture("my-alg"),
    "KNeighborsRegressor":k_neighbors_regressor("my-alg"),
    "RadiusNeighborsRegressor":radius_neighbors_regressor("my-alg"),
    "KMeans":k_means("my-alg"),
    "MiniBatchKMeans":mini_batch_k_means("my-alg")
}

hyperopt_metric_class_mapping = {
    'Accuracy': accuracy_score,
    'F1': f1_score
}


def hyperopt_map_preprocessing_algorithm(preprocessingAlg):
    """
    Maps a preprocessing algorithm name (after removing 'sklearn-' prefix) to its corresponding class from the hyperopt preprocessing mapping.
    """
    preprocessingAlg = preprocessingAlg.removeprefix("sklearn-")
    if preprocessingAlg in hyperopt_preprocessing_class_mapping:
        return hyperopt_preprocessing_class_mapping[preprocessingAlg]
    return None

def hyperopt_map_algorithm(algorithm):
    """
    Maps an algorithm name (after removing 'sklearn-' prefix) to its corresponding class from the hyperopt algorithm mapping.
    """
    algorithm = algorithm.removeprefix("sklearn-")
    if algorithm in hyperopt_algorithm_class_mapping:
        return hyperopt_algorithm_class_mapping[algorithm]
    return None

def hyperopt_map_loss_fn(metric):
    """
    Maps a metric name to its corresponding class from the hyperopt metric mapping.
    """
    if metric in hyperopt_metric_class_mapping:
        return hyperopt_metric_class_mapping[metric]
    return None


def save_plot_as_image(filename):
    """
    Saves the current plot to a file with the specified filename and then closes the plot.
    """
    plt.savefig(filename)
    plt.close()

def hyperopt_pipeline_generator(restrictions):
    """
    Generates and evaluates a Hyperopt ML pipeline based on given constraints, saves the results and visualizations, and returns file paths for the results.
    """
    data_file_path = restrictions.get('dataset')
    intent = restrictions.get('intent')
    metric = restrictions.get('metric')
    preprocessing = restrictions.get('preprocessing')
    hyperparameter = restrictions.get('hyperparameter')
    hyperparameterValue = restrictions.get('hyperparameterValue')
    algorithm = restrictions.get('algorithm')
    preprocessingAlg = restrictions.get('preprocessingAlgorithm')
    time = restrictions.get('timeLimit')

    visualisation = ''

    df = pd.read_csv(data_file_path)
    X = df.iloc[:, :-1]
    y = df.iloc[:, -1]
    X_train, X_test, y_train, y_test = train_test_split(X, y, train_size=0.75, test_size=0.25, random_state=34)
    dataset_name = os.path.basename(data_file_path).split('.')[0]

    # preprocessing
    preprocessing_steps = []
    if preprocessing:
        mapped_algorithm = hyperopt_map_preprocessing_algorithm(preprocessingAlg)

        if mapped_algorithm!=None:
            preprocessing_steps = [mapped_algorithm]
        else:
            preprocessing_steps = any_preprocessing("my_pre")
    else:
        preprocessing_steps = []  # No preprocessing

    #timeLimit
    if time and time!='':
        timeLimit = float(time)
    else:
        timeLimit = 300 # default value

    
    if intent == 'classification':
         # algorithm
        alg = []
        if algorithm and algorithm!='':
            mapped_algorithm = hyperopt_map_algorithm(algorithm)
            # print(mapped_algorithm)
            if mapped_algorithm!=None:
                alg = mapped_algorithm
            else:
                alg = any_classifier("my_alg")
        else:
            alg = any_classifier("my_alg")   

      
        loss_fn = hyperopt_map_loss_fn(metric)
        if loss_fn==None:
            loss_fn = accuracy_score

        estim = HyperoptEstimator(
            preprocessing=preprocessing_steps,
            classifier=alg,
            loss_fn=loss_fn,
            algo=tpe.suggest,
            trial_timeout = timeLimit,
            max_evals=5
        )
        
        estim.fit(X_train, y_train)
        if not metric or metric=='':
            metric_name = "accuracy"
        else:
            metric_name=metric.lower()

    elif intent == 'regression':
        # algorithm
        alg = []
        if algorithm and algorithm!='':
            mapped_algorithm = hyperopt_map_algorithm(algorithm)

            if mapped_algorithm!=None:
                alg = mapped_algorithm
            else:
                alg = any_regressor("my_alg")
        else:
            alg = any_regressor("my_alg")   


        estim = HyperoptEstimator(preprocessing=preprocessing_steps,
                                regressor=alg,
                                loss_fn=mean_absolute_error, # default setting for regression
                                algo=tpe.suggest,
                                max_evals=5,
                                trial_timeout=timeLimit, verbose=False)
        estim.fit(X_train, y_train)
        
        metric_name = "mae" # default setting for regression

    else:
        print('Intent must be classification or regression')
        return
    
    metric_value = estim.score(X_test, y_test)
    y_pred = estim.predict(X_test)
    pipeline = estim.best_model()

    json_filename = f"results/hyperopt-results/pipelines/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-result.json"
    results_json = save_results_to_json(
        filename=json_filename,
        dataset_name=dataset_name,
        intent=intent,
        algorithm=str(pipeline['learner']).split('(')[0],
        hyperparameter_constraints=hyperparameter or {},
        preprocessing_constraint=str(preprocessingAlg).split("-")[1] if preprocessing else 'None',
        time=timeLimit,
        metric_name=metric_name,
        metric_value=metric_value,
        pipeline={
            'preprocs': str(preprocessing_steps[0]).split('(')[0].split('\n')[0].split(" ")[1].split("_")[1] + "()" if preprocessing_steps else [],
            'learner': str(pipeline['learner']).split('(')[0] + "()"
        }
    )

    if intent == 'classification':
        visualisation = 'Confusion Matrix'
        cm = confusion_matrix(y_test, y_pred)
        sns.heatmap(cm, annot=True, cmap='Blues', fmt='d', cbar=False)
        plt.xlabel('Predicted')
        plt.ylabel('True')
        plt.title('Confusion Matrix')
        if os.path.exists('results/hyperopt-results/images'):
            shutil.rmtree('results/hyperopt-results/images')
        os.makedirs('results/hyperopt-results/images')
        img_filename = f"results/hyperopt-results/images/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-conf_matrix.png"
        save_plot_as_image(img_filename)

    elif intent == 'regression':
        visualisation = 'Scatter Plot'
        plt.figure(figsize=(8, 6))
        plt.scatter(y_test, y_pred, color='blue', alpha=0.5)
        plt.plot([y_test.min(), y_test.max()], [y_test.min(), y_test.max()], 'k--', lw=2)
        plt.xlabel('Actual')
        plt.ylabel('Predicted')
        plt.title('Actual vs. Predicted Values')
        if os.path.exists('results/hyperopt-results/images'):
            shutil.rmtree('results/hyperopt-results/images')
        os.makedirs('results/hyperopt-results/images')
        img_filename = f"results/hyperopt-results/images/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-scatter_plot.png"
        save_plot_as_image(img_filename)

    if not os.path.exists('results/hyperopt-results/dataflows'):
        os.makedirs('results/hyperopt-results/dataflows')

    graph_filename = f"results/hyperopt-results/dataflows/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-dataflow"
    graph = Digraph('DataFlow', filename=graph_filename)
    graph.attr(rankdir='LR')

    graph.node('Dataset', fillcolor='orange', label=f'Dataset:\n{dataset_name}.csv')
    graph.node('Visualization', fillcolor='lightgreen', label=f'Visualization:\n{visualisation}')
    algo = pipeline['learner']
    algo_name = str(algo).split('(')[0]
    graph.node('Algorithm', fillcolor='lightblue', label=f'Algorithm:\n{algo_name}')

    if len(pipeline['preprocs']) != 0:
        prepro = pipeline['preprocs'][0]
        prepro_name = str(prepro).split('(')[0]
        graph.node('Preprocessing', fillcolor='lightblue', label=f'Preprocessing:\n{prepro_name}')

    if len(pipeline['preprocs']) != 0:
        path = 'utils/template-4-dataflow.svg'
        if os.path.exists(path):
            graph_path = graph_filename + '.svg'
            with open(path, "rt") as change:
                data = change.read()
                data = data.replace('dataset_name.csv', dataset_name + '.csv')
                data = data.replace('methodX', prepro_name)
                data = data.replace('Scatter Plot/Confusion Matrix', visualisation)
                data = data.replace('Classifier/Regressor', algo_name)
            with open(graph_path, "wt") as change:
                change.write(data)
        else:
            print(f"The path '{path}' does not exist.")
        graph.edge('Dataset', 'Preprocessing')
        graph.edge('Preprocessing', 'Algorithm')
        graph.edge('Algorithm', 'Visualization')
    else:
        path = 'utils/template-3-dataflow.svg'
        if os.path.exists(path):
            graph_path = graph_filename + '.svg'
            with open(path, "rt") as change:
                data = change.read()
                data = data.replace('dataset_name.csv', dataset_name + '.csv')
                data = data.replace('Scatter Plot/Confusion Matrix', visualisation)
                data = data.replace('Classifier/Regressor', algo_name)
            with open(graph_path, "wt") as change:
                change.write(data)
        else:
            print(f"The path '{path}' does not exist.")
        graph.edge('Dataset', 'Algorithm')
        graph.edge('Algorithm', 'Visualization')

    graph.save()

    return img_filename, graph_filename + '.svg', metric_name, metric_value, results_json

def tpot_pipeline_generator(restrictions):
    """
    Generates and evaluates a TPOT pipeline based on the specified intent, saves the pipeline and results, and returns file paths for the results.
    """
    data_file_path = restrictions.get('dataset')
    intent = restrictions.get('intent')

    df = pd.read_csv(data_file_path)

    X = df.iloc[:, :-1]
    y = df.iloc[:, -1]
    X_train, X_test, y_train, y_test = train_test_split(X, y, train_size=0.75, test_size=0.25, random_state=34)
    dataset_name = os.path.basename(data_file_path).split('.')[0]


    if intent == 'classification':
        # Default settings
        tpot = TPOTClassifier(verbosity=3,
                            scoring="balanced_accuracy",
                            random_state=23,
                            n_jobs=-1,
                            generations=1,
                            population_size=100)
        tpot.fit(X_train, y_train)
        metric_name = "accuracy"

    if intent == 'regression':
        # Default settings
        tpot = TPOTRegressor(verbosity=3,
                            scoring="neg_mean_absolute_error",
                            random_state=23,
                            n_jobs=-1,
                            generations=2,
                            population_size=100)
        tpot.fit(X_train, y_train)
        metric_name = "mae"

    metric_value = tpot.score(X_test, y_test)

    if not os.path.exists('results/tpot-results/pipelines'):
        os.makedirs('results/tpot-results/pipelines')
    tpot.export(f"results/tpot-results/pipelines/tpot_{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-{intent}_pipeline.py")
    with open(f"results/tpot-results/pipelines/tpot_{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-{intent}_pipeline.py", 'r') as f:
        python_code = f.read()

    nb = nbf.v4.new_notebook()
    nb['cells'] = [nbf.v4.new_code_cell(python_code)]

    if not os.path.exists('results/tpot-results/notebooks'):
        os.makedirs('results/tpot-results/notebooks')
    notebook_path = f"results/tpot-results/notebooks/tpot_{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-{intent}_pipeline.ipynb"
    with open(notebook_path, 'w') as f:
        nbf.write(nb, f)

    y_pred = tpot.predict(X_test)
    exctracted_best_model = tpot.fitted_pipeline_.steps[-1][1]

    if intent == 'classification':
        visualisation = 'Confusion Matrix'
        cm = confusion_matrix(y_test, y_pred)
        sns.heatmap(cm, annot=True, cmap='Blues', fmt='d', cbar=False)
        plt.xlabel('Predicted')
        plt.ylabel('True')
        plt.title('Confusion Matrix')
        if os.path.exists('results/tpot-results/images'):
            shutil.rmtree('results/tpot-results/images')
        os.makedirs('results/tpot-results/images')
        img_filename = f"results/tpot-results/images/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-conf_matrix.png"
        save_plot_as_image(img_filename)

    elif intent == 'regression':
        visualisation = 'Scatter Plot'
        plt.figure(figsize=(8, 6))
        plt.scatter(y_test, y_pred, color='blue', alpha=0.5)
        plt.plot([y_test.min(), y_test.max()], [y_test.min(), y_test.max()], 'k--', lw=2)
        plt.xlabel('Actual')
        plt.ylabel('Predicted')
        plt.title('Actual vs. Predicted Values')
        if os.path.exists('results/tpot-results/images'):
            shutil.rmtree('results/tpot-results/images')
        os.makedirs('results/tpot-results/images')
        img_filename = f"results/tpot-results/images/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-scatter_plot.png"
        save_plot_as_image(img_filename)
        metric_value = abs(metric_value)

    if not os.path.exists('results/tpot-results/dataflows'):
        os.makedirs('results/tpot-results/dataflows')

    graph_filename = f"results/tpot-results/dataflows/{dataset_name}-{datetime.now().strftime('%Y%m%d%H%M%S')}-dataflow"
    graph = Digraph('DataFlow', filename=graph_filename)
    graph.attr(rankdir='LR')

    graph.node('Dataset', fillcolor='orange', label=f'Dataset:\n{dataset_name}.csv')
    graph.node('Visualization', fillcolor='lightgreen', label=f'Visualization:\n{visualisation}')

    algo = exctracted_best_model
    algo_name = str(algo).split('(')[0]
    graph.node('Algorithm', fillcolor='lightblue', label=f'Algorithm:\n{algo_name}')

    path = 'utils/template-3-dataflow.svg'
    if os.path.exists(path):
        graph_path = graph_filename + '.svg'
        with open(path, "rt") as change:
            data = change.read()
            data = data.replace('dataset_name.csv', dataset_name + '.csv')
            data = data.replace('Scatter Plot/Confusion Matrix', visualisation)
            data = data.replace('Classifier/Regressor', algo_name)
        with open(graph_path, "wt") as change:
            change.write(data)
    else:
        print(f"The path '{path}' does not exist.")
        graph.edge('Dataset', 'Algorithm')
        graph.edge('Algorithm', 'Visualization')

    graph.save()

    return img_filename, graph_filename + '.svg', metric_name, metric_value

# if __name__ == "__main__":
#     if len(sys.argv) != 2:
#         print("Usage: python generate_ml_pipeline.py <file_path of csv file>")
#         sys.exit(1)

#     file_path = sys.argv[1]
#     restictions = {
#     "dataset": "data/preprocessed/iris.csv",
#     "intent": "regression",
#     "metric": "F1",
#     "preprocessing": True,
#     "hyperparameter": "",
#     "hyperparameterValue": "",
#     "algorithm": "sklearn-SVC",
#     "preprocessingAlgorithm": "sklearn-MinMaxScaler",
#     "timeLimit": ""
#     }   
#     # tpot_pipeline_generator(restictions)
#     hyperopt_pipeline_generator(restictions)
