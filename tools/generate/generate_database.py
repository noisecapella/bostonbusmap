from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor
import sqlite3
from citibike import Citibike
from mbta_heavy_rail import MbtaHeavyRail
from mbta_commuter_rail import MbtaCommuterRail
from nextbus import NextBus
from gtfs import Gtfs
from create_tables import create_tables

from datetime import datetime

from gtfs_map import GtfsMap

import os
import argparse
import traceback

statusCode = 1

@inlineCallbacks
def generate(conn, gtfs_path):
    create_tables(conn)
    print "Generating Citibike stops..."
    index = yield Citibike().generate(conn, 0)
    print "Generating GTFS stops..."
    index = yield Gtfs().generate(conn, index, gtfs_path)
    print index

@inlineCallbacks
def main():
    try:
        parser = argparse.ArgumentParser()
        parser.add_argument("output_database")
        parser.add_argument("gtfs_path")
        args = parser.parse_args()

        try:
            os.unlink(args.output_database)
        except:
            pass

        if not os.path.isdir(args.gtfs_path):
            raise Exception("%s is not a directory" % args.gtfs_path)

        print "Reading GTFS into memory..."
        conn = sqlite3.connect(args.output_database)
        yield generate(conn, args.gtfs_path)

        conn.close()
        
        statusCode = 0
    finally:
        reactor.stop()



if __name__ == "__main__":
    reactor.callLater(0, main)
    reactor.run()
    exit(statusCode)
