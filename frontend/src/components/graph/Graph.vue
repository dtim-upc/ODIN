<template>

    <!-- Graph view -->
 <!-- <div class="row items-stretch"> -->

 <div ref="graphParent" class="col"  :class="$q.dark.isActive ? 'graph--dark': 'graph--light'" style="position:relative; height:100%">
                <div ref="graphDiv" id="graph" style="line-height: 0; width:100%; height:100%;" > 
                </div>

                   
                   <!-- <div padding> -->
                   
                   
     <q-btn-group spread style="flex-direction: column;position:absolute;" class="fixed-bottom-right q-ma-md"  >
        <q-btn v-if="props.enableQuery"  color="white" text-color="black"  icon="o_search" @click="querySelection" style="padding: 4px 8px"/>
      <q-btn v-if="props.enableSelection"  color="white" text-color="black"  icon="o_highlight_alt" @click="enableSelection" style="padding: 4px 8px"/>
      <q-btn color="white" text-color="black"  :icon="$q.fullscreen.isActive ? 'fullscreen_exit' : 'fullscreen'" @click="toggleFullscreen" style="padding: 4px 8px"/>
      <q-btn color="white" text-color="black"  icon="filter_center_focus" @click="center" style="padding: 4px 8px"/>
      <q-btn color="white" text-color="black" icon="add" @click="zoomIn" style="padding: 4px 8px"/>
      <q-btn color="white" text-color="black" icon="remove" @click="zoomOut" style="padding: 4px 8px"/>
    </q-btn-group>
                   
       <!-- </div> -->
                   <!--  -->


     <q-resize-observer @resize="onResize" /> 
</div>


   
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from '@vue/runtime-core'
import * as d3 from "d3";
import { useGraphUtils } from 'src/use/useGraphUtils.js'
import { useGraphZoom } from 'src/use/useGraphZoom.js'
import { useLazzo } from 'src/use/useLazzo.js'
import {useGeometry} from 'src/use/useGeometry.js'
import {useSelectionGraph} from 'src/use/useSelectionGraph.js'
import { useQuasar } from 'quasar'
import {useNotify} from 'src/use/useNotify.js'

const props = defineProps({
    graphical: {type: String},
    enableQuery: {type:Boolean, default: false},
    enableSelection: {type:Boolean, default: false},
    zoomDragEnable: {type: Boolean, default:false},

    centerGraphonLoad: {type:Boolean, default:true},
    enableClickR: { type:Boolean, default: false  },

    queryFunc : { type: Function, default(){return 'default funtion'}  },

    alignment: { type:Object, default:{

    type : '', //both resource must be same type
    trueType: '',
    shortType: '',
    resourceA : {
        name:'',
        label: '',
        iri: '',
    },
    resourceB: {
        name:'',
        label: '',
        iri: '',
    }
} }
});
const emit = defineEmits(["elementClick"])
// ----------------------------------------------------------------------------
//                  Data set up
// ----------------------------------------------------------------------------
let json = !props.graphical || props.graphical == "" ? {"nodes":[], "links":[]}: JSON.parse(props.graphical)
let graphicalNodes = json.nodes
let graphicalLinks = json.links

watch( () => props.graphical, n  => {

    if(props.graphical || props.graphical == "") {
        console.log("entra props actualizado")
        // console.log(props.graphical)
        json = props.graphical == ""? {"nodes":[], "links":[]}:JSON.parse(props.graphical);
        graphicalNodes = json.nodes
        graphicalLinks = json.links
        // console.log("update json")
        cleanVisualGraph()
        initVisualGraph()
    } 
   
})

// ----------------------------------------------------------------------------
//                  HTML set up
// ----------------------------------------------------------------------------
const graphDiv = ref(null); 
const graphParent = ref(null);
let svg = ref(null);
let root = ref(null);
// ----------------------------------------------------------------------------
//                  Graph set up variables
// ----------------------------------------------------------------------------

let simulation = null;

let width = ref(null);
let height = ref(null);
let linkContainer = null;
let nodeContainer = null;


const notify = useNotify()
// ----------------------------------------------------------------------------
// 
//                  Buttons functionalities
// 
// ----------------------------------------------------------------------------
const $q = useQuasar()

const toggleFullscreen = (e) => {
        $q.fullscreen.toggle(graphParent.value)
          .then(() => {
            // success!
          })
          .catch((err) => {
            alert(err)
            // uh, oh, error!!
            // console.error(err)
          })
}

const enableSelection = () => {

    // we disable zoom since overlap with the drag functionality of selection
    disableZoom(true);
    resetSelection();
    // if(props.selectSubGraph)
    console.log("width:",width.value)
    lazzo.initSelection(root.value,svg.value) 
    lazzo.marked(selectMarked)
    lazzo.afterMarked(afterMarked)
}

const querySelection = () => {
    console.log("query...",Object.keys(selectionG.getSelected()))
    if( Object.keys(selectionG.getSelected()).length == 0 ){
        notify.negative("You must perform a selection over the graph before querying")
    } else {
        
        var data = selectionG.prepareSelectionObject();
        props.queryFunc(data)
    }
    
}

// ----------------------------------------------------------------------------
// 
//                  OTHERS
// 
// ----------------------------------------------------------------------------
// const zoomDragEnable  = ref(true) 


let centerGraphView = props.centerGraphonLoad;

const clickResource= (event, node) =>{

     var clickElement = {}
            clickElement.iri = node.iri
            clickElement.id = node.id
            // clickElement.type = node.iriType

            clickElement.type = node.type
            clickElement.shortType = node.shortType
            clickElement.trueType = node.iriType

            clickElement.label = node.label
            clickElement.event = event

            // console.log("node....", node)
            emit('elementClick', clickElement)

}
const onResize =  (size) => {
        // report.value = size
        // console.log(size)

         if(width.value != size.width || height.value != size.height) {

            width.value = size.width
            height.value = size.height
            if(!centerGraphView) // to avoid centering when centering at graph view since it's done later
                center(0)
         }
         
      }


const { getLabel, setClass, calcPropertyBoxWidth, defaultPropertyHeight, defaultRadius, drag, isGConvex} = useGraphUtils();
const { zoomIn, zoomOut,center, initZoom,disableZoom  } = useGraphZoom(svg, root, width, height);
const lazzo = useLazzo();
const geometry = useGeometry();
const selectionG = useSelectionGraph();

const cleanVisualGraph = () => {

simulation.stop()
    svg.value.remove()
    
    svg.value = d3.select(graphDiv.value).append('svg')
    // .attr("width", width.value).attr("height", height.value);

}

const resetSelection = () => {
    svg.value.selectAll(".link").style("opacity", "0.3");
    svg.value.selectAll(".node").style("opacity", "0.3");
    selectionG.clearSelection()
}

const afterMarked = () => {
    console.log("after...")
    if( Object.keys(selectionG.getSelected()).length > 1 &&  !isGConvex(Object.keys(selectionG.getSelected()),selectionG.getLinks()) ){
        notify.negative("The query graph must be convex")
        // reset opacity
        resetSelection()
    } 
    lazzo.removeEvents(root.value,svg.value)
    disableZoom(false);
}

const selectMarked = (node) => {
    // console.log("selectedMarked...")
    // simulation.stop
    svg.value.selectAll(".class,.type").each( function (node) {
      
        
        if (!selectionG.contains(node)) {
            var point = [node.x, node.y];
            // console.log("node point...", node)
            // console.log("node inside...", node)
                // console.log(node.getCTM())
            if (lazzo.contains(point)) {
                // console.log("node inside...", node)
                selectionG.addNode(node);
                
                svg.value.select("#" + node.id).style("opacity", "1");
                if( Object.keys(selectionG.getSelected()).length > 1) {

                    var nodesId = Object.keys(selectionG.getSelected());
                    nodesId.forEach(function (id)  {
                        svg.value.select("#" + id).style("opacity", "1");
                    });
                    var labs = [];
                    svg.value.selectAll(".link").each(function (link) {
                        var domain = link.source.id;
                        var range =link.target.id;
                        if(nodesId.includes(domain) && nodesId.includes(range)){
                            //path
                            svg.value.select("#" + link.id).style("opacity", "1");
                            //rect
                            let linkN = svg.value.select("#" + link.nodeId)
                            linkN.style("opacity", "1"); 

                            // console.log("dpks",linkN.data()[0])
                            labs.push(link);
                            selectionG.addLinkNode(linkN.data()[0])
                        }
                    })
                    selectionG.setLinks(labs)
                }
            }
        }

    } )

}



const initVisualGraph = () => {

    //set the repel force - may need to be tweaked for multiple data
    //the lower the strength the more they will repel away from each other
    //the larger the distance, the more apart they will be
    var repelForce = d3.forceManyBody().strength(-500).distanceMax(600).distanceMin(120); //450


// use if node disappear when draggin  -> https://stackoverflow.com/questions/45297356/d3-js-v4-force-layout-with-link-forces-causes-strange-movement-when-using-drag-b
//  .force("collide", d3.forceCollide(20).radius(20).strength(0))


    simulation = d3.forceSimulation()
           .force("charge", d3.forceManyBody().strength(-500)) // push nodes apart to space them out
        // .force("charge", (d) => {
        //     var charge = -500;
        //     console.log(d)
        //     if(d.type === 'class' ) {
        //         console.log("entering....")
        //         charge = 10 * charge
        //     }
        //     return charge;
        // })
        .force("collide", d3.forceCollide().radius(100)) // add some collision detection so they don't overlap
         .force("repelForce",repelForce)
        // .force("collide",d3.forceCollide().radius( d => {console.log(d.r * 20); d.r * 20} ).iterations(10).strength(0.01)) //s 1
         .force("center", d3.forceCenter(width.value / 2, height.value / 2)) // and draw them around the centre of the space
         .force("link", d3.forceLink().id(d => d.id)
         .distance(d => {
            let visibleLinkDistance = 130 
            if(d.target.type === "class"){
                visibleLinkDistance = 300  // 200 for classes
            }
            // if(d.source.radius) linkPartDistance += d.source.radius this does not enter? :'v
            //  visibleLinkDistance += 100
            // linkPartDistance += d.target.radius
            return visibleLinkDistance;
         }).strength(1)) // s 1.5 // specifies that id is the link variable
        // to attract nodes to center
        .force('centerX', d3.forceX(width.value / 2))
        .force('centerY', d3.forceY(height.value / 2))

    
    simulation.nodes(graphicalNodes)
    simulation.force("link").links(graphicalLinks)


    root.value = svg.value.append('g'); 
    
    initZoom();
    // disableZoom(false);
    // disableDragZoom(true)

     var marker = root.value.selectAll("marker")
        .append("svg:defs")
        .data(["end-arrow"])
        .enter()
        .append("svg:marker")
        .attr("id", String)
        .attr("viewBox", "0 -3 6 6")
        .attr("refX", 5)
        // .attr("refY", -0.2)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-3L6,0L0,3");


    // Last container -> elements of this container OVERLAP others
    linkContainer = root.value.append('g').classed('linkContainer', true);
    // const labelContainer = root.append("g").classed("labelContainer", true);
    // const labelContainer2 = root.append("g").classed("labelContainer2", true);
    nodeContainer = root.value.append('g').classed('nodeContainer', true);

    // if(props.selectSubGraph)
    //     lazzo.initSelection(root.value,svg.value, width.value*-1, height.value*-1) 

let glinks = linkContainer.selectAll(".link")
    .data(graphicalLinks)
    .enter()
    .append("svg:path")
    .classed("link", true)
    .attr("id", d =>  d.id)
    .style("stroke", "black")
    .style("stroke-width", 2)
    .style("fill", "none")
    .attr("marker-end", "url(#end-arrow)");
   

   let gnodes = nodeContainer.selectAll(".node")
    .data(simulation.nodes())
    .enter()
    .append("svg:g")
    .classed("node", true)
    .classed('class', d => d.type === 'class' )
    .classed("subclassof", d => d.iri === 'http://www.w3.org/2000/01/rdf-schema#subClassOf')
    .classed('integratedClass',  d => d.type === 'class' && d.isIntegrated )
    .classed('integratedDatatypeProperty', d => d.type === 'datatype' &&  d.isIntegrated )
    .classed('integratedObjectProperty', d => d.type === 'object' && d.isIntegrated )
    .classed('objectProperty', d => d.type === 'object' )
    .classed('datatypeProperty', d => d.type === 'datatype')
    .classed('type', d => d.type === 'xsdType')
    .attr('id', (d) => d.id)

    

  let gclasses =  nodeContainer.selectAll('.class')
  let gobjectProperties =  nodeContainer.selectAll('.objectProperty')
  let gdatatypeProperties =  nodeContainer.selectAll('.datatypeProperty')
  let gproperties = nodeContainer.selectAll('.objectProperty, .datatypeProperty')
  let gtypes =  nodeContainer.selectAll('.type')
  let gClassesAndTypes = nodeContainer.selectAll('.class,.type')

const setFocus =( element, d )=> {

    let focusedElement = nodeContainer.selectAll('.focused')
    let d3element = d3.select(element)

    console.log("***", props.alignment)
    console.log("***", d)

    if(focusedElement.size() > 0 && !d3element.classed("focused") )  {
        // there's another element selected. user needs to unselect it
    } else if( d3element.classed("focused") ) {
        // user wants to unfocused
        d3element.classed("focused", false)
        clickResource('unfocused',d)
    // } else if (props.alignment.trueType == d.iriType || props.alignment.trueType == '') {
    } else if (props.alignment.type == d.type || props.alignment.type == '') {
        d3element.classed("focused", true)
                clickResource('focused',d)
    }

    // console.log("***",nodeContainer.selectAll('.focused').size() )
    //  d3element.classed('focused', true)
    
}

// TODO: fix unfouc...
const unfocusAll = () => {
    console.log("unfocus all...")
   nodeContainer.selectAll('.focused').each(n => n.classed("focused", false))
}


watch ( () => props.alignment, n => {

if(props.alignment.type == ''){

    unfocusAll()
}

} )

    gclasses.on("click", function (event, d) {
        if(props.enableClickR){
           setFocus(this, d)
        }
    } )

    gobjectProperties.on("click", function (event, d) {
        if(props.enableClickR){
            setFocus(this, d)
        }
    } )
    gdatatypeProperties.on("click", function (event, d) {
        if(props.enableClickR){
            setFocus(this, d)
        }
    })

  gClassesAndTypes.call(drag(simulation));
   
   gclasses.append("svg:circle")
   .attr('class', setClass)
   .attr('r', d => {d.radius = defaultRadius; return d.radius})
  
   
// TODO: change
    gtypes.append("rect")
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
//     .append("svg:circle")
//    .attr('class', setClass)
//    .attr('r', width.value * 0.03) 


   gproperties.append("rect")
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
    // .style('fill', "#B8E1FF") ;  


    gnodes.append("text")
    .attr("text-anchor", "middle")
    .text(d => getLabel(d.label) )
    .attr("dy", ".35em") // this is only for properties


        simulation.on("tick",function(d){

// o.oo014


        var value = 1.0 - 10 * simulation.alpha();
        if(value > 0.009) {
            if(centerGraphView){
                // console.log("centering")
                centerGraphView = false;
                center();
            }
                
        }
        
            // console.log(simulation.alpha())

        //        glinks.attr('d', function (d) {
        //        return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y;
        //  return "M" + d.source.x + "," + d.source.y + "L" + (d.target.x - offsetX) + "," + (d.target.y - offsetY);
        
        //    });


        

        // glinks
        linkContainer.selectAll(".link").attr("d", function(d) {



        // Total difference in x and y from source to target
//    var dx = d.target.x - d.source.x;
//    var dy = d.target.y - d.source.y;

       // Length of path from center of source node to center of target node
        // var dr = Math.sqrt(dx * dx + dy * dy);
    

        // let offsetX = 0,
        //      offsetY = 0;

        // todo: improve if. use attr type to identify circle or rect
        if(d.target.radius) {   
        // x and y distances from center to outside edge of target node
            //  offsetX = (dx * d.source.radius) / dr;
            //  offsetY = (dy * d.source.radius) / dr;
            var point = geometry.getCircleOutlinePoint(d)
            // return "M" + d.source.x + "," + d.source.y + "L" + (d.target.x - offsetX) + "," + (d.target.y - offsetY);
            return "M" + point.source.x + "," + point.source.y  + "L" + point.target.x + "," + point.target.y;
            return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x  +"," + d.target.y ;

        } else if (d.target.width){
            // console.log("target is rect")
            var sourcePoint = geometry.getCircleOutlinePoint(d, true)
            var targetPoint = geometry.getRectOutlinePoint(d)
            return "M" + sourcePoint.x + "," + sourcePoint.y + "L" + targetPoint.x + "," + targetPoint.y ;  
        }   

        // return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + (d.target.x - offsetX )+ "," + (d.target.y - offsetY);
        //  return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x  +"," + d.target.y ;
        // if(d.target.type == "class")
           
        // return "M" + d.source.x + "," + d.source.y + "L" + (d.target.x - offsetX) + "," + (d.target.y - offsetY) ;    
        
        // return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x  +"," + d.target.y ;
     });


    // gproperties
    nodeContainer.selectAll('.objectProperty, .datatypeProperty').attr("transform", (d,i) => {

        
        var pathLength = svg.value.select( "#"+d.linkId).node().getTotalLength();
        d.point = svg.value.select( "#"+d.linkId).node().getPointAtLength((pathLength) / 2);
        return ("translate(" + d.point.x + "," + d.point.y + ")");


    });

    //  glabels.attr("transform", (d,i) => {

    //      var pathLength = d3.select("#link" + i).node().getTotalLength();
    //      d.point = d3.select("#link" + i).node().getPointAtLength(pathLength / 2);
    //      return ("translate(" + d.point.x + "," + d.point.y + ")");


    //  });

// gClassesAndTypes

    nodeContainer.selectAll('.class,.type').attr("transform", function(node) {
      return ("translate(" + node.x + "," + node.y + ")");
    });


})


}



onMounted( () => {

    // todo: update values when resize window and draw graph again
     width.value = graphParent.value.clientWidth;
     height.value = graphParent.value.clientHeight;

    // svg.value = d3.select(graphDiv.value).append('svg').attr("width", width.value).attr("height", height.value);
    svg.value = d3.select(graphDiv.value).append('svg')
    // .attr("viewBox", "0 0 " + width.value + " " + height.value )
        //  .attr("preserveAspectRatio", "xMidYMid meet");
    // .attr("width", "100%").attr("height", "100%");

    console.log("onmounted graph...")



     initVisualGraph()


// simulation.alpha(1).restart()

// simulation.alpha(0.1).restart()
// console.log("alpha is...")
// console.log(simulation.alpha())


// const nodes = [
//     {"id":"0", "type": "class", "label":"Country"},
//      {"id":"1", "type":"class", "label":"lbl"}, 
//      {"id":"2", "type":"class", "label":"health"},
//       {"id":"7", "type":"class", "label":"super"},
//      {"id":"3", "type":"xsdType", "label":"string"}, //xsdType

//      {"id":"4", "type":"objectProperty", "label":"name", "linkId": "link1" },
//      {"id":"5", "type":"objectProperty", "label":"another link", "linkId": "link2"}, //datatypeProperty
//      {"id":"6", "type":"datatypeProperty", "label":"has_name", "linkId": "link3"}, //datatypeProperty
//      {"id":"8", "type":"objectProperty", "label":"hams", "linkId": "link4" },
//      ];
// const links = [ {"id":"link1", "source":"0", "target":"1","label":"name"},
//                 {"id":"link2","source":"1", "target":"2","label":"another link"},
//                 {"id":"link3","source":"2", "target":"3","label":"has_name"},
//                  {"id":"link4","source":"7", "target":"2","label":"has_name"}
// ]

// const nodes = [
//     {"id":"Class1","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk.created_by","type":"objectProperty","label":"created_by","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link1"},
// {"id":"Class2","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk","type":"class","label":"msddk","domain":"null","range":"null","linkId":"null"},
// {"id":"Class3","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk.title","type":"objectProperty","label":"title","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link2"},
// {"id":"Class4","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/fd749dcb27e24c64b1c36442ce4c0c1c","type":"class","label":"fd749dcb27e24c64b1c36442ce4c0c1c","domain":"null","range":"null","linkId":"null"},
// {"id":"Class5","iri":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk.identifier","type":"objectProperty","label":"identifier","domain":"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/fd749dcb27e24c64b1c36442ce4c0c1c/msddk","range":"http://www.w3.org/2001/XMLSchema#string","linkId":"Link3"},
// {"id":"Datatype6","iri":"http://www.w3.org/2001/XMLSchema#string","type":"xsdType","label":"created_by","domain":"null","range":"null","linkId":"null"},
// {"id":"Datatype7","iri":"http://www.w3.org/2001/XMLSchema#string","type":"xsdType","label":"title","domain":"null","range":"null","linkId":"null"},
// {"id":"Datatype8","iri":"http://www.w3.org/2001/XMLSchema#string","type":"xsdType","label":"identifier","domain":"null","range":"null","linkId":"null"},
// ]

// const links = [
//     {"id":"Link1","source":"Class2","target":"Datatype6","label":"created_by"},
// {"id":"Link2","source":"Class2","target":"Datatype7","label":"title"},
// {"id":"Link3","source":"Class2","target":"Datatype8","label":"identifier"},
// ]

        } );

onUnmounted ( () => {
    console.log("unmounted")
    simulation.stop();
})
// before unmonunted?
</script>


<style lang="scss">

// https://stackoverflow.com/questions/45144700/how-to-scale-inline-svg-to-parent-containers-width-and-height-automatically
#graph svg:not(:root) {
    width:100%;
    height: 100%;
}

.polygonMarker {
	fill:green;
	fill-opacity:0.15;
	stroke-width: 1px;
	stroke: green;
	stroke-opacity: 0.4;
    stroke-dasharray: 3,3;
}

$bg-color:  $neutral100;
$dot-color: #212134;
// Colors dark
$bg-colord: hsl(256,33,10);
$dot-colord: #fff;

// Dimensions
$dot-size: 1px;
$dot-space: 22px;

g.focused circle,
g.focused rect {
    stroke: #f00 !important;
}

.objectProperty > rect {

    fill: #B8E1FF

}

.datatypeProperty > rect {
    fill: plum;
}

.type > rect{
    fill: #FACEB4;
    // fill:peachpuff;
}

.integratedClass > circle {
        // fill: #A5FFD6 !important;
        fill: #ADEDD0 !important; 
}

.subclassof > rect {
    // fill: #EAC386 !important;
    // fill: #E4EBE7 !important;
    fill: #BBA58B !important
}

#graph text{
    pointer-events: none;
    -webkit-touch-callout: none; /* iOS Safari */
    -webkit-user-select: none; /* Safari */
     -khtml-user-select: none; /* Konqueror HTML */
       -moz-user-select: none; /* Old versions of Firefox */
        -ms-user-select: none; /* Internet Explorer/Edge */
            user-select: none; /* Non-prefixed version, currently
                                  supported by Chrome, Edge, Opera and Firefox */
}

.graph--dark{


    #graph {
	background:
		linear-gradient(90deg, $bg-colord ($dot-space - $dot-size), transparent 1%) center,
		linear-gradient($bg-colord ($dot-space - $dot-size), transparent 1%) center,
		$dot-colord;
	background-size: $dot-space $dot-space;
   }


// .node{
//     fill: #ccc;
//     stroke: #fff;
//     stroke-width: 2px;

// }

    .class > circle{
        fill: #7B79FF;
        stroke: #fff;
        stroke-width: 2px;
    }

    .integratedClass > circle {
        fill: #A5FFD6;
    }
    .integratedDatatypeProperty > rect {
        fill:  #A5FFD6;
    }

    #end-arrow{
        fill: #fff;
    }

    .link {
        stroke: #fff !important;
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

    .class > circle {
    fill: #D9D8FF;
       stroke: #7B79FF;
    stroke-width: 2px;
    }

    .integratedClass > circle {
        fill: #A5FFD6;
    }
    .integratedDatatypeProperty > rect {
        fill:  #A5FFD6;
    }





}


// pending of dark 
.link{
    stroke:#777;
    stroke-width: 2px;
}




</style>
