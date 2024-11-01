import sys

from rdflib.collection import Collection

from common import *


def add_class(graph, nodes):
    l = nodes if isinstance(nodes, list) else [nodes]
    for node in l:
        graph.add((node, RDF.type, OWL.Class))


def add_union(graph, nodes):
    sequence = Collection(graph, BNode(), nodes)
    union = BNode()
    graph.add((union, OWL.unionOf, sequence.uri))
    return union


def add_object_property(graph, property, domain, range):
    graph.add((property, RDF.type, OWL.ObjectProperty))
    if domain:
        graph.add((property, RDFS.domain, add_union(graph, domain) if isinstance(domain, list) else domain))
    if range:
        graph.add((property, RDFS.range, add_union(graph, range) if isinstance(range, list) else range))


def add_datatype_property(graph, property, domain, range):
    if isinstance(range, list):
        for r in range:
            assert r in XSD
    else:
        assert range in XSD
    graph.add((property, RDF.type, OWL.DatatypeProperty))
    if domain:
        graph.add((property, RDFS.domain, add_union(graph, domain) if isinstance(domain, list) else domain))
    if range:
        graph.add((property, RDFS.range, add_union(graph, range) if isinstance(range, list) else range))


def add_property(graph, property, domain, range):
    graph.add((property, RDF.type, RDF.Property))
    if domain:
        graph.add((property, RDFS.domain, add_union(graph, domain) if isinstance(domain, list) else domain))
    if range:
        graph.add((property, RDFS.range, add_union(graph, range) if isinstance(range, list) else range))


def init_ontology() -> Graph:
    ontology = get_graph_xp()

    ontology.add((URIRef(str(tb)), RDF.type, OWL.Ontology))
    ontology.add((URIRef(str(tb)), RDFS.label, Literal("ExtremeXP Ontology TBox")))
    return ontology


def add_classes(ontology: Graph):
    classes = [
        tb.Data,
        tb.Intent,
        tb.Problem,
        tb.Algorithm,
        tb.Workflow,
        tb.DataTag,
        tb.Step,
        tb.Component,
        tb.LearnerComponent,
        tb.ApplierComponent,
        tb.Implementation,
        tb.LearnerImplementation,
        tb.ApplierImplementation,
        tb.Parameter,
        tb.ParameterValue,
        tb.Transformation,
        tb.CopyTransformation,
        tb.LoaderTransformation,
        tb.IOSpec,
        tb.IO,
    ]
    add_class(ontology, classes)

    ontology.add((tb.CopyTransformation, RDFS.subClassOf, tb.Transformation))
    ontology.add((tb.LoaderTransformation, RDFS.subClassOf, tb.Transformation))

    ontology.add((tb.LearnerImplementation, RDFS.subClassOf, tb.Implementation))
    ontology.add((tb.ApplierImplementation, RDFS.subClassOf, tb.Implementation))
    ontology.add((tb.LearnerImplementation, OWL.disjointWith, tb.ApplierImplementation))

    ontology.add((tb.LearnerComponent, RDFS.subClassOf, tb.Component))
    ontology.add((tb.ApplierComponent, RDFS.subClassOf, tb.Component))
    ontology.add((tb.LearnerComponent, OWL.disjointWith, tb.ApplierComponent))


def add_properties(ontology: Graph):
    properties = [
        # Intent
        (tb.overData, tb.Intent, tb.Data),
        (tb.tackles, tb.Intent, [tb.Problem, tb.Algorithm]),
        (tb.usingParameter, tb.Intent, [tb.Parameter, tb.ParameterValue]),
        (tb.createdFor, tb.Workflow, tb.Intent),
        # Problem
        (tb.subProblemOf, tb.Problem, tb.Problem),
        (tb.solves, [tb.Algorithm, tb.Workflow], tb.Problem),
        # Workflow
        (tb.applies, tb.Workflow, tb.Algorithm),
        (tb.hasStep, tb.Workflow, tb.Step),
        # Workflow / Implementation
        (tb.hasParameter, [tb.Workflow, tb.Implementation], tb.Parameter),
        (tb.specifiesInput, [tb.Workflow, tb.Implementation], tb.IOSpec),
        (tb.specifiesOutput, [tb.Workflow, tb.Implementation], tb.IOSpec),
        # Implementation
        (tb.hasParameter, tb.Implementation, tb.Parameter),
        (tb.hasLearner, tb.ApplierImplementation, tb.LearnerImplementation),
        (tb.hasApplier, tb.LearnerImplementation, tb.ApplierImplementation),
        # Component
        (tb.hasTransformation, tb.Component, RDF.List),
        (tb.hasImplementation, tb.Component, tb.Implementation),
        (tb.overridesParameter, tb.Component, tb.ParameterValue),
        (tb.exposesParameter, tb.Component, tb.Parameter),
        (tb.hasLearner, tb.ApplierComponent, tb.LearnerComponent),
        (tb.hasApplier, tb.LearnerComponent, tb.ApplierComponent),
        # Step
        (tb.followedBy, tb.Step, tb.Step),
        (tb.runs, tb.Step, [tb.Workflow, tb.Implementation]),
        (tb.hasParameterValue, tb.Step, tb.ParameterValue),
        (tb.hasInput, tb.Step, tb.IO),
        (tb.hasOutput, tb.Step, tb.IO),
        # Parameter
        (tb.forParameter, tb.ParameterValue, tb.Parameter),
        (tb.hasDatatype, tb.Parameter, None),
        (tb.hasDefaultValue, tb.Parameter, None),
        # Data
        # (tb.conformsTo, tb.Data, tb.DataTag),
        # IOSpec
        (tb.hasTag, tb.IOSpec, tb.DataTag),
        # IO
        (tb.hasData, tb.IOSpec, tb.Data),
    ]
    for s, p, o in properties:
        add_object_property(ontology, s, p, o)

    ontology.add((tb.subProblemOf, RDF.type, OWL.TransitiveProperty))

    dproperties = [
        # Transformation
        (tb.copy_input, tb.CopyTransformation, XSD.integer),
        (tb.copy_output, tb.CopyTransformation, XSD.integer),
        (tb.transformation_language, tb.Transformation, XSD.string),
        (tb.transformation_query, tb.Transformation, XSD.string),
        # IO
        (tb.has_position, [tb.IO, tb.IOSpec, tb.Step, tb.Parameter], XSD.integer),
    ]

    for s, p, o in dproperties:
        add_datatype_property(ontology, s, p, o)

    oproperties = [
        (tb.has_value, tb.ParameterValue, None),
    ]

    for s, p, o in oproperties:
        add_property(ontology, s, p, o)

    subproperties = [
        # Column
        (dmop.hasColumnName, dmop.ColumnInfoProperty),
        (dmop.hasDataPrimitiveTypeColumn, dmop.ColumnInfoProperty),
        (dmop.hasPosition, dmop.ColumnInfoProperty),
        (dmop.isCategorical, dmop.ColumnInfoProperty),
        (dmop.isFeature, dmop.ColumnInfoProperty),
        (dmop.isLabel, dmop.ColumnInfoProperty),
        (dmop.isUnique, dmop.ColumnInfoProperty),
        (dmop.containsNulls, dmop.ColumnValueInfoProperty),
        (dmop.hasMeanValue, dmop.ColumnValueInfoProperty),
        (dmop.hasStandardDeviation, dmop.ColumnValueInfoProperty),
        (dmop.hasMaxValue, dmop.ColumnValueInfoProperty),
        (dmop.hasMinValue, dmop.ColumnValueInfoProperty),

        # Dataset
        (dmop.delimiter, dmop.DatasetPhysicalProperty),
        (dmop.doubleQuote, dmop.DatasetPhysicalProperty),
        (dmop.encoding, dmop.DatasetPhysicalProperty),
        (dmop.fileFormat, dmop.DatasetPhysicalProperty),
        (dmop.hasHeader, dmop.DatasetPhysicalProperty),
        (dmop.isNormalized, dmop.DatasetValueInfoProperty),
        (dmop.lineDelimiter, dmop.DatasetPhysicalProperty),
        (dmop.numberOfColumns, dmop.DatasetInfoProperty),
        (dmop.numberOfRows, dmop.DatasetInfoProperty),
        (dmop.path, dmop.DatasetPhysicalProperty),
        (dmop.quoteChar, dmop.DatasetPhysicalProperty),
        (dmop.skipInitialSpace, dmop.DatasetPhysicalProperty),
    ]

    for s, o in subproperties:
        ontology.add((s, RDFS.subPropertyOf, o))


def main(dest: str = '../ontologies/tbox.ttl') -> None:
    ontology = init_ontology()
    add_classes(ontology)
    add_properties(ontology)
    ontology.serialize(dest, format='turtle')


if __name__ == '__main__':
    if len(sys.argv) > 1:
        main(sys.argv[1])
    else:
        main()
