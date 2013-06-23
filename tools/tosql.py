import xml.sax.handler
import xml.sax
import sys

import schema
import routetitleshandler
import argparse

def escaped(s):
    return s.replace("'", "''")

def hexToDec(s):
    return str(int(s, 16))

class ToSql(xml.sax.handler.ContentHandler):
    def __init__(self, routeKeysToTitles, startingOrder, sharedLatLon):
        self.currentDirection = None
        self.currentRoute = None
        self.sharedStops = {}
        self.sharedLatLon = sharedLatLon
        self.table = schema.getSchemaAsObject()
        self.routeKeysToTitles = routeKeysToTitles
        self.startingOrder = startingOrder
        self.inPath = False
        self.paths = []

    def startElement(self, name, attributes):
        table = self.table
        if name == "route":
            route = attributes["tag"]
            self.currentRoute = route
            table.routes.route.value = attributes["tag"]
            routetitle, order = self.routeKeysToTitles[route]
            table.routes.routetitle.value = routetitle
            table.routes.color.value = int(attributes["color"], 16)
            table.routes.oppositecolor.value = int(attributes["oppositeColor"], 16)
            table.routes.listorder.value = self.startingOrder + order
            table.routes.agencyid.value = schema.BusAgencyId

        elif name == "stop":
            tag = attributes["tag"]
            if not self.currentDirection:
                if tag not in self.sharedStops:
                    self.sharedStops[tag] = True
                    tup = attributes["lat"], attributes["lon"]
                    if tup not in self.sharedLatLon:
                        table.stopgroup.lat.value = attributes["lat"]
                        table.stopgroup.lon.value = attributes["lon"]
                        groupid = len(self.sharedLatLon)
                        table.stopgroup.groupid.value = groupid
                        table.stopgroup.insert()
                        self.sharedLatLon[tup] = groupid

                    table.stops.tag.value = tag
                    table.stops.groupid.value = self.sharedLatLon[tup]
                    table.stops.title.value = attributes["title"]
                    table.stops.insert()
                table.stopmapping.route.value = self.currentRoute
                table.stopmapping.tag.value = tag
                table.stopmapping.insert()
            else:
                pass
                #table.directionsStops.dirTag.value = self.currentDirection
                #table.directionsStops.tag.value = tag
                #table.directionsStops.insert()
                
        elif name == "direction": #band attributes["useForUI"] == "true":
            dirTag = attributes["tag"]
            self.currentDirection = dirTag
            if self.currentRoute:
                table.directions.dirTag.value = dirTag
                table.directions.dirTitleKey.value = attributes["title"]
                table.directions.dirRouteKey.value = self.currentRoute
                table.directions.dirNameKey.value = attributes["name"]
                table.directions.useAsUI.value = schema.getIntFromBool(attributes["useForUI"])
                table.directions.insert()
        elif name == "path":
            self.inPath = True
            self.currentPathPoints = []
        elif name == "point":
            lat = float(attributes["lat"])
            lon = float(attributes["lon"])
            
            self.currentPathPoints.append((lat, lon))

    def endElement(self, name):
        if name == "direction":
            self.currentDirection = None
        elif name == "route":
            self.currentRoute = None
            if len(self.paths) > 0:
                self.table.routes.pathblob.value = schema.Box(self.paths).get_blob_string()
            self.paths = []
            self.table.routes.insert()
        elif name == "path":
            self.inPath = False
            self.paths.append(self.currentPathPoints)
                
def write_sql(routeList, routeconfig, starting_route_index, sharedLatLon):
    routeTitleParser = xml.sax.make_parser()
    routeHandler = routetitleshandler.RouteTitlesHandler()
    routeTitleParser.setContentHandler(routeHandler)
    routeTitleParser.parse(routeList)
        
    print "BEGIN TRANSACTION;"
    parser = xml.sax.make_parser()
    handler = ToSql(routeHandler.mapping, starting_route_index, sharedLatLon)
    parser.setContentHandler(handler)
    parser.parse(routeconfig)
    print "END TRANSACTION;"


