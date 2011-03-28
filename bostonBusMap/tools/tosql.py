import xml.sax.handler
import xml.sax
import sys

def escaped(s):
    return s.replace("'", "''")

def hexToDec(s):
    return str(int(s, 16))

class ToSql(xml.sax.handler.ContentHandler):
    routesTable = "routes"
    stopsTable = "stops"
    stopsRoutesMap = "stopmapping"
    
    routeKey = "route"
    pathsBlobKey = "pathblob"
    colorKey = "color"
    oppositeColorKey = "oppositecolor"

    latitudeKey = "lat"
    longitudeKey = "lon"
    stopTitleKey = "title"
    stopTagKey = "tag"
    dirTagKey = "dirTag"

    def __init__(self):
        self.currentRoute = None
        self.currentDirection = None
        self.sharedStops = {}

        print "CREATE TABLE IF NOT EXISTS " + self.routesTable + " (" + self.routeKey + " STRING PRIMARY KEY, " + self.colorKey + " INTEGER, " + self.oppositeColorKey + " INTEGER, " + self.pathsBlobKey + " BLOB);"
        print "CREATE TABLE IF NOT EXISTS " + self.stopsTable + " (" + self.stopTagKey + " STRING PRIMARY KEY, " + self.latitudeKey + " FLOAT, " + self.longitudeKey + " FLOAT, " + self.stopTitleKey + " STRING);"
        print "CREATE TABLE IF NOT EXISTS " + self.stopsRoutesMap + " (" + self.routeKey + " STRING, " + self.stopTagKey + " STRING, " + self.dirTagKey + " STRING);"

    def startElement(self, name, attributes):
        if name == "route":
            self.currentRoute = attributes["tag"]
            
            print "INSERT INTO " + self.routesTable,
            #print "(" + self.routeKey + ", " + self.pathsBlobKey + ", " + self.colorKey + ", " + self.oppositeColorKey + ") ",
            print " VALUES (",
            print "'" + self.currentRoute + "', ",
            print "x'00000000', ",
            print "" + hexToDec(attributes["color"]) + ", ",
            print "" + hexToDec(attributes["oppositeColor"]) + ");"
        elif name == "stop":
            if not self.currentDirection:
                tag = attributes["tag"]
                if tag not in self.sharedStops:
                    self.sharedStops[tag] = True
                    print "INSERT INTO " + self.stopsTable,
                    #print "(" + self.stopTagKey + ", " + self.latitudeKey + ", " + self.longitudeKey + ", " + self.stopTitleKey + ") "
                    print " VALUES (",
                    print "'" + attributes["tag"] + "', ",
                    print "" + attributes["lat"] + ", ",
                    print "" + attributes["lon"] + ", ",
                    print "'" + escaped(attributes["title"]) + "');"
            else:
                print "INSERT INTO " + self.stopsRoutesMap,
                #print " (" + routeKey + ", " + stopTagKey + ", " + dirTagKey + ") " 
                print " VALUES (",
                print "'" + self.currentRoute + "', ",
                print "'" + attributes["tag"] + "', ",
                print "'" + self.currentDirection + "');"
        elif name == "direction": #band attributes["useForUI"] == "true":
            self.currentDirection = attributes["tag"]
                
            
            

    def endElement(self, name):
        if name == "route":
            self.currentRoute = None
        elif name == "direction":
            self.currentDirection = None

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "arg required: routeConfig.xml"
        exit(-1)
        
    print "BEGIN TRANSACTION;"
    parser = xml.sax.make_parser()
    handler = ToSql()
    parser.setContentHandler(handler)
    parser.parse(sys.argv[1])
    print "END TRANSACTION;"
