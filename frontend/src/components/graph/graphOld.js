<template>

    <!-- Graph view -->
 
            <div ref="graphParent" class="col-10"  :class="$q.dark.isActive ? 'graph--dark': 'graph--light'">
                <div ref="graphDiv" id="graph"> 
                </div>
            </div>
            <button @click="zoomIn()">Zoom in</button>
            <button @click="zoomOut()">Zoom out</button>
            <!-- <button @click="resetZoom()">Reset zoom</button> -->
            <button @click="center()">Center</button>

   
</template>

<script setup>
import { onMounted, ref } from '@vue/runtime-core'
import * as d3 from "d3";
import { useGraphUtils } from 'src/use/useGraphUtils.js'

const graphDiv = ref(null); 
const graphParent = ref(null);

let simulation = null;

const { getLabel, setClass, calcPropertyBoxWidth, defaultPropertyHeight, defaultRadius, drag} = useGraphUtils();

const nodes = [
    {"id":"0", "type": "rdfs:Class", "label":"Country"},
     {"id":"1", "type":"rdfs:Class", "label":"lbl"}, 
     {"id":"2", "type":"rdfs:Class", "label":"health"},
     {"id":"3", "type":"object:property", "label":"string"}
     ];
const links = [  {"source":"0", "target":"1","label":"name"},
                    {"source":"1", "target":"2","label":"another link"},
                     {"source":"2", "target":"3","label":"has_name"}
]


let svg = null;
let zoom = null;
let width = null;
let height = null;

  // **


    const zoomIn = () => {
console.log("zooming in...")
   
        svg.transition()
		.call(zoom.scaleBy, 2);
    }

    const zoomOut = () => {
	svg.transition()
		.call(zoom.scaleBy, 0.5);
    }

const center = () => {
    console.log("hola");
	svg.transition().call(zoom.translateTo, 0.5 * width, 0.5 * height);
    svg.transition().call(zoom.scaleTo, 1);
}

    // **


onMounted( () => {

    // todo: update values when resize window and draw graph again
     width = graphParent.value.clientWidth;
     height = graphParent.value.clientHeight;

     svg = d3.select(graphDiv.value).append('svg').attr("width", width).attr("height", height);


    simulation = d3.forceSimulation()
     .force("charge", d3.forceManyBody().strength(-500)) // push nodes apart to space them out
     .force("collide", d3.forceCollide().radius(20)) // add some collision detection so they don't overlap
    .force("collide",d3.forceCollide().radius(function(d) { return d.r * 20; }).iterations(10).strength(1))
    .force("center", d3.forceCenter(width / 2, height / 2)) // and draw them around the centre of the space
    .force("link", d3.forceLink().id(d => d.id).distance(300).strength(1.5)); // specifies that id is the link variable
    
    

    simulation.nodes(nodes)
    simulation.force("link").links(links)


    let root = svg.append('g');  
    

    let handleZoom = (e) => {
        root.attr('transform', e.transform);
    }

    zoom = d3.zoom().on('zoom', handleZoom)
    .scaleExtent([1, 5])
    .translateExtent([[0, 0], [width, height]]);

    svg.call(zoom);


     var marker = root.selectAll("marker")
        .append("svg:defs")
        .data(["end-arrow"])
        .enter()
        .append("svg:marker")
        .attr("id", String)
        .attr("viewBox", "0 -3 6 6")
        .attr("refX", 11.3)
        .attr("refY", -0.2)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-3L6,0L0,3");


    // Last container -> elements of this container OVERLAP others
    // .classed add a class - .attr('class','') will override all classes
    const linkContainer = root.append('g').classed('linkContainer', true);
    const labelContainer = root.append("g").classed("labelContainer", true);
    const labelContainer2 = root.append("g").classed("labelContainer2", true);
    const nodeContainer = root.append('g').classed('nodeContainer', true);


// adjust size on resize -> redraw

let glinks = linkContainer.selectAll(".link")
    .data(links)
    .enter()
    .append("svg:path")
    .attr("id", function(d, i) {return "link_" + i;})
    .style("stroke", "black")
    .style("stroke-width", 2)
    .style("fill", "none")
    .attr("marker-end", "url(#end-arrow)");


    var glabels = labelContainer2.selectAll(".labelsG").data(links).enter()
                    .append("g")
                    .attr("class", "labelsG")
   
   glabels.append("rect")
    .attr('height', d => {
        d.height = defaultPropertyHeight
        return d.height
    })
    .attr('width', d => {
        d.width = calcPropertyBoxWidth(d.label)
        return d.width
        })
           .attr("x", d => -d.width/2)
    .attr("y", d => -d.height/2)
    .style('fill', "red") ;  

    glabels.append("text")
    .style("text-anchor", "middle")
    // .style("dominant-baseline", "central")
    .attr("class", "shadow")
    .attr("dy", ".35em")
    .text( d => getLabel(d.label));


//   var link_label_rect = labelContainer.selectAll(".link_label_rect")
//     .data(links)
//     .enter()
//     .append("rect")
//     .attr('height', d => {
//         d.height = defaultPropertyHeight
//         return d.height
//     })
//     .attr('width', d => {
//         d.width = calcPropertyBoxWidth(d.label)
//         return d.width
//         })
//     .style('fill', "red") ;  

//   var link_label = labelContainer.selectAll(".link_label")
//     .data(links)
//     .enter()
//     .append("text")
//     .style("text-anchor", "middle")
//     .style("dominant-baseline", "central")
//     .attr("class", "shadow")
//     .text( d => getLabel(d.label));
   

   let gnodes = nodeContainer.selectAll(".node")
    .data(simulation.nodes())
    .enter()
    .append("svg:g")
    .attr("class", "node")
    .attr('id', (d) => d.id)
    .call(drag(simulation));
    
    //  .call(d3.drag()
    //         .on("start", dragstarted)
    //         .on("drag", dragged)
    //         .on("end", dragended));



  gnodes.append("svg:circle")
    // .attr("r", 10)
    // .style("fill", "black");
    // width * 0.03
    .attr('class', setClass)
         .attr('r', width * 0.03) 
         
 

    gnodes.append("text")
    .attr("text-anchor", "middle")
    .text(d => d.label)




        
        simulation.on("tick",function(d){

        // //      link.attr('d', function (d) {
        // //      return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y;
        // //  });


        glinks.attr("d", function(d) {
       var dx = d.target.x - d.source.x;
       var dy = d.target.y - d.source.y;
       var dr = Math.sqrt(dx * dx + dy * dy);

       return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
     });

    // link_label.attr("x", function(d, i) {
    //     var pathLength = d3.select("#link_" + i).node().getTotalLength();
    //     d.point = d3.select("#link_" + i).node().getPointAtLength(pathLength / 2);
    //     return d.point.x
    //   })
    //   .attr("y", function(d) {
    //     return d.point.y
    //   })
    // gnodes.attr("transform", function(d) {
    //   return ("translate(" + d.x + "," + d.y + ")");
    // });

    // link_label_rect.attr("x", function(d, i) {
    //     var pathLength = d3.select("#link_" + i).node().getTotalLength();
    //     d.point = d3.select("#link_" + i).node().getPointAtLength(pathLength / 2);
    //     // var w = d.width/2
    //     // console.log(w);
    //     // console.log(d.point.x);
    //     return d.point.x - d.width/2
    //   })
    //   .attr("y", function(d) {
    //     return d.point.y - d.height/2
    //   })


    glabels.attr("transform", (d,i) => {

        var pathLength = d3.select("#link_" + i).node().getTotalLength();
        d.point = d3.select("#link_" + i).node().getPointAtLength(pathLength / 2);
        return ("translate(" + d.point.x + "," + d.point.y + ")");


    });

    gnodes.attr("transform", function(d) {
      return ("translate(" + d.x + "," + d.y + ")");
    });


})
simulation.alpha(1).restart()


        } );



</script>
<style lang="scss">

$bg-color:  $neutral100;
$dot-color: #212134;
// Colors
$bg-colord: hsl(256,33,10);
$dot-colord: hsl(256,33,70);

// Dimensions
$dot-size: 1px;
$dot-space: 22px;

.link{
    stroke:#777;
    stroke-width: 2px;
}

.graph--dark{


    #graph {
	background:
		linear-gradient(90deg, $bg-colord ($dot-space - $dot-size), transparent 1%) center,
		linear-gradient($bg-colord ($dot-space - $dot-size), transparent 1%) center,
		$dot-colord;
	background-size: $dot-space $dot-space;
   }


.node{
    fill: #ccc;
    stroke: #fff;
    stroke-width: 2px;

}



.rdfs-class{
    fill: #7B79FF;
       stroke: #fff;
    stroke-width: 2px;
}


}

.graph--light{


    #graph {
        background:
            linear-gradient(90deg, $bg-color ($dot-space - $dot-size), transparent 1%) center,
            linear-gradient($bg-color ($dot-space - $dot-size), transparent 1%) center,
            $dot-color;
        background-size: $dot-space $dot-space;
    }

    .rdfs-class{
    fill: #D9D8FF;
       stroke: #7B79FF;
    stroke-width: 2px;
    }




}


// pending of dark 





</style>