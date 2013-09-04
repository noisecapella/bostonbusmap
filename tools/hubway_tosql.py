from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor

import argparse

import schema

import xml.dom.minidom

default_color = 0x6bc533

@inlineCallbacks
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("start_index")
    args = parser.parse_args()
    
    start_index = int(args.start_index)

    url = "http://www.thehubway.com/data/stations/bikeStations.xml"

    obj = schema.getSchemaAsObject()

    data = yield getPage(url)
    root = xml.dom.minidom.parseString(data)

    print "BEGIN TRANSACTION;\n"
    obj.routes.route.value = "Hubway"
    obj.routes.routetitle.value = "Hubway"
    obj.routes.color.value = default_color
    obj.routes.oppositecolor.value = default_color
    obj.routes.listorder.value = start_index
    obj.routes.agencyid.value = schema.HubwayAgencyId
    obj.routes.pathblob.value = schema.Box([]).get_blob_string()
    obj.routes.insert()

    for station_node in root.getElementsByTagName("station"):
        stop_tag = "hubway_" + str(station_node.getElementsByTagName("id")[0].firstChild.nodeValue)

        obj.stops.tag.value = stop_tag
        obj.stops.title.value = str(station_node.getElementsByTagName("name")[0].firstChild.nodeValue)
        obj.stops.lat.value = float(station_node.getElementsByTagName("lat")[0].firstChild.nodeValue)
        obj.stops.lon.value = float(station_node.getElementsByTagName("long")[0].firstChild.nodeValue)
        obj.stops.insert()

        obj.stopmapping.route.value = "Hubway"
        obj.stopmapping.tag.value = stop_tag
        obj.stopmapping.insert()

    print "END TRANSACTION;\n"
        


def print_and_stop(x):
    #print x
    reactor.stop()

if __name__ == "__main__":
    main().addBoth(print_and_stop)
    reactor.run()
