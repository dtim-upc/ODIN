from owlrl import DeductiveClosure, OWLRL_Semantics
from rdflib import *

dmop = Namespace('http://www.e-lico.eu/ontologies/dmo/DMOP/DMOP.owl#')
tb = Namespace('https://extremexp.eu/ontology/tbox#')
cb = Namespace('https://extremexp.eu/ontology/cbox#')
ab = Namespace('https://extremexp.eu/ontology/abox#')


def get_graph_xp():
    g = Graph()
    g.bind('tb', tb)
    g.bind('cb', cb)
    g.bind('ab', ab)
    g.bind('dmop', dmop)
    return g


def get_ontology_graph():
    graph = get_graph_xp()
    ontologies = [
        r'ontologies/tbox.ttl',
        r'ontologies/cbox.ttl',
        r'ontologies/abox.ttl',
        # r'dataset_annotator/penguins_annotated.ttl',
        r'dataset_annotator/annotated_datasets/titanic_annotated.ttl',
        r'dataset_annotator/annotated_datasets/diabetes_annotated.ttl',
    ]
    for o in ontologies:
        graph.parse(o, format="turtle")

    DeductiveClosure(OWLRL_Semantics).expand(graph)
    return graph
