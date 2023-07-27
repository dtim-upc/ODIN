export function useSelectionGraph() {

    let selected = {}
    let links = [];
    let linksNodes = [];

    const addNode = (node) =>{
        selected[node.id] = node;
    }
    const addLinkNode = (node) =>{
        linksNodes[node.id] = node;
    }

    const contains = (node) => {
        return node.id in selected;
    }

    const setLinks = (l) => {
        links = l;
    }
    const getLinks = () => {
        return links;
    }


    const clearSelection = () => {
        console.log("clear selection...")
        selected = {};
        links = [];
        linksNodes = [];
    }
    const getSelected = () => {
        return selected;
    }

    const prepareSelectionObject = () => {

        let classes = [];
        let properties = [];
        let data = [];
        let nodesID = [];

        // console.log("preparingselecition", selected)
        Object.keys(selected).forEach( k => {
            let node = selected[k]
            let n  = new Object();
            n.iri = node.iri
            n.id = node.id;
            n.isIntegrated =  node.isIntegrated
            n.type = node.iriType
            data.push(n);
            nodesID.push(node.id);
            if(!node.iri.includes("http://www.w3.org/2001/XMLSchema#")){
                classes.push(n)
            }

        });

        // var integrationProperties = [ Integration.IntegrationDProperty.iri, Integration.IntegrationOProperty.iri ]
        links.forEach( link => {
            console.log("link", link)
            let n  = new Object();
            // n.domain =data[nodesId.indexOf(domain)].iri;
            // n.range =data[nodesId.indexOf(range)].iri  ;
            n.domain = link.source.iri
            n.range = link.target.iri
            // might be a good idea to put iri, type info in link. 
            let linkInfo = linksNodes[link.nodeId]
            n.iri = linkInfo.iri
            n.type = linkInfo.iriType
            n.isIntegrated = linkInfo.isIntegrated
            properties.push(n);

        })

        var selectionObj  = new Object();
        selectionObj.classes = classes;
        selectionObj.properties = properties;
        // not sure if needed:?
        // selectionObj.graphIRI = graph.options().loadingModule().currentGlobalGraph().iri;
        return selectionObj;
    }


    return {
        addNode,
        addLinkNode,
        contains,
        clearSelection,
        getSelected,
        setLinks,
        getLinks,
        prepareSelectionObject
    }


}