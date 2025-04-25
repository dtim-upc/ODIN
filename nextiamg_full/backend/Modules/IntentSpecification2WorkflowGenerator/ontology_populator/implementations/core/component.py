from typing import List, Union, Tuple, Any

from rdflib.collection import Collection

from common import *
from .implementation import Implementation
from .transformation import Transformation

LiteralValue = Union[str, bool, int, float, None]


class Component:

    def __init__(self, name: str, implementation: Implementation, transformations: List[Transformation],
                 exposed_parameters: List[str] = None,
                 overriden_parameters: List[Tuple[str, Any]] = None,
                 counterpart: Union['Component', List['Component']] = None,
                 namespace: Namespace = cb) -> None:
        super().__init__()
        self.name = name
        self.url_name = f'component-{self.name.replace(" ", "_").replace("-", "_").lower()}'
        self.uri_ref = namespace[self.url_name]

        self.implementation = implementation
        self.transformations = transformations
        self.overriden_parameters = overriden_parameters if overriden_parameters is not None else []
        self.exposed_parameters = exposed_parameters if exposed_parameters is not None else []
        self.component_type = {
            tb.LearnerImplementation: tb.LearnerComponent,
            tb.ApplierImplementation: tb.ApplierComponent,
            tb.Implementation: tb.Component,
        }[self.implementation.implementation_type]
        self.counterpart = counterpart
        if self.counterpart is not None:
            assert self.component_type in {tb.LearnerComponent, tb.ApplierComponent}
            if isinstance(self.counterpart, list):
                for c in self.counterpart:
                    if c.counterpart is None:
                        c.counterpart = self
            elif self.counterpart.counterpart is None:
                self.counterpart.counterpart = self

    def add_to_graph(self, g: Graph):

        # Base triples
        g.add((self.uri_ref, RDF.type, self.component_type))
        g.add((self.uri_ref, RDFS.label, Literal(self.name)))
        g.add((self.uri_ref, tb.hasImplementation, self.implementation.uri_ref))

        # Transformation triples
        transformation_nodes = []
        for transformation in self.transformations:
            trans_ref = BNode()
            g.add((trans_ref, RDF.type, transformation.owl_type))
            for p, o in transformation.triples():
                g.add((trans_ref, p, o))
            transformation_nodes.append(trans_ref)

        g.add((self.uri_ref, tb.hasTransformation, Collection(g, uri=BNode(), seq=transformation_nodes).uri))

        # Overriden parameters triples
        for parameter, value in self.overriden_parameters:
            parameter_value = BNode()
            g.add((parameter_value, RDF.type, tb.ParameterValue))
            g.add((parameter_value, tb.forParameter, self.implementation.parameters[parameter].uri_ref))
            g.add((parameter_value, tb.has_value, Literal(value)))
            g.add((self.uri_ref, tb.overridesParameter, parameter_value))

        # Exposed parameters triples
        for parameter in self.exposed_parameters:
            g.add((self.uri_ref, tb.exposesParameter, self.implementation.parameters[parameter].uri_ref))

        return self.uri_ref

    def add_counterpart_relationship(self, g: Graph):
        if self.counterpart is None:
            return
        counters = self.counterpart if isinstance(self.counterpart, list) else [self.counterpart]
        for c in counters:
            counterpart_query = f'''
            PREFIX tb: <{tb}>
            SELECT ?self ?counterpart
            WHERE {{
                ?self a <{self.component_type}> ;
                    rdfs:label "{self.name}" .
                ?counterpart a <{c.component_type}> ;
                    rdfs:label "{c.name}" .
            }}
            '''
            result = g.query(counterpart_query).bindings
            assert len(result) == 1
            self_node = result[0][Variable('self')]
            relationship = tb.hasApplier if self.component_type == tb.LearnerComponent else tb.hasLearner
            counterpart_node = result[0][Variable('counterpart')]
            g.add((self_node, relationship, counterpart_node))
