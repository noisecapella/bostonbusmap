import schema
import sqlite3
import xml.dom.minidom
import requests
import json

default_color = 0x6bc533

class Hubway:
    def generate(self, conn, start_index):
        info_url = "https://gbfs.thehubway.com/gbfs/en/station_information.json"

        obj = schema.getSchemaAsObject()

        info_data = json.loads(requests.get(info_url).text)

        cur = conn.cursor()
        
        obj.routes.route.value = "Hubway"
        obj.routes.routetitle.value = "Hubway"
        obj.routes.color.value = default_color
        obj.routes.oppositecolor.value = default_color
        obj.routes.listorder.value = start_index
        obj.routes.agencyid.value = schema.HubwayAgencyId
        obj.routes.pathblob.value = schema.Box([]).get_blob_string()
        cur.execute(obj.routes.insert())

        conn.commit()

        cur.close()
        return 1
        

