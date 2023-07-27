import * as d3 from "d3";

export function useGraphUtils() {


    const maxNameLength = 14; //15
    const defaultPropertyHeight = 20;

    // maybe delete or update default radius value
    var defaultRadius = 50;

    const labelFromURI = (uri) => {

        var uriLabel = '';
        if (uri !== undefined && uri !== '') {
            var lastSlash = uri.lastIndexOf('/');
            var lastHash = uri.lastIndexOf('#');
            uriLabel = uri.substr(Math.max(lastSlash, lastHash) + 1).replace(/\_/g, ' ');
        }
        return uriLabel;
    };

    const getLabel = (name) => {

        var label = '';
        if (name !== undefined && name !== '') {
            label = name;
        } else {
            label = that.labelFromURI(obj.uri);
        }

        if (label.length > maxNameLength) {
            label = label.substring(0, maxNameLength-2) + '...';
        }

        return label;

    };


    const setClass = d => {
        if(d.type == 'rdfs:Class')
            return 'rdfs-class'
        return 'rdfs-class'    
    }


    const measureTextWidth = ( text, textStyle ) => {
        // Set a default value
        if ( !textStyle ) {
          textStyle = "text";
        }
        var d = d3.select("body")
            .append("div")
            .attr("class", textStyle)
            .attr("id", "width-test") // tag this element to identify it
            .attr("style", "position:absolute; float:left; white-space:nowrap; visibility:hidden;")
            .text(text),
          w = document.getElementById("width-test").offsetWidth;
        d.remove();
        return w;
      }


    // TODO: change *8 to a more stable width for labels
    const calcPropertyBoxWidth = name => measureTextWidth(getLabel(name) + 20) //getLabel(name).length * 8;



const drag = simulation => {


    const dragstarted = (event) => {
      if (!event.active) simulation.alphaTarget(0.3).restart();
      event.subject.fx = event.subject.x;
      event.subject.fy = event.subject.y;
    }

    const dragged = (event) => {
      // simulation.restart()
      event.subject.fx = event.x;
      event.subject.fy = event.y;
    }
    
    const dragended = (event) => {
      // console.log("dragended")
      if (!event.active) {
        simulation.alphaTarget(0);
        // console.log("alpha to 0")
      }
      // console.log("dragended....",event)
      event.subject.fx = null;
      event.subject.fy = null;
    }

    return d3.drag().on("start", dragstarted)
      .on("drag", dragged)
      .on("end", dragended);

}

const isGConvex = (vertex, edges) => {
  let componentCount = 0;
  if(vertex.length==0){
    return false;
  }
  componentCount = 1;
        var nodes = [];
        //construct adjacency list of graph
        var adjList = {};
        vertex.forEach(function(v){
            var n = new Object();
            n.id = v;
            n.visited = false;
            nodes[n.id]=n;
            adjList[v]=[];
        });

      edges.forEach(function(e){
          adjList[e.source.id].push(nodes[e.target.id]);
          adjList[e.target.id].push(nodes[e.source.id]);
      });

      //perform DFS on nodes
      var q = [];
      q.push(nodes[Object.keys(nodes)[0]]);

      while(q.length>0){

          var v1 = q.shift();
          var adj = adjList[v1.id];

          for(var i=0; i<adj.length; i++){
              var v2 = adj[i];
              if(v2.visited)
                  continue;
              q.push(v2);
          }

          v1.visited = true;
          //check for unvisited nodes
          if(q.length==0){
              for(var key in nodes){
                  if(!nodes[key].visited){
                      q.push(nodes[key]);
                      componentCount++;
                      break;
                  }
              }
          }
      }
      console.log("components "+componentCount )
      if (componentCount == 1)
          return true
      return false;

}

    return {
        labelFromURI,
        getLabel,
        setClass,
        calcPropertyBoxWidth,
        defaultPropertyHeight,
        defaultRadius,
        drag,
        isGConvex
    }


}