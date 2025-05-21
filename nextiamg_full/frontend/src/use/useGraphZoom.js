import * as d3 from "d3";

export function useGraphZoom(svg, root, fullWidth, fullHeight) {
  let zoom = null;
  const zoomIn = () => {
    svg.value.transition().call(zoom.scaleBy, 1.5);
  }
    
  const zoomOut = () => {
    svg.value.transition().call(zoom.scaleBy, 0.2);
  }
    
  const center = (time) => {
    zoomToFit(time)
  }

  // Should be initialized once root is created
  const initZoom = () => {
    let handleZoom = (e) => {
      root.value.attr('transform', e.transform)
    }
    zoom = d3.zoom().on('zoom', handleZoom).scaleExtent([0.01, 5])
    svg.value.call(zoom)
  }

  const disableZoom = (flag) => {
    if (svg.value){
      if (!flag) {
        svg.value.call(zoom);
      } else {
        svg.value.on('.zoom', null);
      }
    }
  }

  const zoomToFit = (time) => {
    var bounds = root.value.node().getBBox();
    var width = bounds.width,
    height = bounds.height;
    var midX = bounds.x + width / 2,
    midY = bounds.y + height / 2;
      
    if (width == 0 || height == 0) return; // nothing to fit
    var scale = (.9) / Math.max(width / fullWidth.value, height / fullHeight.value);
    var translate = [fullWidth.value / 2 - scale * midX, fullHeight.value / 2 - scale * midY];


    var transform = d3.zoomIdentity.translate(translate[0], translate[1]).scale(scale);

    if (time == 0) {
        svg.value.transition().call(zoom.transform,transform);
    } else { 
        svg.value.transition().duration(750).call(zoom.transform,transform);
    }
  }

  return {
    zoomIn,
    zoomOut,
    center,
    initZoom,
    disableZoom
  }
}