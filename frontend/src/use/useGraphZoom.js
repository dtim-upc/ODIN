import * as d3 from "d3";
import { isRef, watch } from '@vue/runtime-core'

/*
* All parameters are vue reactive 
*/
export function useGraphZoom(svg, root, fullWidth, fullHeight /*, zoomEnabled*/) {

   


    let zoom = null;
  const zoomIn = () => {
       
            svg.value.transition()
            .call(zoom.scaleBy, 1.5);
        }
    
        const zoomOut = () => {
        svg.value.transition()
            .call(zoom.scaleBy, 0.2);
        }
    
    const center = (time) => {
        zoomToFit(time)
        // svg.value.transition().call(zoom.translateTo, 0.5 * fullWidth.value, 0.5 * fullHeight.value);
        // svg.value.transition().call(zoom.scaleTo, 1);
    }


    /*
    * Should be initialized once root is created
    */
    const initZoom = () => {

        // console.log("initZoom")
        // console.log(svg)
        // console.log(root)
        let handleZoom = (e) => {
            root.value.attr('transform', e.transform);
        }
    
        zoom = d3.zoom().on('zoom', handleZoom)
        .scaleExtent([0.01, 5])
        // .translateExtent([[0, 0], [fullWidth.value, fullHeight.value]]);
    
        svg.value.call(zoom);
    

    }

    const disableZoom = (flag) => {
        console.log("disable Zoom", flag)
         if(svg.value){
            if (!flag) {
                console.log("enable zoom")
                svg.value.call(zoom);
            } else {
                console.log("disable zoom")
                svg.value.on('.zoom', null);
            }
         } else {
            console.log("no svg defined...disableZoom")
         }
       
    }



    const zoomToFit = (time) => {

        var bounds = root.value.node().getBBox();
        // console.log("zoomtofit")
        // console.log(bounds)
        var width = bounds.width,
          height = bounds.height;
        var midX = bounds.x + width / 2,
          midY = bounds.y + height / 2;
          
        if (width == 0 || height == 0) return; // nothing to fit
        var scale = (.9) / Math.max(width / fullWidth.value, height / fullHeight.value);
        var translate = [fullWidth.value / 2 - scale * midX, fullHeight.value / 2 - scale * midY];
    

         var transform = d3.zoomIdentity
           .translate(translate[0], translate[1])
           .scale(scale);


        if(time == 0){
            svg.value.transition().call(zoom.transform,transform);
        } else{ 
            svg.value.transition().duration(750).call(zoom.transform,transform);
        }

      }

    // if(isRef(zoomEnabled)){
    //     watch( zoomEnabled, (first, second) => {
    //         console.log("toggle zoom for: ")
    //         disableZoom(zoomEnabled.value)
    //     })
    // } else {
    //     console.log("setting zoom to: ", zoomEnabled)
    //     disableZoom(zoomEnabled)
    // }

    



    return {
        zoomIn,
        zoomOut,
        center,
        initZoom,
        disableZoom
    }


}