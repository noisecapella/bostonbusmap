import tosql
import heavyrail_tosql
import commuterrail_tosql

import argparse
import os

from gtfs_map import GtfsMap

def main():
    parser = argparse.ArgumentParser(description="Parse routeconfig.xml into SQL statements")
    parser.add_argument("routeconfig", type=str)
    parser.add_argument("routeList", type=str)
    parser.add_argument("gtfs_path", type=str)
    parser.add_argument("stationorder_csv", type=str)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "%s is not a directory" % args.gtfs_path
        exit(-1)

    if not os.path.isfile(args.routeList):
        print "%s is not a route list file" % args.routeList
        exit(-1)

    if not os.path.isfile(args.routeconfig):
        print "%s is not a routeconfig file" % args.routeconfig
        exit(-1)

    if not os.path.isfile(args.stationorder_csv):
        print "%s is not a stationorder_csv file" % args.stationorder_csv
        exit(-1)

    shared_lat_lon = {}
    gtfs_map = GtfsMap(args.gtfs_path)

    commuterrail_tosql.write_sql(0, gtfs_map, args.stationorder_csv, shared_lat_lon)
    # starts at 12 because there are 12 commuter rail routes
    heavyrail_tosql.write_sql(gtfs_map, 12, shared_lat_lon)
    # starts at 15 to include 3 subway routes
    tosql.write_sql(args.routeList, args.routeconfig, 15, shared_lat_lon)

if __name__ == "__main__":
    main()
