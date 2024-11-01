from random import randrange
import rdflib
from rdflib import BNode, URIRef, Literal, XSD
import time
from rdflib.namespace import RDF
import os

# Initialize namespaces
uri = "http://localhost/7200/intentOntology#"
dmop = "http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#"
dolce = "http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#"

ns = rdflib.Namespace(uri)
ns_dmop = rdflib.Namespace(dmop)
ns_dolce = rdflib.Namespace(dolce)

def parse_params(param_str):
    params = {}
    if param_str:
        for param in param_str.split(','):
            key, value = param.split('=', 1)
            key = key.strip()
            value = value.strip().strip('\'"')
            
            # Handle data types
            if value.isdigit():
                params[key] = int(value)
            elif value.replace('.', '', 1).isdigit():
                params[key] = float(value)
            elif value.lower() in ['true', 'false']:
                params[key] = value.lower() == 'true'
            else:
                params[key] = value
    return params

def generate_rdf_triples(data, file_path):
    g = rdflib.Graph()

    user_name = data['user']
    dataset_name = data['dataset']
    current_time = str(int(time.time()))

    user = URIRef(uri + user_name)
    workflow = URIRef(uri+'Worflow'+user_name+dataset_name+'-'+current_time)
    dataset = URIRef(uri + dataset_name)
    task = URIRef(uri+'Task'+user_name+dataset_name+'-'+current_time)

    g.add((user, RDF.type, ns.User))
    g.add((user,ns.runs,workflow))
    g.add((workflow,ns.hasFeedback,Literal(randrange(11), datatype=XSD.integer)))
    g.add((dataset,RDF.type,ns_dmop.DataSet))
    g.add((workflow,ns.hasInput,dataset))
    g.add((workflow,ns.achieves,task))

    optimization_time = data.get('time', None)
    if optimization_time:
        opt_time = URIRef(uri+'Time'+user_name+dataset_name+'-'+current_time)
        g.add((workflow,URIRef(dolce+'has-quality'),opt_time))
        g.add((opt_time,RDF.type,ns_dmop.OptimizationTime))
        g.add((opt_time,ns.hasValue,Literal(optimization_time, datatype=XSD.float)))


    '''
    In the future, the following two paragraphs should be instantiated previosly, with the creation of the ontology
    '''

    use = URIRef(uri+'Use')
    no_use = URIRef(uri+'NoUse')

    maximize = URIRef(uri+'Max')
    minimize = URIRef(uri+'Min')
    equal = URIRef(uri+'Equal')


    # Task Intent and Requirements
    intent = URIRef(uri + ('Classification' if data['intent'] == 'Classification' else 'Regression'))
    g.add((task, ns.hasIntent, intent))
    
    if 'metric_name' in data:
        evalRequirement = URIRef(uri+'EvalReq'+data['metric_name']+'TrainTestSplit')
        traintestsplit = URIRef(uri+'TrainTestSplit')
        metric = URIRef(uri+data['metric_name'])

        g.add((task,ns.hasRequirement,evalRequirement))
        g.add((evalRequirement,ns.withMethod,traintestsplit))
        g.add((evalRequirement,ns.onMetric,metric))
        g.add((evalRequirement,ns.howEval,maximize))


        modelEval = URIRef(uri+'ModelEval'+user_name+dataset_name+'-'+current_time)
        g.add((workflow,ns.hasOutput,modelEval))
        g.add((modelEval,ns.specifies,metric))
        g.add((modelEval,ns.hasValue,Literal(data['metric_value'], datatype=XSD.float)))


        
    # Task: Constraints
    algorithm_constraint = data.get('algorithm_constraint', None)
    if algorithm_constraint:
        const = URIRef(uri+'Constraint'+'sklearn-'+algorithm_constraint)
        g.add((const,RDF.type,URIRef(uri+'ConstraintAlgorithm')))
        g.add((task,ns.hasConstraint,const))
        g.add((const,ns.isHard,Literal(True, datatype=XSD.boolean)))
        g.add((const,ns.howConstraint,use))
        g.add((const,ns.on,URIRef(uri+'sklearn-'+algorithm_constraint)))
        

        hyp_constraint = data.get('hyperparam_constraints', None)

        if hyp_constraint:
            for i,hycon in enumerate(hyp_constraint):
                const = URIRef(uri+'Constraint'+algorithm_constraint+'-'+hycon)
                g.add((const,RDF.type,URIRef(uri+'ConstraintHyperparameter')))
                g.add((task,ns.hasConstraint,const))
                g.add((const,ns.isHard,Literal(True, datatype=XSD.boolean)))
                g.add((const,ns.howConstraint,equal))


                value = hyp_constraint[hycon]
                bn = BNode()
                g.add((task,ns.hasConstraintValue,bn))
                g.add((bn,ns.onConstraint,const))

                if type(value)==int:
                    g.add((bn,ns.hasValue,Literal(value, datatype=XSD.integer)))
                elif type(value)==float:
                    g.add((bn,ns.hasValue,Literal(value, datatype=XSD.float)))
                elif type(value)==str:
                    g.add((bn,ns.hasValue,Literal(value, datatype=XSD.string)))
                elif type(value)==bool:
                    g.add((bn,ns.hasValue,Literal(value, datatype=XSD.boolean)))

                g.add((const,ns.on,URIRef(uri+'sklearn-'+algorithm_constraint+'-'+hycon)))


        preprocessor_constraint = data.get('preprocessor_constraint', None)
        not_use_pre = URIRef(uri+'ConstraintNoPreprocessing')

        if preprocessor_constraint:
            if preprocessor_constraint != 'NoPre':
                const = URIRef(uri+'Constraint'+'sklearn-'+preprocessor_constraint)
                g.add((const,RDF.type,URIRef(uri+'ConstraintPreprocessingAlgorithm')))
                g.add((task,ns.hasConstraint,const))
                g.add((const,ns.howConstraint,use))
                g.add((const,ns.on,URIRef(uri+'sklearn-'+preprocessor_constraint)))
            else: 
                g.add((task,ns.hasConstraint,not_use_pre))
                g.add((const,ns.isHard,Literal(True, datatype=XSD.boolean)))

        max_time = data.get('max_time', None)
        
        if optimization_time and max_time:
            const = URIRef(uri+'TimeConstraint')
            g.add((const,RDF.type,URIRef(uri+'ConstraintWorkflow')))
            g.add((task,ns.hasConstraint,const))
            g.add((const,ns.on,opt_time))
            g.add((const,ns.isHard,Literal(True, datatype=XSD.boolean)))

            bn = BNode()
            g.add((task,ns.hasConstraintValue,bn))
            g.add((bn,ns.onConstraint,const))
            g.add((bn,ns.hasValue,Literal(max_time, datatype=XSD.boolean)))
            


    ## PIPELINE AND STEPS
    prepro_list = data.get('pipeline', {}).get('preprocs', [])
    if prepro_list:
        model = URIRef(uri + 'Model-' + user_name + '-' + dataset_name + '-' + current_time)
        preproc = URIRef(uri + 'Prepro-' + user_name + '-' + dataset_name + '-' + current_time)
        g.add((workflow, ns.hasStep, model))
        g.add((workflow, ns.hasStep, preproc))
        g.add((model, ns.order, Literal(2, datatype=XSD.integer)))
        g.add((preproc, ns.order, Literal(1, datatype=XSD.integer)))
        g.add((preproc, ns.followedBy, model))

        prepro = prepro_list[0]
        name = str(prepro).split('(')[0]
        g.add((preproc, ns.hasImplementation, URIRef(uri + 'sklearn-' + name)))

        param_str = str(prepro).split('(')[1].strip(')') if '(' in prepro else ''
        params = parse_params(param_str)
        for param in params:
            hyperinput = URIRef(uri + user_name + '-' + dataset_name + '-' + name + '-' + param + '-' + current_time)
            g.add((preproc, ns.hasHyperparamInput, hyperinput))
            g.add((URIRef(uri + 'sklearn-' + name + '-' + param), ns.specifiedBy, hyperinput))
            value = params[param]
            datatype = XSD.string
            if isinstance(value, int):
                datatype = XSD.integer
            elif isinstance(value, float):
                datatype = XSD.float
            elif isinstance(value, bool):
                datatype = XSD.boolean

            g.add((hyperinput, ns.hasValue, Literal(value, datatype=datatype)))

    else:
        model = URIRef(uri+'Model'+user_name+dataset_name+'-'+current_time)
        g.add((workflow,ns.hasStep,model))
        g.add((model,ns.order,Literal(1, datatype=XSD.integer)))
    
    algo = data['pipeline']['learner']
    name = str(algo).split('(')[0]
    g.add((model,ns.hasImplementation,URIRef(uri+'sklearn-'+name)))


    param_str = str(algo).split('(')[1].strip(')') if '(' in algo else ''
    params = parse_params(param_str)
    for param in params:
        hyperinput = URIRef(uri + user_name + '-' + dataset_name + '-' + name + '-' + param + '-' + current_time)
        g.add((model, ns.hasHyperparamInput, hyperinput))
        g.add((URIRef(uri + 'sklearn-' + name + '-' + param), ns.specifiedBy, hyperinput))
        value = params[param]
        datatype = XSD.string
        if isinstance(value, int):
            datatype = XSD.integer
        elif isinstance(value, float):
            datatype = XSD.float
        elif isinstance(value, bool):
            datatype = XSD.boolean

        g.add((hyperinput, ns.hasValue, Literal(value, datatype=datatype)))

    
    file_exists = os.path.isfile(file_path)
    
    mode = 'ab' if file_exists else 'wb'
    
    # Open the file in the determined mode and serialize the graph
    with open(file_path, mode) as file:
        # Serialize the graph to the file in N-Triples format
        g.serialize(format="nt", destination=file)
    
    print(f'Graph serialized to {file_path}')
    return g

def generate_sparql_insert_query(data):
    insert_query = """
    PREFIX ns: <{0}>
    PREFIX ns_dmop: <{1}>
    PREFIX ns_dolce: <{2}>
    PREFIX rdf: <{3}>
    PREFIX xsd: <{4}>

    INSERT DATA {{
    """.format(uri, dmop, dolce, RDF, XSD)

    user_name = data['user']
    dataset_name = data['dataset']
    current_time = str(int(time.time()))

    user_uri = uri + user_name
    workflow_uri =uri+'Worflow'+user_name+dataset_name+'-'+current_time
    dataset_uri = uri + dataset_name
    task_uri = uri+'Task'+user_name+dataset_name+'-'+current_time
    # random integer for feedback
    feedback_value = randrange(11)

    insert_query += f"""
        <{user_uri}> rdf:type ns:User .
        <{user_uri}> ns:runs <{workflow_uri}> .
        <{workflow_uri}> ns:hasFeedback "{feedback_value}"^^xsd:integer .
        <{workflow_uri}> ns:hasInput <{dataset_uri}> .
        <{workflow_uri}> ns:achieves <{task_uri}> .
        <{dataset_uri}> rdf:type ns_dmop:DataSet .
    """

    optimization_time = data.get('time', None)
    if optimization_time:
        opt_time_uri = uri+'Time'+user_name+dataset_name+'-'+current_time
        insert_query += f"""
            <{workflow_uri}> ns_dolce:has-quality <{opt_time_uri}> .
            <{opt_time_uri}> rdf:type ns_dmop:OptimizationTime .
            <{opt_time_uri}> ns:hasValue "{optimization_time}"^^xsd:float .
        """

    intent_uri = uri + ('Classification' if data['intent'] == 'Classification' else 'Regression')
    insert_query += f"""
        <{task_uri}> ns:hasIntent <{intent_uri}> .
    """

    if 'metric_name' in data:
        eval_req_uri = uri+'EvalReq'+data['metric_name']+'TrainTestSplit'
        train_test_split_uri = uri + 'TrainTestSplit'
        metric_uri = uri + data['metric_name']
        model_eval_uri =uri+'ModelEval'+user_name+dataset_name+'-'+current_time

        insert_query += f"""
            <{task_uri}> ns:hasRequirement <{eval_req_uri}> .
            <{eval_req_uri}> ns:withMethod <{train_test_split_uri}> .
            <{eval_req_uri}> ns:onMetric <{metric_uri}> .
            <{eval_req_uri}> ns:howEval <{uri + 'Max'}> .
            <{workflow_uri}> ns:hasOutput <{model_eval_uri}> .
            <{model_eval_uri}> ns:specifies <{metric_uri}> .
            <{model_eval_uri}> ns:hasValue "{data['metric_value']}"^^xsd:float .
        """

    algorithm_constraint = data.get('algorithm_constraint', None)
    if algorithm_constraint:
        const_uri = uri+'Constraint'+'sklearn-'+algorithm_constraint
        insert_query += f"""
            <{const_uri}> rdf:type <{uri + 'ConstraintAlgorithm'}> .
            <{task_uri}> ns:hasConstraint <{const_uri}> .
            <{const_uri}> ns:isHard "true"^^xsd:boolean .
            <{const_uri}> ns:howConstraint <{uri + 'Use'}> .
            <{const_uri}> ns:on <{uri + 'sklearn-' + algorithm_constraint}> .
        """

    hyp_constraint = data.get('hyperparam_constraints', None)
    if hyp_constraint:
        for hycon, value in hyp_constraint.items():
            const_uri = uri + 'Constraint' + algorithm_constraint + '-' + hycon
            insert_query += f"""
                <{const_uri}> rdf:type <{uri + 'ConstraintHyperparameter'}> .
                <{task_uri}> ns:hasConstraint <{const_uri}> .
                <{const_uri}> ns:isHard "true"^^xsd:boolean .
                <{const_uri}> ns:howConstraint <{uri + 'Equal'}> .
                <{const_uri}> ns:on <{uri + 'sklearn-' + algorithm_constraint + '-' + hycon}> .
            """
            bn = BNode()
            datatype = "xsd:string"
            if isinstance(value, int):
                datatype = "xsd:integer"
            elif isinstance(value, float):
                datatype = "xsd:float"
            elif isinstance(value, bool):
                datatype = "xsd:boolean"

            insert_query += f"""
                <{task_uri}> ns:hasConstraintValue _:bn .
                _:bn ns:onConstraint <{const_uri}> .
                _:bn ns:hasValue "{value}"^^{datatype} .
            """

    preprocessor_constraint = data.get('preprocessor_constraint', None)
    if preprocessor_constraint:
        if preprocessor_constraint != 'NoPre':
            const_uri = uri+'Constraint'+'sklearn-'+preprocessor_constraint
            insert_query += f"""
                <{const_uri}> rdf:type <{uri + 'ConstraintPreprocessingAlgorithm'}> .
                <{task_uri}> ns:hasConstraint <{const_uri}> .
                <{const_uri}> ns:howConstraint <{uri + 'Use'}> .
                <{const_uri}> ns:on <{uri + 'sklearn-' + preprocessor_constraint}> .
            """
        else:
            not_use_pre = uri + 'ConstraintNoPreprocessing'
            insert_query += f"""
                <{task_uri}> ns:hasConstraint <{not_use_pre}> .
                <{const_uri}> ns:isHard "true"^^xsd:boolean .
            """

    max_time = data.get('max_time', None)
    if optimization_time and max_time:
        const_uri = uri + 'TimeConstraint'
        insert_query += f"""
            <{const_uri}> rdf:type <{uri + 'ConstraintWorkflow'}> .
            <{task_uri}> ns:hasConstraint <{const_uri}> .
            <{const_uri}> ns:on <{uri+'Time'+user_name+dataset_name+'-'+current_time}> .
            <{const_uri}> ns:isHard "true"^^xsd:boolean .
        """
        insert_query += f"""
            <{task_uri}> ns:hasConstraintValue _:bn .
            _:bn ns:onConstraint <{const_uri}> .
            _:bn ns:hasValue "{max_time}"^^xsd:boolean .
        """

    ## PIPELINE AND STEPS
    prepro_list = data.get('pipeline', {}).get('preprocs', [])
    if prepro_list:
        model_uri = uri+'Model'+user_name+dataset_name+'-'+current_time
        preproc_uri =uri+'Prepro'+user_name+dataset_name+'-'+current_time
        insert_query += f"""
            <{workflow_uri}> ns:hasStep <{model_uri}> .
            <{workflow_uri}> ns:hasStep <{preproc_uri}> .
            <{model_uri}> ns:order "2"^^xsd:integer .
            <{preproc_uri}> ns:order "1"^^xsd:integer .
            <{preproc_uri}> ns:followedBy <{model_uri}> .
        """
        prepro = prepro_list[0] 
        name = str(prepro).split('(')[0]
        insert_query += f"""
            <{preproc_uri}> ns:hasImplementation <{uri + 'sklearn-' + name}> .
        """
        param_str = str(prepro).split('(')[1].strip(')') if '(' in prepro else ''
        params = parse_params(param_str)
        for param, value in params.items():
            hyperinput_uri = uri+user_name+dataset_name+name+param+'-'+current_time
            insert_query += f"""
                <{preproc_uri}> ns:hasHyperparamInput <{hyperinput_uri}> .
                <{uri + 'sklearn-' + name + '-' + param}> ns:specifiedBy <{hyperinput_uri}> .
            """
            datatype = "xsd:string"
            if isinstance(value, int):
                datatype = "xsd:integer"
            elif isinstance(value, float):
                datatype = "xsd:float"
            elif isinstance(value, bool):
                datatype = "xsd:boolean"

            insert_query += f"""
                <{hyperinput_uri}> ns:hasValue "{value}"^^{datatype} .
            """
    else:
        model_uri =uri+'Model'+user_name+dataset_name+'-'+current_time
        insert_query += f"""
            <{workflow_uri}> ns:hasStep <{model_uri}> .
            <{model_uri}> ns:order "1"^^xsd:integer .
        """

    algorithm = data['pipeline']['learner']
    name = str(algorithm).split('(')[0]

    if algorithm:
        insert_query += f"""
            <{model_uri}> ns:hasImplementation <{uri + 'sklearn-' + name}> .
        """
        for param, value in data.get('hyperparams', {}).items():
            hyperinput_uri = uri+user_name+dataset_name+name+param+'-'+current_time
            insert_query += f"""
                <{model_uri}> ns:hasHyperparamInput <{hyperinput_uri}> .
                <{uri + 'sklearn-' + name + '-' + param}> ns:specifiedBy <{hyperinput_uri}> .
            """
            datatype = "xsd:string"
            if isinstance(value, int):
                datatype = "xsd:integer"
            elif isinstance(value, float):
                datatype = "xsd:float"
            elif isinstance(value, bool):
                datatype = "xsd:boolean"

            insert_query += f"""
                <{hyperinput_uri}> ns:hasValue "{value}"^^{datatype} .
            """

    insert_query += "}\n"
    workflow_name = 'Worflow'+user_name+dataset_name+'-'+current_time

    return insert_query, workflow_uri, user_uri, workflow_name

# Example usage
# data = {
#     'user': 'john_doe',
#     'dataset': 'iris',
#     'intent': 'Classification',
#     'algorithm_constraint': 'SVC',
#     'hyperparam_constraints': {
        
#     },
#     'time': 100,
#     'preprocessor_constraint': 'StandardScaler',
#     'time': 100,
#     'max_time': 300,
#     'pipeline': {
#         'preprocs': [
#             'StandardScaler()',
#         ],
#         'learner': 'SVC(C=1.0)'
#     },
#     'metricName': 'Accuracy',
#     'metric_value': 0.90
# }

# generate_rdf_triples(data, 'RDFtriples.nt')
# sparql_query = generate_sparql_insert_query(data)
# print(sparql_query)