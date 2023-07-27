import * as d3 from "d3";
// https://github.com/pcbje/ggraph/blob/master/src/marker/marker.js

export function useLazzo() {


    var poly;
    var points = [];
    var isEnabled = false;
    var releaseFunct;
    var resetFunct;
    var max_radius = 0;
    var selected = {};
    var callbacks = [];
    var state = {select: true, started: false}
  
  
    // https://github.com/Jam3/chaikin-smooth
    const smooth = function(input, output) {
      if (!Array.isArray(output))
          output = []
  
      if (input.length>0 && input[0]) {
          output.push([input[0][0], input[0][1]])
      }
      for (var i=0; i<input.length-1; i++) {
          var p0 = input[i]
          var p1 = input[i+1]
          var p0x = p0[0],
              p0y = p0[1],
              p1x = p1[0],
              p1y = p1[1]
  
          var Q = [ 0.75 * p0x + 0.25 * p1x, 0.75 * p0y + 0.25 * p1y ]
          var R = [ 0.25 * p0x + 0.75 * p1x, 0.25 * p0y + 0.75 * p1y ]
          output.push(Q)
          output.push(R)
      }
      if (input.length > 1) {
        output.push([input[input.length-1][0], input[input.length-1][1]])
  
      }
      return output
    }
  
    const enabled = (p) => {
        if (arguments.length) isEnabled = p;
        return isEnabled;
    }

    const contains = function(point) {
        var vs = points;
        var x = point[0], y = point[1];
        var inside = false;
        for (var i = 0, j = vs.length - 1; i < vs.length; j = i++) {
            var xi = vs[i][0], yi = vs[i][1];
            var xj = vs[j][0], yj = vs[j][1];
            var intersect = ((yi > y) != (yj > y))
                && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    };
  
    const to_str = function(points, skip, do_smooth) {
      var arr = [];
  
      var x = [];
      points.map(function(a,i ) {
        if (i % skip == 0) x.push(a)
      });
  
      x.push(points[points.length - 1]);
  
      if (do_smooth) {
        x = smooth(x);
      }
      x.map(function(point) {
        arr.push(point[0] + ',' + point[1])
      })
  
      return arr.join(' ')
    }
  
    // maybe changed for the function 
    const marked = function(callback) {
      callbacks.push(callback);
    };

    const afterMarked = function(f){
        releaseFunct =f;
    };

    const resetFunction = function(f){
      resetFunct = f;
    }
  
    const call = function(clear) {
      callbacks.map(function(callback) {
        callback(clear);
      })
    }

    
  
    // background is svg
    // width and height of svg...
    var initSelection = function( root ,background) {

      var container = root.append("g").classed("markerContainer", true);

      poly = container.append('polygon').attr('class', 'polygonMarker')
      state.started = false;
      points = [];
  
      background.on('mousedown', (event)  =>{
        console.log("mousedow....")
        if (!state.select) return;
        call(!event.shiftKey);
        state.started = true;
        
      });
  
      var up = function(event) {

        if(releaseFunct)
          releaseFunct(true)

        console.log("up()...")
        state.started = false;
        points = [];
        poly.attr('points', to_str(points, 4, true));
      }
  
      document.addEventListener('mouseup', up);
      background.on('mouseup', up);
  
      background.on('mousemove', function(event) {
        // console.log("moving...")
        if (!state.started) return;
        var pos = d3.pointer(event, background.select("g").node()); //background.select("g").node()
        // var pos= d3.mouse(background.select("g").node());
        var x = pos[0];
        var y = pos[1];
        // console.log("x: "+x+" y: "+y)
        points.push([x, y])
        poly.attr('points', to_str(points, 4, true));

        call();
      });
    }

    const removeEvents = (root ,background) => {

        background.on('mousedown',null)
        background.on('mouseup',null)
        background.on('mousemove',null)
        document.addEventListener('mouseup', null);

    }
  




    return {
        enabled,
        contains,
        marked,
        initSelection,
        afterMarked,
        removeEvents


    //   init: init,
    //   marked: register,
    //   contains: is_in,
    //   state: state,
    //   get_mouse_pos: get_mouse_pos,
    //   points: points,
    //   to_str: to_str,
    //   set: function(points) {
    //     points = points;
    //     call(true);
    //   }
    }




}
