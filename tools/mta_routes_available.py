import sys
from xml.dom.minidom import parseString

from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor


KEY="TEST"

@inlineCallbacks
def get_available_routes():
    data = yield getPage("http://bustime.mta.info/api/where/routes-for-agency/MTA%20NYCT.xml?key=" + KEY)

    dom = parseString(data)
    
    routes = set()
    for route in dom.getElementsByTagName("route"):
        id = route.getElementsByTagName("id")[0].firstChild.nodeValue
        if id.startswith("MTA NYCT_"):
            id = id[len("MTA NYCT_"):]
            routes.add(id)

    returnValue(routes)

@inlineCallbacks
def _print_output_and_stop(output):
    print output
    yield None
    reactor.stop()

if __name__ == "__main__":
    get_available_routes().addBoth(_print_output_and_stop)
    reactor.run()
