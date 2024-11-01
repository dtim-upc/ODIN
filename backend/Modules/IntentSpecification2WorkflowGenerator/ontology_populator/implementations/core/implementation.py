import uuid
from typing import List, Union

from rdflib.collection import Collection

from common import *
from .parameter import Parameter

LiteralValue = Union[str, bool, int, float, None]


class Implementation:
    def __init__(self, name: str, algorithm: URIRef, parameters: List[Parameter],
                 input: List[Union[URIRef, List[URIRef]]] = None, output: List[URIRef] = None,
                 implementation_type=tb.Implementation,
                 counterpart: 'Implementation' = None,
                 namespace: Namespace = cb,
                 ) -> None:
        super().__init__()
        self.name = name
        self.url_name = f'implementation-{self.name.replace(" ", "_").replace("-", "_").lower()}'
        self.namespace = namespace
        self.uri_ref = self.namespace[self.url_name]
        self.algorithm = algorithm
        self.parameters = {param.label: param for param in parameters}
        self.input = input or []
        self.output = output or []
        assert implementation_type in {tb.Implementation, tb.LearnerImplementation, tb.ApplierImplementation}
        self.implementation_type = implementation_type
        self.counterpart = counterpart
        if self.counterpart is not None:
            assert implementation_type in {tb.LearnerImplementation, tb.ApplierImplementation}
            if self.counterpart.counterpart is None:
                self.counterpart.counterpart = self

        for parameter in self.parameters.values():
            parameter.uri_ref = self.namespace[f'{self.url_name}-{parameter.url_name}']

    def add_to_graph(self, g: Graph):
        # Base triples
        g.add((self.uri_ref, RDF.type, self.implementation_type))
        g.add((self.uri_ref, RDFS.label, Literal(self.name)))
        g.add((self.uri_ref, tb.implements, self.algorithm))

        # Input triples
        for i, input_tag in enumerate(self.input):
            input_node = BNode()
            g.add((input_node, RDF.type, tb.IOSpec))
            g.add((self.uri_ref, tb.specifiesInput, input_node))
            g.add((input_node, tb.has_position, Literal(i)))
            if isinstance(input_tag, list):
                if len(input_tag) > 1:
                    input_collection = BNode()
                    input_shape = self.namespace.term(f'Shape_{uuid.uuid4()}')
                    Collection(g, input_collection, input_tag)
                    g.add((input_shape, RDF.type, tb.DataTag))
                    g.add((input_shape, RDF.type, SH.NodeShape))
                    g.add((input_shape, SH['and'], input_collection))
                    g.add((input_node, tb.hasTag, input_shape))
                else:
                    g.add((input_node, tb.hasTag, input_tag[0]))
            else:
                g.add((input_node, tb.hasTag, input_tag))

        # Output triples
        for i, output_tag in enumerate(self.output):
            output_node = BNode()
            g.add((output_node, RDF.type, tb.IOSpec))
            g.add((self.uri_ref, tb.specifiesOutput, output_node))
            g.add((output_node, tb.hasTag, output_tag))
            g.add((output_node, tb.has_position, Literal(i)))

        # Parameter triples
        for i, parameter in enumerate(self.parameters.values()):
            g.add((parameter.uri_ref, RDF.type, tb.Parameter))
            g.add((parameter.uri_ref, RDFS.label, Literal(parameter.label)))
            g.add((parameter.uri_ref, tb.hasDatatype, parameter.datatype))
            g.add((parameter.uri_ref, tb.has_position, Literal(i)))
            g.add((parameter.uri_ref, tb.has_condition, Literal(parameter.condition)))
            if isinstance(parameter.default_value, URIRef):
                g.add((parameter.uri_ref, tb.hasDefaultValue, parameter.default_value))
            else:
                g.add((parameter.uri_ref, tb.hasDefaultValue, Literal(parameter.default_value)))
            g.add((self.uri_ref, tb.hasParameter, parameter.uri_ref))

        return self.uri_ref

    def add_counterpart_relationship(self, g: Graph):
        if self.counterpart is None:
            return
        counterpart_query = f'''
        PREFIX tb: <{tb}>
        SELECT ?self ?counterpart
        WHERE {{
            ?self a <{self.implementation_type}> ;
                rdfs:label "{self.name}" .
            ?counterpart a <{self.counterpart.implementation_type}> ;
                rdfs:label "{self.counterpart.name}" .
        }}
        '''
        result = g.query(counterpart_query).bindings
        assert len(result) == 1
        self_node = result[0][Variable('self')]
        relationship = tb.hasApplier if self.implementation_type == tb.LearnerImplementation else tb.hasLearner
        counterpart_node = result[0][Variable('counterpart')]
        g.add((self_node, relationship, counterpart_node))
