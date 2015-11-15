import sqlite3
from hubway import Hubway
from mbta_heavy_rail import MbtaHeavyRail
from mbta_commuter_rail import MbtaCommuterRail
from nextbus import NextBus
from create_tables import create_tables

from datetime import datetime

from gtfs_map import GtfsMap

import os
import argparse
import traceback

statusCode = 1

def generate(conn, gtfs_map):
    create_tables(conn)
    index = 0
    print("Generating NextBus stops...")
    index = NextBus("ttc").generate(conn, index, gtfs_map)
    print(index)

def main():
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

    print("Reading GTFS into temporary database (this may take a few minutes)...")
    gtfs_map = GtfsMap(args.gtfs_path)

    if gtfs_map.last_date < datetime.now():
        raise Exception("GTFS data is old: %s is older than today" % str(gtfs_map.last_date))

    conn = sqlite3.connect(args.output_database)
    generate(conn, gtfs_map)

    conn.close()

if __name__ == "__main__":
    main()
