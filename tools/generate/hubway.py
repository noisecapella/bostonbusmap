import schema
import sqlite3
import xml.dom.minidom
import requests

default_color = 0x6bc533

class Hubway:
    def generate(self, conn, start_index):
        url = "http://www.thehubway.com/data/stations/bikeStations.xml"

        obj = schema.getSchemaAsObject()

        data = requests.get(url).text
        root = xml.dom.minidom.parseString(data)

        cur = conn.cursor()
        
        obj.routes.route.value = "Hubway"
        obj.routes.routetitle.value = "Hubway"
        obj.routes.color.value = default_color
        obj.routes.oppositecolor.value = default_color
        obj.routes.listorder.value = start_index
        obj.routes.agencyid.value = schema.HubwayAgencyId
        obj.routes.pathblob.value = schema.Box([]).get_blob_string()
        cur.execute(obj.routes.insert())

        for station_node in root.getElementsByTagName("station"):
            stop_tag = "hubway_" + str(station_node.getElementsByTagName("id")[0].firstChild.nodeValue)

            obj.stops.tag.value = stop_tag
            obj.stops.title.value = str(station_node.getElementsByTagName("name")[0].firstChild.nodeValue)
            obj.stops.lat.value = float(station_node.getElementsByTagName("lat")[0].firstChild.nodeValue)
            obj.stops.lon.value = float(station_node.getElementsByTagName("long")[0].firstChild.nodeValue)
            obj.stops.parent.value = ""
            cur.execute(obj.stops.insert())

            obj.stopmapping.route.value = "Hubway"
            obj.stopmapping.tag.value = stop_tag
            cur.execute(obj.stopmapping.insert())

        conn.commit()

        cur.close()
        return 1
        

