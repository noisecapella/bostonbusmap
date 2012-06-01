import sys
import gzip
from xml.dom.minidom import parse


header = """package boston.Bus.Map.data;
import java.util.ArrayList;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.database.DatabaseHelper;

class PrepopulatedData {

    public static RouteConfig[] makeRoutes(TransitSource transitSource, TransitDrawables transitDrawables, Directions directions) {
    
"""


footer = """
        return routes;
    }
}
"""

def get_dom(filename):
    try:
        with gzip.open(filename, "rb") as f:
            return parse(f)

    except IOError:
        with open(filename, "rb") as f:
            return parse(f)
    
        
def run(filename):
    print header

    dom = get_dom(filename)
    
    routes = dom.getElementsByTagName("route")
    numRoutes = len(routes)
    print "        int numRoutes = {0};".format(numRoutes)
    print "        RouteConfig[] routes = new RouteConfig[numRoutes];"
    for i in xrange(len(routes)):
        route = routes[i]
        print "        {"
        print "            RouteConfig route = new RouteConfig(\"{0}\", \"{1}\", 0x{2}, 0x{3}, transitSource);".format(route.getAttribute("tag"), route.getAttribute("title"), route.getAttribute("color"), route.getAttribute("oppositeColor"))

        children = route.childNodes
        for child in children:
            if child.nodeName == "stop":
                print "            route.addStop(\"{0}\", new StopLocation({1}f, {2}f, transitDrawables, \"{0}\", \"{3}\"));".format(child.getAttribute("tag"), child.getAttribute("lat"), child.getAttribute("lon"), child.getAttribute("title"))
            elif child.nodeName == "direction":
                print "            directions.add(\"{0}\", \"{1}\", \"{2}\", \"{3}\");".format(child.getAttribute("tag"), child.getAttribute("name"), child.getAttribute("title"), route.getAttribute("tag"))

        print "            routes[{0}] = route;".format(i)
        print "        }"
    print footer

def main():
    if len(sys.argv) != 2:
        print "Usage: python routeconfig-to-java.py routeconfig.xml"
        exit(1)

    run(sys.argv[1])

if __name__ == "__main__":
    main()
