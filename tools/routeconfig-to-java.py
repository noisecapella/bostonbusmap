import sys
import gzip
import xml.dom.minidom
import math
from geopy import distance
from geopy.point import Point

#note: pypy is significantly faster than python on this script

headerDirections = """package boston.Bus.Map.data;

public class PrepopulatedDirections {
"""

header = """package boston.Bus.Map.data;
import java.util.ArrayList;
import java.io.IOException;

import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;

public class PrepopulatedData {
    private final TransitSource transitSource;
    private final Directions directions;
    private final MyHashMap<String, StopLocation> allStops = new MyHashMap<String, StopLocation>();
    private final RouteConfig[] allRoutes;


    public PrepopulatedData(TransitSource transitSource, Directions directions) throws Exception {
        this.transitSource = transitSource;
        this.directions = directions;
        allRoutes = makeAllRoutes();
    }

    public MyHashMap<String, StopLocation> getAllStops() {
        return allStops;
    }

    public RouteConfig[] getAllRoutes() {
        return allRoutes;
    }
"""

footerDirections = "}"
footer = """}"""


def get_dom(filename):
    try:
        with gzip.open(filename, "rb") as f:
            return xml.dom.minidom.parse(f)

    except IOError:
        with open(filename, "rb") as f:
            return xml.dom.minidom.parse(f)
    
def escapeSingleQuote(s):
    if s == "'" or s == "\\":
        return "\\" + s
    else:
        return s

def escapeDoubleQuote(s):
    #TODO: implement. I don't think any stop title has a quote in it, though
    return s

def printMakeAllRoutes(routes, f):
    f.write("    private RouteConfig[] makeAllRoutes() throws IOException {\n")
    f.write("        return new RouteConfig[] {\n")
    for i in xrange(len(routes)):
        f.write("            makeRoute{0}(),".format(i) + "\n")
    f.write("        };\n")
    f.write("    }\n")

distanceMap = {}                        

def distanceFunc(tup1, tup2):
    # based on haversine formula for great circle distance
    _, lat1, lon1 = tup1
    _, lat2, lon2 = tup2

    #return distance.distance(Point(lat1, lon1), Point(lat2, lon2)).miles
    degreesToRadians = math.pi / 180.0

    deltaLon = (lon2 - lon1)*degreesToRadians
    deltaLat = (lat2 - lat1)*degreesToRadians

    sinResult1 = math.sin(deltaLon/2)
    sinResult2 = math.sin(deltaLat/2)
    
    

    c = 2 * math.asin(math.sqrt(sinResult1*sinResult1 + math.cos(degreesToRadians*lon1)*math.cos(degreesToRadians*lon2)*sinResult2*sinResult2))
    earthRadiusInKilo = 6371.2
    kiloPerMile = 1.609344
    dist = earthRadiusInKilo * c
    ret = dist/kiloPerMile
    return ret

def printStopGraph(routes, f):
    f.write("    public MyHashMap<String, String[]> getLocalStops() {\n")
    f.write("        MyHashMap<String, String[]> ret = new MyHashMap<String, String[]>();\n")
    locationMap = {}
    stopMap = {}
    for route in routes:
        routeTag = route.getAttribute("tag")
        for child in route.childNodes:
            if child.nodeName == "stop":
                stopTag = child.getAttribute("tag")
                lat = float(child.getAttribute("lat"))
                lon = float(child.getAttribute("lon"))

                locKey = (lat, lon)
                if not locKey in locationMap:
                    locationMap[locKey] = {}
                locationMap[locKey][stopTag] = True
                stopMap[stopTag] = (stopTag, lat, lon)
                
    for locKey, stops in locationMap.iteritems():
        if len(stops) == 0:
            continue
        lat, lon = locKey

        f.write("            makeClosest{0}(ret);".format(humanize(locKey) + "\n"))


    f.write("        return ret;\n")
    f.write("    }\n")

    for locKey, stops in locationMap.iteritems():
        if len(stops) == 0:
            continue

        printEachMakeClosestStops(locKey, stops, stopMap, f)

def humanize(locKey):
    return str(locKey).replace("-", "_").replace(".", "_").replace(",", "_").replace(" ", "_").replace("(", "_").replace(")", "_")

def printEachMakeClosestStops(locKey, stops, stopMap, f):
        lat, lon = locKey
        f.write("    public void makeClosest{0}(MyHashMap<String, String[]> map) {1}".format(humanize(locKey), "{") + "\n")
        f.write("        String[] arr = new String[] {\n")
        key = (stops.keys()[0], lat, lon)
        byDistance = filter(lambda t: distanceFunc(t, key) < 0.5, stopMap.values())
        byDistance = sorted(byDistance, key = lambda t: distanceFunc(t, key))

        usedLatKeys = {(lat, lon):True}
        for each in byDistance:
            newLatKey = (each[1], each[2])
            if newLatKey not in usedLatKeys:
                f.write("                \"{0}\",".format(each[0]) + "\n")
                usedLatKeys[newLatKey] = True
                if len(usedLatKeys) == 10:
                    break
        f.write("        };\n")

        for stop in stops.iteritems():
            stopTag, _ = stop
            f.write("        map.put(\"{0}\", arr);".format(stopTag) + "\n")
        f.write("    }\n")

def printEachMakeRoute(routes, f):
    for i in xrange(len(routes)):
        route = routes[i]
        routeTag = route.getAttribute("tag")
        f.write("    public RouteConfig makeRoute{0}() throws IOException {1}".format(i, "{") + "\n")
        f.write("        TransitDrawables drawables = transitSource.getDrawables();\n")
        f.write("        RouteConfig route = new RouteConfig(\"{0}\", \"{1}\", 0x{2}, 0x{3}, transitSource);".format(routeTag, route.getAttribute("title"), route.getAttribute("color"), route.getAttribute("oppositeColor")) + "\n")

        children = route.childNodes
        for child in children:
            if child.nodeName == "stop":
                stopTag = child.getAttribute("tag")
                f.write("        StopLocation stop{0} = new StopLocation({1}f, {2}f, drawables, \"{0}\", \"{3}\");".format(stopTag, child.getAttribute("lat"), child.getAttribute("lon"), child.getAttribute("title")) + "\n")
                f.write("        allStops.put(\"{0}\", stop{1});".format(stopTag, stopTag) + "\n")
                f.write("        route.addStop(\"{0}\", stop{1});".format(stopTag, stopTag) + "\n")
            elif child.nodeName == "direction":
                f.write("            directions.add(\"{0}\", new Direction(\"{1}\", \"{2}\", \"{3}\"));".format(child.getAttribute("tag"), child.getAttribute("name"), child.getAttribute("title"), routeTag) + "\n")
                prevDirChild = None
                for dirChild in child.childNodes:
                    if dirChild.nodeName == "stop":
                        if prevDirChild:
                            f.write("            stop{0}.addNextStop(stop{1}, \"{2}\");".format(prevDirChild.getAttribute("tag"), dirChild.getAttribute("tag"), routeTag) + "\n")
                        prevDirChild = dirChild
                       

        f.write("        return route;\n")
        f.write("    }\n")


def runPrepopulated(dom, f):
    f.write(header + "\n")

    routes = dom.getElementsByTagName("route")
    #printMakeRoute(routes)

    # a helper for search suggestions
    #printStopSuffixes(routes)
    printMakeAllRoutes(routes, f)
    printEachMakeRoute(routes, f)
    
    f.write(footer + "\n")
    #printConstants(routes)

def runDirections(dom, f):
    f.write(headerDirections + "\n")

    routes = dom.getElementsByTagName("route")

    printStopGraph(routes, f)

    f.write(footerDirections + "\n")

def main():
    if len(sys.argv) != 3:
        sys.stderr.write("Usage: python routeconfig-to-java.py routeconfig.xml srcDirectory\n")
        exit(1)

    dom = get_dom(sys.argv[1])
    f = open(sys.argv[2] + "/boston/Bus/Map/data/PrepopulatedData.java", "wb")
    runPrepopulated(dom, f)
    f = open(sys.argv[2] + "/boston/Bus/Map/data/PrepopulatedDirections.java", "wb")
    runDirections(dom, f)

    

def test():
    x = """<route tag="1" title="1" color="330000" oppositeColor="ffffff" latMin="42.3297899" latMax="42.37513" lonMin="-71.11896" lonMax="-71.07354">
<stop tag="10590" title="banana" lat="42.3364699" lon="-71.07681" stopId="10590"/>
<stop tag="97" title="banana2" lat="42.3591799" lon="-71.09354" stopId="00097"/>
<stop tag="101" title="nana" lat="42.3629899" lon="-71.09949" stopId="00101"/>
</route>
"""

    dom = xml.dom.minidom.parseString(x)
    run(dom)

if __name__ == "__main__":
    main()

