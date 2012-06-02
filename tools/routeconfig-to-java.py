import sys
import gzip
from xml.dom.minidom import parse


header = """package boston.Bus.Map.data;
import java.util.ArrayList;
import java.io.IOException;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.transit.TransitSource;
import boston.Bus.Map.database.DatabaseHelper;

class PrepopulatedData {

"""

footer = """}"""

def get_dom(filename):
    try:
        with gzip.open(filename, "rb") as f:
            return parse(f)

    except IOError:
        with open(filename, "rb") as f:
            return parse(f)
    
class Node:
    def __init__(self):
        self.children = []
        self.data = set()
        self.value = ""

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

def printTrie(node, javaVarName, level = 0):
    indent = "        {0}".format("    " * level)
    
    if len(node.children) > 0:
        allChildNodesAreChars = True
        for child in node.children:
            if len(child.value) > 1:
                allChildNodesAreChars = False
                break

        if allChildNodesAreChars:
            print indent + "switch({0}.charAt({1})) {2}".format(javaVarName, level, "{")
            for child in node.children:
                print indent + "    " + "case '{0}': {1}".format(child.value, "{")
                printTrie(child, javaVarName, level + 1)

                print indent + "    }"
            print indent + "}"
        else:
            for childIndex in xrange(len(node.children)):
                child = node.children[childIndex]
                print indent + "    ",
                if childIndex != 0:
                    print "else ",
                print "if (\"{0}\".equals({1}) {2}".format(child.value, javaVarName, "{")
                printTrie(child, javaVarName, level + 1)
                print indent + "    }"
            
    else:
        if len(node.data) == 1:
            print indent + "return new String[]{0}\"{1}\"{2};".format("{", node.data.pop(), "}")
        else:
            print indent + "return new String[] {0}".format("{")
            for d in node.data:
                print indent + "    " + "\"{0}\",".format(d)
            print indent + "}"

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
    print """    public static StopLocation[] getMatchingStops(String search) {"""
    print """        String lower = search.toLowerCase();"""
    stopTrie = Node()
    for route in routes:
        for child in route.childNodes:
            if child.nodeName == "stop":
                stopTag = child.getAttribute("tag")
                stopTitle = child.getAttribute("title").lower()
                for i in xrange(len(stopTitle)):
                    s = stopTitle[i:]
                    addToTrie(stopTrie, s, stopTag)
    
    optimizeTrie(stopTrie)

    printTrie(stopTrie, "lower")
    print """    }"""
                
def printMakeRoute(routes):
    print """    public static RouteConfig makeRoute(String routeTag, TransitSource transitSource, TransitDrawables transitDrawables, Directions directions) throws IOException {"""
    
    routeTrie = Node()
    for i in xrange(len(routes)):
        route = routes[i]
        addToTrie(routeTrie, route.getAttribute("tag"), "makeRoute{0}(transitSource, transitDrawables, directions)".format(i))
    printTrie(routeTrie, "routeTag")

    print "    throw new RuntimeException(\"Route not found: \" + routeTag);"
    print "    }"

def printEachMakeRoute(routes):
    for i in xrange(len(routes)):
        route = routes[i]
        print "    public static RouteConfig makeRoute{0}(TransitSource transitSource, TransitDrawables transitDrawables, Directions directions) throws IOException {1}".format(i, "{")
        print "        RouteConfig route = new RouteConfig(\"{0}\", \"{1}\", 0x{2}, 0x{3}, transitSource);".format(route.getAttribute("tag"), route.getAttribute("title"), route.getAttribute("color"), route.getAttribute("oppositeColor"))

        children = route.childNodes
        for child in children:
            if child.nodeName == "stop":
                print "        route.addStop(\"{0}\", new StopLocation({1}f, {2}f, transitDrawables, \"{0}\", \"{3}\"));".format(child.getAttribute("tag"), child.getAttribute("lat"), child.getAttribute("lon"), child.getAttribute("title"))
            elif child.nodeName == "direction":
                print "        directions.add(\"{0}\", \"{1}\", \"{2}\", \"{3}\");".format(child.getAttribute("tag"), child.getAttribute("name"), child.getAttribute("title"), route.getAttribute("tag"))

        print "        return route;"
        print "    }"


def run(filename):
    print header

    dom = get_dom(filename)
    
    routes = dom.getElementsByTagName("route")
    printMakeRoute(routes)

    # a helper for search suggestions
    printStopSuffixes(routes)

    printEachMakeRoute(routes)
    
    print footer

def main():
    if len(sys.argv) != 2:
        print "Usage: python routeconfig-to-java.py routeconfig.xml"
        exit(1)

    run(sys.argv[1])

if __name__ == "__main__":
    main()
