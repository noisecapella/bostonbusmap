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

        arrow_width, arrow_height = arrow.size

        for prefix in ("bus", "bus_selected", "rail", "rail_selected"):
            with Image(filename=os.path.join(path, "%s.png" % prefix)) as bus:
                cbus = bus.clone()
                bus_width, bus_height = bus.size

                carrow = arrow.clone()
                carrow.rotate(angle)
                new_arrow_width, new_arrow_height = adjust_arrow_dimensions(arrow_width, arrow_height, angle)

                carrow.resize(new_arrow_width, new_arrow_height)

                arrow_left = bus_width/2 - new_arrow_width/2
                arrow_top = bus_height/6

                cbus.composite(carrow, arrow_left, arrow_top)

                cbus.save(filename=os.path.join(path, "%s_%d.png" % (prefix, int(angle))))


def write_bus_drawables():
    total_rotation = 360
    num_divisions = 45
    increment = float(total_rotation) / float(num_divisions)
    with open("../src/boston/Bus/Map/ui/BusDrawables.java", "wb") as f:
        header = """package boston.Bus.Map.ui;

public class BusDrawables {
"""
        f.write(header)

        for prefix in ("bus", "bus_selected", "rail", "rail_selected"):
            code = """
    public static final int[] %sLookup = new int[] {
 """ % prefix

            f.write(code)
            for i in xrange(num_divisions):
                angle = i * increment

                if i != 0:
                    f.write(",\n")
                if prefix.endswith("selected"):
                    resource_name = prefix[:-len("selected")] + "statelist"
                else:
                    resource_name = prefix
                f.write("        R.drawable.%s_%d" % (resource_name, angle))

            code = """
        };
"""
            f.write(code)

        f.write("""
}
""")
            
def write_images():
    total_rotation = 360
    num_divisions = 45
    increment = float(total_rotation) / float(num_divisions)

    for i in xrange(num_divisions):
        angle = i * increment
        create_image("../res/drawable-hdpi", angle)
        create_image("../res/drawable-mdpi", angle)

def write_xml():
    total_rotation = 360
    num_divisions = 45
    increment = float(total_rotation) / float(num_divisions)

    for i in xrange(num_divisions):
        angle = i * increment
        for prefix in ("bus", "rail"):
            xml = """<?xml version="1.0" encoding="utf-8"?>

<selector xmlns:android="http://schemas.android.com/apk/res/android">

    <item
  android:state_focused="true"
  android:drawable="@drawable/%s_selected_%d" />
    <item  android:drawable="@drawable/%s_%d" />
</selector>
""" % (prefix, angle, prefix, angle)

            for path in ("../res/drawable-hdpi", "../res/drawable-mdpi"):
                with open("%s/%s_statelist_%d.xml" % (path, prefix, int(angle)), 'w') as f:
                    f.write(xml)



def main():
    #write_images()
    #write_bus_drawables()
    write_xml()

if __name__ == "__main__":
    main()
