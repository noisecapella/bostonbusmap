from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor
import sqlite3
from hubway import Hubway
from create_tables import create_tables

import os
import argparse

@inlineCallbacks
def generate(conn):
    create_tables(conn)
    index = yield Hubway().generate(conn, 0)
    index = yield MbtaCommuterRail().generate(conn, index)
    index = yield MbtaHeavyRail().generate(conn, index)
    index = yield NextBus("mbta").generate(conn, index)
    print index

@inlineCallbacks
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("output_database")
    args = parser.parse_args()

    os.unlink(args.output_database)

    conn = sqlite3.connect(args.output_database)
    yield generate(conn)

    conn.close()

    reactor.stop()


if __name__ == "__main__":
    reactor.callWhenRunning(main)
    reactor.run()
