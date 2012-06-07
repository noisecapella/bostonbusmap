import sys
import gzip
import xml.dom.minidom
import math
from geopy import distance
from geopy.point import Point

header = """package boston.Bus.Map.data;
import java.util.ArrayList;
import java.io.IOException;

import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.data.Directions;

public class PrepopulatedData {
    private final TransitSource transitSource;
    private final Directions directions;

    public PrepopulatedData(TransitSource transitSource, Directions directions) {
        this.transitSource = transitSource;
        this.directions = directions;
    }
"""

footer = """}"""

def get_dom(filename):
    try:
        with gzip.open(filename, "rb") as f:
            return xml.dom.minidom.parse(f)

    except IOError:
        with open(filename, "rb") as f:
            return xml.dom.minidom.parse(f)
    
nodeCount = 0
class Node:
    def __init__(self):
        self.children = []
        self.data = set()
        self.value = ""
        global nodeCount
        self.id = nodeCount
        nodeCount += 1

def addToTrie(trie, routeName, data):
    currentNode = trie

    for c in routeName:
        for child in currentNode.children:
            if child.value == c:
                currentNode = child
                break
        else:
            newNode = Node()
            newNode.value = c
            currentNode.children.append(newNode)
            currentNode = newNode
    currentNode.data.add(data)

def escapeSingleQuote(s):
    if s == "'" or s == "\\":
        return "\\" + s
    else:
        return s

def escapeDoubleQuote(s):
    #TODO: implement. I don't think any stop title has a quote in it, though
    return s

def printTrie(node, javaVarName, javaVarNodeId, javaVarNodeLevel, dataIsCommand):
    indent = "            "
    
    print indent + "case {0}: {1}".format(node.id, "{")

    allChildrenOneChar = True
    for child in node.children:
        if len(child.value) > 1:
            allChildrenOneChar = False
            break

    if len(node.children) == 1:
        if allChildrenOneChar:
            child = node.children[0]
            print indent + "if ('{0}' == {1}.charAt({2})) {3}".format(escapeSingleQuote(child.value), javaVarName, javaVarNodeLevel, "{")
            print indent + "    " + javaVarNodeId + " = {0};".format(child.id)
            print indent + "    } else {"
            print indent + "        break out;"
            print indent + "    }"
        else:
            print indent + "if (\"{0}\".equals({1}.substring({2}))) {3}".format(escapeDoubleQuote(child.value), javaVarName, javaVarNodeLevel, "{")
            print indent + "    " + javaVarNodeId + " = {0};".format(child.id)
            print indent + "    } else {"
            print indent + "        break out;"
            print indent + "    }"
    elif len(node.children) > 1:
        if allChildrenOneChar:
            print indent + "switch({0}.charAt({1})) {2}".format(javaVarName, javaVarNodeLevel, "{")
            for child in node.children:
                print indent + "    " + "case '{0}': {1}".format(escapeSingleQuote(child.value),     "{")
                print indent + "    " + "    " + javaVarNodeId + " = {0};".format(child.id)
                print indent + "    }"
            print indent + "    " + "default:"
            print indent + "    " + "    break out;"
            print indent + "}"
        else:
            for childIndex in xrange(len(node.children)):
                child = node.children[childIndex]
                print indent + "    ",
                if childIndex != 0:
                    print "} else ",

                print "if (\"{0}\".equals({1}.substring({2}))) {3}".format(escapeDoubleQuote(child.value), javaVarName, javaVarNodeLevel, "{")
                print indent + "    " + javaVarNodeId + " = {0};".format(child.id)
            print indent + "    } else {"
            print indent + "        break;"
            print indent + "    }"
    else:
        if dataIsCommand:
            print indent + "return {0};".format(node.data.pop())
        elif len(node.data) == 1:
            print indent + "return new String[]{0}\"{1}\"{2};".format("{", node.data.pop(), "}")
        else:
            print indent + "return new String[] {0}".format("{")
            for d in node.data:
                print indent + "    " + "\"{0}\",".format(d)
            print indent + "};"
    
    print indent + "}"

    for child in node.children:
        printTrie(child, javaVarName, javaVarNodeId, javaVarNodeLevel, dataIsCommand)


def optimizeTrie(currentNode):
    while len(currentNode.children) == 1 and len(currentNode.data) == 0:
        childNode = currentNode.children[0]

        currentNode.value = currentNode.value + childNode.value
        currentNode.children = childNode.children
        currentNode.data = childNode.data

    for i in xrange(len(currentNode.children)):
        currentNode.children[i] = optimizeTrie(currentNode.children[i])

    return currentNode
        
        

def printStopSuffixes(routes):
    stopTrie = Node()
    print """    public static String[] getMatchingStops(String search) {"""
    print """        String lower = search.toLowerCase();"""
    print """        int currentNodeId = {0};""".format(stopTrie.id)
    print """        int i = 0;"""
    print """    out:"""
    print """        for (i = 0; i < search.length(); i++) {"""
    print """            switch (currentNodeId) {"""
    for route in routes:
        for child in route.childNodes:
            if child.nodeName == "stop":
                stopTag = child.getAttribute("tag")
                stopTitle = child.getAttribute("title").lower()
                for i in xrange(len(stopTitle)):
                    s = stopTitle[i:]
                    addToTrie(stopTrie, s, stopTag)
    
    stopTrie = optimizeTrie(stopTrie)

    printTrie(stopTrie, "lower", "currentNodeId", "i", False)
    print """            }"""
    print """        }"""
    print "    throw new RuntimeException(\"Stop not found: \" + search);"
    print """    }"""
                
def printMakeRoute(routes):
    routeTrie = Node()
    print """    public static RouteConfig makeRoute(String search, TransitSource transitSource, TransitDrawables transitDrawables, Directions directions) throws IOException {"""
    print """        int currentNodeId = {0};""".format(routeTrie.id)
    print """        int i = 0;"""
    print """    out:"""
    print """        for (i = 0; i < search.length(); i++) {"""
    print """            switch (currentNodeId) {"""
    
    for i in xrange(len(routes)):
        route = routes[i]
        addToTrie(routeTrie, route.getAttribute("tag"), "makeRoute{0}(transitSource, transitDrawables, directions)".format(i))
    printTrie(routeTrie, "search", "currentNodeId", "i", True)
    print "            }"
    print "        }"
    print "    throw new RuntimeException(\"Route not found: \" + search);"
    print "    }"

def printMakeAllRoutes(routes):
    print "    public RouteConfig[] makeAllRoutes() throws IOException {"
    print "        return new RouteConfig[] {"
    for i in xrange(len(routes)):
        print "            makeRoute{0}(),".format(i)
    print "        };"
    print "    }"

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

def printStopGraph(routes):
    print "    public MyHashMap<String, String[]> getLocalStops() {"
    print "        MyHashMap<String, String[]> ret = new MyHashMap<String, String[]>();"
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
        print "        {"
        print "            String[] arr = new String[] {"
        key = (stops.keys()[0], lat, lon)
        byDistance = filter(lambda t: distanceFunc(t, key) < 0.5, stopMap.values())
        byDistance = sorted(byDistance, key = lambda t: distanceFunc(t, key))

        usedLatKeys = {(lat, lon):True}
        for each in byDistance:
            newLatKey = (each[1], each[2])
            if newLatKey not in usedLatKeys:
                print "                \"{0}\",".format(each[0])
                usedLatKeys[newLatKey] = True
                if len(usedLatKeys) == 5:
                    break
        print "            };"
        for stopTag, _ in stops.iteritems():
            print "            ret.put(\"{0}\", arr);".format(stopTag)
        print "        }"


    print "        return ret;"
    print "    }"

    
def printEachMakeRoute(routes):
    for i in xrange(len(routes)):
        route = routes[i]
        print "    public RouteConfig makeRoute{0}() throws IOException {1}".format(i, "{")
        print "        TransitDrawables drawables = transitSource.getDrawables();"
        print "        RouteConfig route = new RouteConfig(\"{0}\", \"{1}\", 0x{2}, 0x{3}, transitSource);".format(route.getAttribute("tag"), route.getAttribute("title"), route.getAttribute("color"), route.getAttribute("oppositeColor"))

        children = route.childNodes
        for child in children:
            if child.nodeName == "stop":
                print "        route.addStop(\"{0}\", new StopLocation({1}f, {2}f, drawables, \"{0}\", \"{3}\"));".format(child.getAttribute("tag"), child.getAttribute("lat"), child.getAttribute("lon"), child.getAttribute("title"))
            elif child.nodeName == "direction":
                print "        directions.add(\"{0}\", \"{1}\", \"{2}\", \"{3}\");".format(child.getAttribute("tag"), child.getAttribute("name"), child.getAttribute("title"), route.getAttribute("tag"))

        print "        return route;"
        print "    }"


def run(dom):
    print header

    routes = dom.getElementsByTagName("route")
    #printMakeRoute(routes)

    # a helper for search suggestions
    #printStopSuffixes(routes)
    printMakeAllRoutes(routes)
    printEachMakeRoute(routes)
    
    printStopGraph(routes)

    print footer

def main():
    if len(sys.argv) != 2:
        print "Usage: python routeconfig-to-java.py routeconfig.xml"
        exit(1)

    dom = get_dom(sys.argv[1])
    
    run(dom)

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

