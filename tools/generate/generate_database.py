from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor
import sqlite3
from hubway import Hubway
from mbta_heavy_rail import MbtaHeavyRail
from mbta_commuter_rail import MbtaCommuterRail
from create_tables import create_tables

from gtfs_map import GtfsMap

import os
import argparse
import traceback

@inlineCallbacks
def generate(conn, gtfs_map):
    create_tables(conn)
    print "Generating Hubway stops..."
    index = yield Hubway().generate(conn, 0)
    print "Generating commuter rail stops..."
    index = yield MbtaCommuterRail().generate(conn, index, gtfs_map)
    print "Generating heavy rail stops..."
    index = yield MbtaHeavyRail().generate(conn, index, gtfs_map)
    #print "Generating NextBus stops..."
    #index = yield NextBus("mbta").generate(conn, index)
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
        gtfs_map = GtfsMap(args.gtfs_path)

        conn = sqlite3.connect(args.output_database)
        yield generate(conn, gtfs_map)

        conn.close()
        
        reactor.stop()
    except Exception as e:
        reactor.stop()
        raise


if __name__ == "__main__":
    reactor.callWhenRunning(main)
    reactor.run()
