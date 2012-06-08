import sys
import gzip
import xml.dom.minidom
import math
from geopy import distance
from geopy.point import Point

#note: pypy is significantly faster than python on this script

headerDirections = """package boston.Bus.Map.data;

public class NextbusPrepopulatedDirections {
"""

header = """package boston.Bus.Map.data;
import java.util.ArrayList;
import java.io.IOException;

import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;

public class {0}PrepopulatedData {1}
    private final TransitSource transitSource;
    private final Directions directions;
    private final RouteConfig[] allRoutes;
    private final MyHashMap<LocationGroup, LocationGroup> allStops;


    public {0}PrepopulatedData(TransitSource transitSource, Directions directions) throws Exception {1}
        this.transitSource = transitSource;
        this.directions = directions;
        allRoutes = makeAllRoutes();
        allStops = makeAllStops();
    {2}

    private MyHashMap<LocationGroup, LocationGroup> makeAllStops() {1}
        MyHashMap<LocationGroup, LocationGroup> ret = new MyHashMap<LocationGroup, LocationGroup>();
        for (RouteConfig route : allRoutes) {1}
            for (StopLocation stop : route.getStops()) {1}
                LocationGroup locationGroup = ret.get(stop);
                if (locationGroup != null) {1}
                    if (locationGroup instanceof MultipleStopLocations) {1}
                        ((MultipleStopLocations)locationGroup).addStop(stop);
                    {2}
                    else
                    {1}
                        MultipleStopLocations multipleStopLocations = new MultipleStopLocations();
                        multipleStopLocations.addStop((StopLocation)locationGroup);
                        multipleStopLocations.addStop(stop);
                        ret.put(multipleStopLocations, multipleStopLocations);
                    {2}
                {2}
                else
                {1}
                    ret.put(locationGroup, locationGroup);
                {2}
            {2}
        {2}
        return ret;
    {2}

    public RouteConfig[] getAllRoutes() {1}
        return allRoutes;
    {2}

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


def humanize(locKey):
    return str(locKey).replace("-", "_").replace(".", "_").replace(",", "_").replace(" ", "_").replace("(", "_").replace(")", "_")


def printEachMakeRoute(routes, f):
    for i in xrange(len(routes)):
        route = routes[i]
        routeTag = route["tag"]
        f.write("    public RouteConfig makeRoute{0}() throws IOException {1}".format(i, "{") + "\n")
        f.write("        TransitDrawables drawables = transitSource.getDrawables();\n")
        f.write("        RouteConfig route = new RouteConfig(\"{0}\", \"{1}\", 0x{2}, 0x{3}, transitSource);".format(routeTag, route["title"], route["color"], route["oppositeColor"]) + "\n")

        for stop in route["stops"]:
            stopTag = stop["tag"]
            f.write("        StopLocation stop{0} = new StopLocation({1}f, {2}f, drawables, \"{0}\", \"{3}\");".format(stopTag, stop["lat"], stop["lon"], stop["title"]) + "\n")
            f.write("        allStops.put(stop{1}, stop{1});".format(stopTag, stopTag) + "\n")
            f.write("        route.addStop(\"{0}\", stop{1});".format(stopTag, stopTag) + "\n")
        for direction in route["directions"]:
            f.write("            directions.add(\"{0}\", new Direction(\"{1}\", \"{2}\", \"{3}\"));".format(direction["tag"], direction["name"], direction["title"], routeTag) + "\n")
            for dirChild in direction["stops"]:
                dirStopTag = dirChild["tag"]
                f.write("            route.addStop(\"{0}\", stop{1});".format(dirStopTag, dirStopTag) + "\n")
                       

        f.write("        return route;\n")
        f.write("    }\n")


def nextbusToRoutes(routesXml):
    routes = []
    for routeXml in routesXml.getElementsByTagName("route"):
        stops = []
        directions = []
        route = {"tag":routeXml.getAttribute("tag"), "title": routeXml.getAttribute("title"), "color": routeXml.getAttribute("color"), "oppositeColor": routeXml.getAttribute("oppositeColor"), "stops": stops, "directions": directions}
        routes.append(route)
        for routeChildXml in routeXml.childNodes:
            if routeChildXml.nodeName == "stop":
                stops.append({"tag": routeChildXml.getAttribute("tag"), "title": routeChildXml.getAttribute("title"), "lat": routeChildXml.getAttribute("lat"), "lon": routeChildXml.getAttribute("lon")})
            elif routeChildXml.nodeName == "direction":
                directionStops = []
                direction = {"tag" : routeChildXml.getAttribute("tag"), "title": routeChildXml.getAttribute("title"), "name": routeChildXml.getAttribute("name"), "stops": directionStops}
                directions.append(direction)
                for directionChildXml in routeChildXml.childNodes:
                    if directionChildXml.nodeName == "stop":
                        directionStops.append({"tag": directionChildXml.getAttribute("tag")})
    return routes

def runPrepopulated(routes, f, prefix):
    f.write(header.format(prefix, "{", "}") + "\n")

    printMakeAllRoutes(routes, f)
    printEachMakeRoute(routes, f)
    
    f.write(footer + "\n")


def main():
    if len(sys.argv) != 3:
        sys.stderr.write("Usage: python routeconfig-to-java.py routeconfig.xml srcDirectory\n")
        exit(1)

    dom = get_dom(sys.argv[1])
    nextbusPrefix = "Nextbus"
    f = open(sys.argv[2] + "/boston/Bus/Map/data/{0}PrepopulatedData.java".format(nextbusPrefix), "wb")
    nextbusRoutes = nextbusToRoutes(dom)
    runPrepopulated(nextbusRoutes, f, nextbusPrefix)
    #f = open(sys.argv[2] + "/boston/Bus/Map/data/NextbusPrepopulatedDirections.java", "wb")
    #runDirections(dom, f)

    

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

