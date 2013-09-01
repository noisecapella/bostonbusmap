import json
from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor

import schema

default_color = 0xff0000


@inlineCallbacks
def main():
    url = "http://appservices.citibikenyc.com//data2/stations.php"

    obj = schema.getSchemaAsObject()
    
    data = yield getPage(url)
    json_obj = json.loads(data)

    print "BEGIN TRANSACTION;\n"
    obj.routes.route.value = "Citibike"
    obj.routes.routetitle.value = "Citibike"
    obj.routes.color.value = default_color
    obj.routes.oppositecolor.value = default_color
    obj.routes.listorder.value = 101 # count of routes
    obj.routes.agencyid.value = schema.SubwayAgencyId
    obj.routes.pathblob.value = schema.Box([]).get_blob_string()
    obj.routes.insert()
    for result in json_obj["results"]:
        stop_tag = "citibike_" + str(result["id"])
        
        obj.stops.tag.value = stop_tag
        obj.stops.title.value = result["label"]
        obj.stops.lat.value = result["latitude"]
        obj.stops.lon.value = result["longitude"]
        obj.stops.insert()

        obj.stopmapping.route.value = "Citibike"
        obj.stopmapping.tag.value = stop_tag
        obj.stopmapping.insert()

    print "END TRANSACTION;\n"
    
def print_and_stop(x):
    #print x
    reactor.stop()

if __name__ == "__main__":
    main().addBoth(print_and_stop)

    reactor.run()
