# get Wand from https://github.com/dahlia/wand
from wand.image import Image

import os
import math

def adjust_arrow_dimensions(arrow_width, arrow_height, angle):
    arrow_ratio = 0.6
    arrow_width = arrow_width * arrow_ratio
    arrow_height = arrow_height * arrow_ratio

    # px, py is the upper right point
    px = arrow_width / 2
    py = arrow_height / 2

    # adjust to use with sin/cos
    angle += 90
    angle *= math.pi / 180.0
    
    s = math.sin(angle)
    c = math.cos(angle)
    
    def rotate_by_angle(x, y):
        # rotation of 2d point
        nx = c*x + -s*y
        ny = s*x + c*y
        return nx, ny

    new_arrow_width = 0
    new_arrow_height = 0

    points = [(-px, -py), (px, -py), (-px, py), (px, py)]
    for point in points:
        npx, npy = rotate_by_angle(*point)
        new_arrow_width = max(new_arrow_width, math.fabs(npx))
        new_arrow_height = max(new_arrow_height, math.fabs(npy))


    return int(new_arrow_width + arrow_width/2.0), int(new_arrow_height + arrow_height/2.0)

def create_image(path, angle):
    with Image(filename=os.path.join(path, "arrow.png")) as arrow:
        carrow = arrow.clone()
        arrow_width, arrow_height = arrow.size
        with Image(filename=os.path.join(path, "bus.png")) as bus:
            cbus = bus.clone()
            bus_width, bus_height = bus.size

            new_arrow_width, new_arrow_height = adjust_arrow_dimensions(arrow_width, arrow_height, angle)

            carrow.rotate(angle)

            carrow.resize(new_arrow_width, new_arrow_height)

            arrow_left = bus_width/2 - new_arrow_width/2
            arrow_top = bus_height/6

            cbus.composite(carrow, arrow_left, arrow_top)

            cbus.save(filename=os.path.join(path, "bus_%d.png" % int(angle)))
            

def main():
    total_rotation = 360
    num_divisions = 45
    increment = float(total_rotation) / float(num_divisions)
    
    with open("../src/boston/Bus/Map/ui/BusDrawables.java", "wb") as f:
        header = """package boston.Bus.Map.ui;

public class BusDrawables {

    public static int getIdFromAngle(int angle) {
 """

        f.write(header)
        for i in xrange(num_divisions):
            angle = i * increment
            create_image("../res/drawable-hdpi", angle)
            create_image("../res/drawable-mdpi", angle)

            if i == 0:
                maybe_else = ""
            else:
                maybe_else = "else"
            code = """
        %s if (angle < %d) {
            return R.drawable.bus_%d;
        }
""" % (maybe_else, angle + increment, angle)

            f.write(code)
        footer = """
        else {
            return R.drawable.bus_0;
        }
    }
}
"""

        f.write(footer)

if __name__ == "__main__":
    main()
