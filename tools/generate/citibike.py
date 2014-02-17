from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor
import schema
import sqlite3
import json

default_color = 0xff0000

class Citibike:
    @inlineCallbacks
    def generate(self, conn, start_index):
        url = "http://appservices.citibikenyc.com//data2/stations.php"

        obj = schema.getSchemaAsObject()

        data = yield getPage(url)
        json_obj = json.loads(data)

        cur = conn.cursor()
        
        obj.routes.route.value = "Citibike"
        obj.routes.routetitle.value = "Citibike"
        obj.routes.color.value = default_color
        obj.routes.oppositecolor.value = default_color
        obj.routes.listorder.value = start_index
        obj.routes.agencyid.value = schema.HubwayAgencyId
        obj.routes.pathblob.value = schema.Box([]).get_blob_string()
        cur.execute(obj.routes.insert())

        for result in json_obj["results"]:
            stop_tag = "citibike_" + str(result["id"])

            obj.stops.tag.value = stop_tag
            obj.stops.title.value = str(result["label"])
            obj.stops.lat.value = float(result["latitude"])
            obj.stops.lon.value = float(result["longitude"])
            cur.execute(obj.stops.insert())

            obj.stopmapping.route.value = "Citibike"
            obj.stopmapping.tag.value = stop_tag
            cur.execute(obj.stopmapping.insert())

        conn.commit()

        cur.close()
        returnValue(1)
        
        

