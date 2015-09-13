import xml.sax

import time

import xml.dom.minidom
import schema
import requests
from simplify_path import simplify_path
class RouteHandler(xml.sax.handler.ContentHandler):
    def __init__(self, cur, startingOrder, sharedStops):
        self.cur = cur

        self.currentDirection = None
        self.currentRoute = None
        self.sharedStops = sharedStops
        self.table = schema.getSchemaAsObject()
        self.startingOrder = startingOrder
        self.inPath = False
        self.paths = []

    def startElement(self, name, attributes):
        table = self.table
        if name == "route":
            route = attributes["tag"]
            self.currentRoute = route
            table.routes.route.value = attributes["tag"]
            table.routes.routetitle.value = attributes["title"]
            table.routes.color.value = int(attributes["color"], 16)
            table.routes.oppositecolor.value = int(attributes["oppositeColor"], 16)
            table.routes.listorder.value = self.startingOrder
            table.routes.agencyid.value = schema.BusAgencyId

        elif name == "stop":
            tag = attributes["tag"]
            if not self.currentDirection:
                if tag not in self.sharedStops:
                    self.sharedStops[tag] = True
                    table.stops.tag.value = tag
                    table.stops.lat.value = attributes["lat"]
                    table.stops.lon.value = attributes["lon"]
                    table.stops.title.value = attributes["title"]
                    self.cur.execute(table.stops.insert())
                table.stopmapping.route.value = self.currentRoute
                table.stopmapping.tag.value = tag
                self.cur.execute(table.stopmapping.insert())
            else:
                pass
                #table.directionsStops.dirTag.value = self.currentDirection
                #table.directionsStops.tag.value = tag
                #self.cur.execute(table.directionsStops.insert())
                
        elif name == "direction": #band attributes["useForUI"] == "true":
            dirTag = attributes["tag"]
            self.currentDirection = dirTag
            if self.currentRoute:
                table.directions.dirTag.value = dirTag
                table.directions.dirTitleKey.value = attributes["title"]
                table.directions.dirRouteKey.value = self.currentRoute
                table.directions.dirNameKey.value = attributes["name"]
                table.directions.useAsUI.value = schema.getIntFromBool(attributes["useForUI"])
                self.cur.execute(table.directions.insert())
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
            self.cur.execute(self.table.routes.insert())
        elif name == "path":
            self.inPath = False
            self.currentPathPoints = simplify_path(self.currentPathPoints)
            self.paths.append(self.currentPathPoints)
                
    

class NextBus:
    url = "http://{prefix}.nextbus.com/service/publicXMLFeed?a={agency}&command={command}{other}"
    def __init__(self, agency):
        self.agency = agency

    def routeListUrl(self):
        return self.url.format(prefix = 'webservices',
                               agency = self.agency,
                               command = 'routeList',
                               other = '')

    def routeConfigUrl(self, route_name):
        return self.url.format(prefix = 'webservices',
                               agency = self.agency,
                               command = 'routeConfig',
                               other=('&r=%s&verbose' % route_name))

    def generate(self, conn, index):
        print("Downloading NextBus route data (this will take 10 or 20 minutes)...")
        routeList_data = requests.get(self.routeListUrl()).text
        routeList_dom = xml.dom.minidom.parseString(routeList_data)

        routes = []

        cur = conn.cursor()
        count = 0
        shared_stops = {}
        for routenode in routeList_dom.getElementsByTagName("route"):
            route_name = routenode.getAttribute("tag")
            route_title = routenode.getAttribute("title")
            routes.append(route_name)
            
            print("Route %s..." % route_title)
            try:
                routeConfig_data = requests.get(self.routeConfigUrl(route_name)).text
            except Exception:
                # try one more time
                routeConfig_data = requests.get(self.routeConfigUrl(route_name)).text

            handler = RouteHandler(cur, index + count, shared_stops)
            xml.sax.parseString(routeConfig_data.encode("utf-8"), handler)

            # NextBus rate limiting
            time.sleep(15)
            count += 1


        conn.commit()
        conn.close()

        return len(routes)
