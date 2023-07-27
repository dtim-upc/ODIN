

export function useGeometry() {


    const getCircleOutlinePoint = (d, isSource) => {
        // Total difference in x and y from source to target
        var deltaX = d.target.x - d.source.x;
        var deltaY = d.target.y - d.source.y;

        // Pythagoras rule
        // Length of path from center of source node to center of target node
        var totalLength = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

        var radiusS = d.source.radius;
            // x and y distances from center to outside edge of target node
            var offsetSX = (deltaX * (radiusS)) / totalLength;
            var offsetSY = (deltaY * (radiusS)) / totalLength;

        if(isSource){
            return {x: (d.source.x + offsetSX), y: (d.source.y + offsetSY)};
        }
        var radius = d.target.radius;
        // x and y distances from center to outside edge of target node
        var offsetX = (deltaX * (radius)) / totalLength;
        var offsetY = (deltaY * (radius)) / totalLength;

        return { source:{x: (d.source.x + offsetSX), y: (d.source.y + offsetSY)},
         target: {x: (d.target.x - offsetX), y: (d.target.y - offsetY)} };
    }


    // for loop lines. Function not tested
    const getAnotherCircleOutlinePoint = (d, a) => {
        var deltaX = (d.target.x - d.source.x);
        var deltaY = (d.target.y - d.source.y);
    
        // the angle of direct connection towards intermediate node
        var angleToLabel = Math.atan2(deltaY, deltaX);
    
        // calculate new angle
        var deltaAngle = (a * (Math.PI / 6));
        var angle = angleToLabel + deltaAngle;
    
        var radius = d.target.radius;
    
        // calculate coordinates on circumference
        var x = d.target.x + (radius * Math.cos(angle));
        var y = d.target.y + (radius * Math.sin(angle));
    
        // return the point on the circle circumference
        return {x: x, y: y};
    }
        

    const getRectOutlinePoint = (d) => {

        var m = (d.target.y - d.source.y) / (d.target.x - d.source.x);

        var boxWidth = d.target.width

        var minX = d.target.x - (boxWidth / 2);
        var maxX = d.target.x + (boxWidth / 2);
    
        var minY = d.target.y - (d.target.height / 2);
        var maxY = d.target.y + (d.target.height / 2);

        // left side
        if (d.source.x < d.target.x) {
            var minXy = m * (minX - d.source.x) + d.source.y;
            if (minY < minXy && minXy < maxY) {
            return {x: minX, y: minXy};
            }
        }
    
        // right side
        if (d.source.x >= d.target.x) {
            var maxXy = m * (maxX - d.source.x) + d.source.y;
            if (minY < maxXy && maxXy < maxY) {
            return {x: maxX, y: maxXy};
            }
        }
    
        // top side
        if (d.source.y < d.target.y) {
            var minYx = (minY - d.source.y) / m + d.source.x;
            if (minX < minYx && minYx < maxX) {
            return {x: minYx, y: minY};
            }
        }
    
        // bottom side
        if (d.source.y >= d.target.y) {
            var maxYx = (maxY - d.source.y) / m + d.source.x;
            if (minX < maxYx && maxYx < maxX) {
            return {x: maxYx, y: maxY};
            }
        }


    }

    return{
        getCircleOutlinePoint,
        getAnotherCircleOutlinePoint,
        getRectOutlinePoint

    }
    





}