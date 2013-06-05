import sys
import gtfs_realtime_pb2

from twisted.internet import reactor
from twisted.internet.defer import inlineCallbacks
from twisted.web.client import getPage

@inlineCallbacks
def main():

    data = yield getPage("http://developer.mbta.com/lib/gtrtfs/Alerts/Alerts.pb")
    update = gtfs_realtime_pb2.FeedMessage()
    update.ParseFromString(data)

    print update

    reactor.stop()

if __name__ == "__main__":
    reactor.callLater(0, main)
    reactor.run()
