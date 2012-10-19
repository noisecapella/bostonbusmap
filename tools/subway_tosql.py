mbtaUrl = "http://developer.mbta.com/RT_Archive/RealTimeHeavyRailKeys.csv"
RedNorthToAlewife = "RedNB0"
RedNorthToAlewife2 = "RedNB1"
RedSouthToBraintree = "RedSB0"
RedSouthToAshmont = "RedSB1"
BlueEastToWonderland = "BlueEB0"
BlueWestToBowdoin = "BlueWB0"
OrangeNorthToOakGrove = "OrangeNB0"
OrangeSouthToForestHills = "OrangeSB0"

RedLine = "Red"
OrangeLine = "Orange"
BlueLine = "Blue"

subway_color = {RedLine: 0xff0000,
                OrangeLine: 0xf88017,
                BlueLine: 0x0000ff}

red = 0xff0000

import urllib2
import sys
import schema
import xml.sax
import xml.sax.handler
import routetitleshandler

def createDirectionHash(direction, branch):
    return direction + branch

class Direction:
    def __init__(self, name, title, route, useForUI):
        self.name = name
        self.title = title
        self.route = route
        self.useForUI = useForUI

def write_sql(data, routeTitles, startOrder):
    lines = data.split("\n")
    indexes = {}
    first_line = lines[0].strip()
    header_items = first_line.split(",")
    for i in xrange(len(header_items)):
        indexes[header_items[i]] = i
    obj = schema.getSchemaAsObject()
    routes_done = {}
    orderedStations = {}
    paths = {}
    for line in filter(lambda x: x, (line.strip() for line in lines[1:])):
        items = line.split(",")
        routeName = items[indexes["Line"]]
        
        if routeName not in routes_done:
            routeTitle, order = routeTitles[routeName]

            routes_done[routeName] = {"route":routeName,
                                      "routetitle":routeTitle,
                                      "color":subway_color[routeName],
                                      "oppositecolor":subway_color[routeName],
                                      "listorder":startOrder + order,
                                      "agencyid":schema.SubwayAgencyId}


        platformOrder = int(items[indexes["PlatformOrder"]])
        latitudeAsDegrees = float(items[indexes["stop_lat"]])
        longitudeAsDegrees = float(items[indexes["stop_lon"]])
        tag = items[indexes["PlatformKey"]]
        title = items[indexes["stop_name"]]
        branch = items[indexes["Branch"]]

        obj.stops.tag.value = tag
        obj.stops.title.value = title
        obj.stops.lat.value = latitudeAsDegrees
        obj.stops.lon.value = longitudeAsDegrees
        obj.stops.insert()

        obj.subway.platformorder.value = platformOrder
        obj.subway.branch.value = branch
        obj.subway.tag.value = tag
        obj.subway.insert()

        obj.stopmapping.route.value = routeName
        obj.stopmapping.tag.value = tag
        obj.stopmapping.dirTag.value = None
        obj.stopmapping.insert()

        if routeName not in orderedStations:
            orderedStations[routeName] = {}
            paths[routeName] = []
        innerMapping = orderedStations[routeName]

        direction = items[indexes["Direction"]]
        
        combinedDirectionHash = createDirectionHash(direction, branch)
        if combinedDirectionHash not in innerMapping:
            innerMapping[combinedDirectionHash] = {}

        innerInnerMapping = innerMapping[combinedDirectionHash]
        innerInnerMapping[platformOrder] = (latitudeAsDegrees, longitudeAsDegrees)

    # workaround
    directions = {RedNorthToAlewife : Direction("North toward Alewife", None, RedLine, True),
                  RedNorthToAlewife2 : Direction("North toward Alewife", None, RedLine, True),
                  RedSouthToBraintree : Direction("South toward Braintree", None, RedLine, True),
                  RedSouthToAshmont : Direction("South toward Ashmont", None, RedLine, True),
                  BlueEastToWonderland : Direction("East toward Wonderland", None, BlueLine, True),
                  BlueWestToBowdoin : Direction("West toward Bowdoin", None, BlueLine, True),
                  OrangeNorthToOakGrove : Direction("North toward Oak Grove", None, OrangeLine, True),
                  OrangeSouthToForestHills : Direction("South toward Forest Hills", None, OrangeLine, True)}

    for dirKey, direction in directions.iteritems():
        obj.directions.dirTag.value = dirKey
        obj.directions.dirNameKey.value = direction.name
        obj.directions.dirTitleKey.value = direction.title
        obj.directions.dirRouteKey.value = direction.route
        obj.directions.useAsUI.value = schema.getIntFromBool(direction.useForUI)
        obj.directions.insert()
    for route, innerMapping in orderedStations.iteritems():
        for directionHash, stations in innerMapping.iteritems():
            floats = []
            for platformOrder in sorted(stations.keys()):
                floats.append(stations[platformOrder])

            #this is kind of a hack. We need to connect the southern branches of the red line
            if directionHash == "NBAshmont" or directionHash == "NBBraintree":
                jfkNorthBoundOrder = 5
                jfkStation = innerMapping["NBTrunk"][jfkNorthBoundOrder]
                jfkLat, jfkLon = jfkStation
                floats.append(jfkStation)
            paths[route].append(floats)

    for route, routedata in routes_done.iteritems():
        for key, value in routedata.iteritems():
            getattr(obj.routes, key).value = value

        if route in paths:
            obj.routes.pathblob.value = schema.Box(paths[route]).get_blob_string()

        obj.routes.insert()

        

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print "arg required: routeList startOrder"
        exit(-1)

    routeTitleParser = xml.sax.make_parser()
    routeHandler = routetitleshandler.RouteTitlesHandler()
    routeTitleParser.setContentHandler(routeHandler)
    routeTitleParser.parse(sys.argv[1])

    routeTitles = routeHandler.mapping
    
    response = urllib2.urlopen(mbtaUrl)
    data = response.read()

    print "BEGIN TRANSACTION;"
    write_sql(data, routeHandler.mapping, int(sys.argv[2]))
    
    print "END TRANSACTION;"









