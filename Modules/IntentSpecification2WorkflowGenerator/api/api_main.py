import shutil
from pathlib import Path

import proactive

from flask import Flask, request, send_file
from flask_cors import CORS

from api.functions import *
from pipeline_translator.pipeline_translator import translate_graph_folder, translate_graph
from dataset_annotator.annotator import annotate_dataset

import requests
import json

app = Flask(__name__)
CORS(app)

temporary_folder = os.path.abspath(r'./api/temp_files')

@app.get('/problems')
def get_problems():
    ontology_only_problems = get_custom_ontology_only_problems()
    problems = {n.fragment: n for n in ontology_only_problems.subjects(RDF.type, tb.Problem)}
    return problems


@app.post('/abstract_planner')
def run_abstract_planner():
    intent_graph = get_graph_xp()

    intent_name = request.json.get('intent_name', '')
    dataset = request.json.get('dataset', '')
    problem = request.json.get('problem', '')
    ontology = Graph().parse(data=request.json.get('ontology', ''), format='turtle')

    intent_graph.add((ab.term(intent_name), RDF.type, tb.Intent))
    intent_graph.add((ab.term(intent_name), tb.overData, URIRef(dataset)))
    intent_graph.add((ab.term(intent_name), tb.tackles, URIRef(problem)))

    intent = intent_graph
    abstract_plans, algorithm_implementations = abstract_planner(ontology, intent)
    return {"abstract_plans": abstract_plans, "intent": intent.serialize(format="turtle"),
            "algorithm_implementations": algorithm_implementations}


def convert_strings_to_uris(obj):
    if isinstance(obj, list):  # If the object is a list
        return [convert_strings_to_uris(item) for item in obj]  # Recursively process each item in the list
    elif isinstance(obj, dict):  # If the object is a dictionary (only the "first" level)
        # Recursively process each value in the dictionary + transform key to URI
        return {URIRef(key): convert_strings_to_uris(value) for key, value in obj.items()}
    else:
        return URIRef(obj)  # Convert non-list, non-dictionary values to URIs


@app.post('/logical_planner')
def run_logical_planner():
    plan_ids = request.json.get('plan_ids', '')
    intent_json = request.json.get('intent_graph', '')
    algorithm_implementations = request.json.get('algorithm_implementations', '')
    ontology = Graph().parse(data=request.json.get('ontology', ''), format='turtle')

    # The algorithms come from the frontend in String format. We need to change them back to URIRefs
    algorithm_implementations_uris = convert_strings_to_uris(algorithm_implementations)

    intent = Graph().parse(data=intent_json, format='turtle')

    impls = [impl
             for alg, impls in algorithm_implementations_uris.items() if str(alg) in plan_ids
             for impl in impls]

    workflow_plans = workflow_planner(ontology, impls, intent)
    ########### ORIGINAL CODE
    # logical_plans = logical_planner(ontology, workflow_plans)
    # return logical_plans

    ########### EXTREMEXP CODE
    logical_plans, extremexp_workflows, tasks = logical_planner_extremexp(ontology, workflow_plans)
    # Retrieving the intent name
    query_template = """ 
                        PREFIX ab: <https://extremexp.eu/ontology/abox#>
                        PREFIX tb: <https://extremexp.eu/ontology/tbox#>
                        SELECT ?subject
                        WHERE {
                          ?subject a tb:Intent .
                        } """
    results = intent.query(query_template)
    intent_name = ""
    for row in results:
        intent_name = str(row["subject"][row["subject"].find('#') + 1:])

    for workflow in extremexp_workflows:
        task_implementations = workflow.get('task_implementations', {})
        for key, value in task_implementations.items():
            updated_value = value.replace("intent_name", intent_name)
            task_implementations[key] = updated_value
        workflow['task_implementations'] = task_implementations

    json_data = {
        "intent_name": intent_name,
        "tasks": tasks,
        "workflows": extremexp_workflows
    }

    # Write data to JSON file
    with open(os.path.join(temporary_folder, "intent_to_dsl.json"), 'w') as json_file:
        json.dump(json_data, json_file, indent=4)

    return logical_plans


@app.post('/workflow_plans/knime/all')
def download_all_knime():
    graphs = request.json.get("graphs", "")
    ontology = Graph().parse(data=request.json.get('ontology', ''), format='turtle')

    folder = os.path.join(temporary_folder, 'rdf_to_trans')
    knime_folder = os.path.join(temporary_folder, 'knime')

    if os.path.exists(folder):
        shutil.rmtree(folder)
    if os.path.exists(knime_folder):
        shutil.rmtree(knime_folder)
    os.mkdir(folder)
    os.mkdir(knime_folder)

    for graph_id, graph_content in graphs.items():
        graph = Graph().parse(data=graph_content, format='turtle')
        file_path = os.path.join(folder, f'{graph_id}.ttl')
        graph.serialize(file_path, format='turtle')

    translate_graph_folder(ontology, folder, knime_folder, keep_folder=False)

    compress(knime_folder, knime_folder + '.zip')
    return send_file(knime_folder + '.zip', as_attachment=True)


@app.post('/workflow_plans/knime')
def download_knime():
    plan_graph = Graph().parse(data=request.json.get("graph", ""), format='turtle')
    ontology = Graph().parse(data=request.json.get('ontology', ''), format='turtle')

    file_path = os.path.join(temporary_folder, f'{uuid.uuid4()}.ttl')
    plan_graph.serialize(file_path, format='turtle')

    knime_file_path = file_path[:-4] + '.knwf'
    translate_graph(ontology, file_path, knime_file_path)

    return send_file(knime_file_path, as_attachment=True)


@app.post('/annotate_dataset')
def annotate_dataset_from_frontend():
    path = request.json.get('path', '')
    label = request.json.get('label', '')
    data_product_name = path[path.rfind("\\") + 1:-4]

    # new_path = path[0:path.rfind("\\") + 1] + data_product_name + "_annotated.ttl"
    new_path = temporary_folder + "/" + data_product_name + "_annotated.ttl"
    annotate_dataset(path, new_path, label)

    custom_ontology = get_custom_ontology(new_path)
    datasets = {n.fragment: n for n in custom_ontology.subjects(RDF.type, dmop.TabularDataset)}
    return {"ontology": custom_ontology.serialize(format="turtle"),
            "data_product_uri": datasets[data_product_name + ".csv"]}


################################# INTEGRATION FUNCTIONS

##### ZENOH
@app.post('/abstract_planner_zenoh')
def run_abstract_planner_zenoh():
    ### Annotate dataset
    intent_name = request.json.get('intent_name', '')
    problem = request.json.get('problem', '')
    original_dataset_path = request.json.get('original_dataset_path', '')
    downloaded_dataset_path = request.json.get('downloaded_dataset_path', '')
    label = request.json.get('label', '')
    zenoh_storage_path = request.json.get('zenoh_storage_path', '')
    token = request.json.get('token', '')
    dataset_name = Path(original_dataset_path).stem

    # Download from Zenoh
    url = "http://localhost:5000/file/" + zenoh_storage_path
    headers = {
        "accept": "application/json",
        "Authorization": f"Bearer {token}"
    }

    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        print("Successfully downloaded")
        with open(downloaded_dataset_path, 'wb') as file:
            file.write(response.content)
    else:
        print(f"Failed to download: {response.status_code}")
        print(f"Reason: {response.reason}")

    new_path = temporary_folder + "/" + dataset_name + "_annotated.ttl"
    annotate_dataset(original_dataset_path, new_path, label)

    custom_ontology = get_custom_ontology(new_path)
    datasets = {n.fragment: n for n in custom_ontology.subjects(RDF.type, dmop.TabularDataset)}
    data_product_uri = datasets[dataset_name + ".csv"]

    intent_graph = get_graph_xp()

    intent_graph.add((ab.term(intent_name), RDF.type, tb.Intent))
    intent_graph.add((ab.term(intent_name), tb.overData, URIRef(data_product_uri)))
    intent_graph.add((ab.term(intent_name), tb.tackles, URIRef(problem)))

    intent = intent_graph
    abstract_plans, algorithm_implementations = abstract_planner(custom_ontology, intent)
    return {"abstract_plans": abstract_plans, "intent": intent.serialize(format="turtle"),
            "algorithm_implementations": algorithm_implementations, "ontology": custom_ontology.serialize(format="turtle")}


@app.post('/store_rdf_zenoh')
def store_rdf_zenoh():
    logical_plans = request.json.get('logical_plans', '')
    selected_ids = request.json.get('selected_ids', '')
    zenoh_store_path = request.json.get('zenoh_store_path', '')
    token = request.json.get('token', '')

    rdf_file = Graph()
    for plan in logical_plans:
        for logical_plan_implementation in plan["plans"]:
            if logical_plan_implementation["id"] in selected_ids:  # select plan
                rdf_file.parse(data=logical_plan_implementation["graph"], format="turtle")
                path_to_store_rdf_file = os.path.join(temporary_folder, f"{logical_plan_implementation["id"]}.rdf")
                rdf_file.serialize(destination=path_to_store_rdf_file, format='turtle')

                ########### STORE RDF FILE ###########
                url = f"http://localhost:5000/file/{zenoh_store_path}{logical_plan_implementation["id"]}"
                headers = {
                    "accept": "application/json",
                    "Authorization": f"Bearer {token}"
                }
                # Define the file to be uploaded
                files = {
                    "file": (logical_plan_implementation["id"], open(path_to_store_rdf_file, "rb"), "application/pdf")
                }

                response = requests.post(url, headers=headers, files=files)
                if response.status_code == 200:
                    print("Successfully uploaded RDF file")
                else:
                    print(f"Failed to upload: {response.status_code}")
                    print(f"Reason: {response.reason}")

    return []

##### PROACTIVE

# TODO: Make the actual translation from the original RDF graph to the Proactive definition
@app.post('/workflow_plans/proactive')
def download_proactive():
    graph = Graph().parse(data=request.json.get("graph", ""), format='turtle')
    ontology = Graph().parse(data=request.json.get('ontology', ''), format='turtle')
    layout = request.json.get('layout', '')
    label_column = request.json.get('label_column', '')
    data_product_name = request.json.get('data_product_name', '')

    # Connect to Proactive
    gateway = proactive.ProActiveGateway(base_url="https://try.activeeon.com:8443", debug=False, javaopts=[], log4j_props_file=None,
                                         log4py_props_file=None)
    gateway.connect("pa75332", "testpwd")

    try:
        # Create one of the example workflows and send it (just to show that the SDK works)
        # proactive_job = gateway.createJob()
        # proactive_job.setJobName("extremexp_example_workflow")
        # bucket = gateway.getBucket("ai-machine-learning")
        #
        # load_iris_dataset_task = bucket.create_Load_Iris_Dataset_task()
        # proactive_job.addTask(load_iris_dataset_task)
        #
        # split_data_task = bucket.create_Split_Data_task()
        # split_data_task.addDependency(load_iris_dataset_task)
        # proactive_job.addTask(split_data_task)
        #
        # logistic_regression_task = bucket.create_Logistic_Regression_task()
        # proactive_job.addTask(logistic_regression_task)
        #
        # train_model_task = bucket.create_Train_Model_task()
        # train_model_task.addDependency(split_data_task)
        # train_model_task.addDependency(logistic_regression_task)
        # proactive_job.addTask(train_model_task)
        #
        # download_model_task = bucket.create_Download_Model_task()
        # download_model_task.addDependency(train_model_task)
        # proactive_job.addTask(download_model_task)
        #
        # predict_model_task = bucket.create_Predict_Model_task()
        # predict_model_task.addDependency(split_data_task)
        # predict_model_task.addDependency(train_model_task)
        # proactive_job.addTask(predict_model_task)
        #
        # preview_results_task = bucket.create_Preview_Results_task()
        # preview_results_task.addDependency(predict_model_task)
        # proactive_job.addTask(preview_results_task)

        # gateway.submitJob(proactive_job, debug=False)

        # Create another workflow and download it. This mimics the behavior that we will have to implement, as there is
        # no way (I think) to upload our own data inside a workflow. It has to be done before the execution of the workflow.
        # Hence, the idea is to generate the workflow, download it, upload the data, import the workflow and execute it.

        proactive_job = gateway.createJob()
        proactive_job.setJobName("extremexp_test_workflow")
        bucket = gateway.getBucket("ai-machine-learning")

        ################## Change file path (name) and label name (send both as parameters)
        load_dataset_task = bucket.create_Import_Data_task(import_from="PA:USER_FILE", file_path=data_product_name + ".csv", file_delimiter=";", label_column=label_column)
        proactive_job.addTask(load_dataset_task)

        # remove_nulls = bucket.create_Fill_NaNs_task(0)
        # split_data_task.addDependency(load_dataset_task)
        # proactive_job.addTask(remove_nulls)

        normalization_task = bucket.create_Scale_Data_task()
        normalization_task.addDependency(load_dataset_task)
        proactive_job.addTask(normalization_task)

        remove_nulls = bucket.create_Fill_NaNs_task()
        remove_nulls.addDependency(normalization_task)
        proactive_job.addTask(remove_nulls)

        split_data_task = bucket.create_Split_Data_task()
        split_data_task.addDependency(remove_nulls)
        proactive_job.addTask(split_data_task)

        # Model depends on the layout, the rest is the same
        model_task = bucket.create_Support_Vector_Machines_task()
        for key in layout:
            if "decision_tree_predictor" in key:
                model_task = bucket.create_Random_Forest_task()
                break
        proactive_job.addTask(model_task)

        train_model_task = bucket.create_Train_Model_task()
        train_model_task.addDependency(split_data_task)
        train_model_task.addDependency(model_task)
        proactive_job.addTask(train_model_task)

        predict_model_task = bucket.create_Predict_Model_task()
        predict_model_task.addDependency(split_data_task)
        predict_model_task.addDependency(train_model_task)
        proactive_job.addTask(predict_model_task)

        gateway.saveJob2XML(proactive_job, os.path.abspath(r'api/temp_files/extremexp_test_workflow.xml'))

    finally:
        print("Disconnecting")
        gateway.disconnect()
        print("Disconnected")
        gateway.terminate()
        print("Finished")

    return send_file(os.path.abspath(r'api/temp_files/extremexp_test_workflow.xml'), as_attachment=True)


if __name__ == '__main__':
    app.run(port=5001)
