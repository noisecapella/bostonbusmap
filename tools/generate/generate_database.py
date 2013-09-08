from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor
import sqlite3
from hubway import Hubway
from create_tables import create_tables

import os
import argparse
import traceback

@inlineCallbacks
def generate(conn):
    create_tables(conn)
    print "Generating Hubway stops..."
    index = yield Hubway().generate(conn, 0)
    print "Generating commuter rail stops..."
    index = yield MbtaCommuterRail().generate(conn, index)
    print "Generating heavy rail stops..."
    index = yield MbtaHeavyRail().generate(conn, index)
    print "Generating NextBus stops..."
    index = yield NextBus("mbta").generate(conn, index)
    print index

@inlineCallbacks
def main():
    try:
        parser = argparse.ArgumentParser()
        parser.add_argument("output_database")
        args = parser.parse_args()

        try:
            os.unlink(args.output_database)
        except:
            pass

        conn = sqlite3.connect(args.output_database)
        yield generate(conn)

        conn.close()
        
        reactor.stop()
    except Exception as e:
        reactor.stop()
        raise


if __name__ == "__main__":
    reactor.callWhenRunning(main)
    reactor.run()
