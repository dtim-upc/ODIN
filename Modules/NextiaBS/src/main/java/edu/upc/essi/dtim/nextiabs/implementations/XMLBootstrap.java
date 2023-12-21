package edu.upc.essi.dtim.nextiabs.implementations;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.bootstrap.IBootstrap;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapODIN;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS.productionRulesDataframe_to_RDFS;

/**
 * Generates an RDFS-compliant representation of an XML file schema
 * @author snadal
 */
public class XMLBootstrap extends DataSource implements IBootstrap<Graph>, BootstrapODIN {
	// Using DataFrame_MM and without Jena
	public String path;

	public XMLBootstrap(String id, String name, String path) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
	}

	@Override
	public Graph bootstrapSchema(Boolean generateMetadata) {
		G_target = CoreGraphFactory.createGraphInstance("local");
//		setPrefixes();
		try {
			//build the XML DOM
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(path));
			document.getDocumentElement().normalize();

			// Get the root element
			Element root = document.getDocumentElement();
			String rootName = root.getNodeName();
			System.out.println("Root element: " + rootName);
			for (int n = 0; n < root.getChildNodes().getLength(); n++) System.out.println("  " + n + "-" + root.getChildNodes().item(n).getNodeName());

			//Add the triples to the graph
			G_target.addTriple(createIRI(rootName), RDF.type, DataFrame_MM.DataFrame);
			G_target.addTripleLiteral(createIRI(rootName), RDFS.label, rootName);

			// Extract attributes recursively
			extractSubElementsFromElement(root);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//TODO: implement wrapper and metadata
//		String select =  parser.getHeaderNames().stream().map(a ->{ return  a +" AS "+ a.replace(".","_"); }).collect(Collectors.joining(","));
//		wrapper = "SELECT " + select  + " FROM " + name;

//		if(generateMetadata)
//			generateMetadata();
//		G_target.setPrefixes(prefixes);

		G_target = productionRulesDataframe_to_RDFS(G_target);

		return G_target;
	}

	/**
	 * Extracts the attributes from an XML element and adds them to the Dataframe_MM instance graph.
	 * This function gets called recursively until it reaches the leaf nodes of the XML tree (text nodes).
	 * @param element The XML element to extract the attributes from (either root element or any children of any node)
	 */
	private void extractSubElementsFromElement(Element element) {
		NodeList childNodes = element.getChildNodes();
		String parentName = element.getNodeName();
		int numChildren = childNodes.getLength();

		//for each children node
		for (int n = 0; n < numChildren; n++) {
			Node child = childNodes.item(n);
			String childName = child.getNodeName();

			//If it's TEXT_NODE and it's not empty (if empty-->IGNORE)
			//--> add Parent Node as property with range string
			//    (domain gets treated later)
			if(child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().replaceAll("\n","").isBlank()){
				G_target.addTriple(createIRI(parentName),RDF.type,DataFrame_MM.Data);
				G_target.addTripleLiteral(createIRI(parentName), RDFS.label,parentName);
				G_target.addTriple(createIRI(parentName),DataFrame_MM.hasDataType,DataFrame_MM.String);
			}

			//If it's ELEMENT_NODE
			else if (child.getNodeType() == Node.ELEMENT_NODE){
				//If it has some children NOT TEXT_NODE to process
				//--> add that node as Class
				if(hasElementChildren(child)) {
					G_target.addTriple(createIRI(childName), RDF.type, DataFrame_MM.DataFrame);
					G_target.addTripleLiteral(createIRI(childName), RDFS.label, childName);
				}

				//If it has only one child TEXT_NODE to process
				//--> conect that node's domain to his parent
				//    (the other atributes like type, label or range are set in his TEXT_NODE child if it's not empty when processed)
				if(hasOneNonEmptyTextChildren(child)) {
					G_target.addTriple(createIRI(parentName),DataFrame_MM.hasData,createIRI(childName));
				}

				//if it's ELEMENT_NODE --> process children recursively
				if(child.hasChildNodes())extractSubElementsFromElement((Element) child);
			}
		}
	}

	/**
	 * Checks if a node has at least one child TEXT_NODE and that child is not empty
	 * @param node The node to check
	 * @return true if the node has at least one child TEXT_NODE and that child is not empty, false otherwise
	 */
	private boolean hasOneNonEmptyTextChildren(Node node) {
		NodeList childNodes = node.getChildNodes();
		int numChildren = childNodes.getLength(), i = 0;
		boolean hasOneNonEmptyTextChildren = false;

		while(i < numChildren && !hasOneNonEmptyTextChildren){
			Node child = childNodes.item(i);
			if(child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().replaceAll("\n","").isBlank()) hasOneNonEmptyTextChildren = true;
			++i;
		}

		return hasOneNonEmptyTextChildren;
	}

	/**
	 * Checks if a node has at least one child ELEMENT_NODE
	 * @param node The node to check
	 * @return true if the node has at least one child ELEMENT_NODE, false otherwise
	 */
	private boolean hasElementChildren(Node node) {
		NodeList childNodes = node.getChildNodes();
		int numChildren = childNodes.getLength(), i = 0;
		boolean hasElementChildren = false;

		while(i < numChildren && !hasElementChildren){
			Node child = childNodes.item(i);
			if(child.getNodeType() == Node.ELEMENT_NODE) hasElementChildren = true;
			++i;
		}

		return hasElementChildren;
	}

	@Override
	public void generateMetadata(){
//		String ds = DataSourceVocabulary.DataSource.getURI() +"/" + name;
//		if (!id.equals("")){
//			ds = DataSourceVocabulary.DataSource.getURI() +"/" + id;
//			G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_ID.getURI(), id);
//		}
//		G_target.addTriple( ds , RDF.type,  DataSourceVocabulary.DataSource.getURI() );
//		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
//		G_target.addTripleLiteral( ds , RDFS.label,  name );
//
//		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.CSV.val());
//		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
	}

	@Override
	public Graph bootstrapSchema() {
		return bootstrapSchema(false);
	}

	@Override
	public BootstrapResult bootstrapDataset(Dataset dataset) {
		bootstrapSchema();
		return new BootstrapResult(G_target, wrapper);
	}

}

