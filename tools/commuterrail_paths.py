import os
import argparse
import csv
from gtfs_map import GtfsMap

from collections import defaultdict

def calculate_path(route, gtfs_map, stationorder_csv):
    if route[-5:] == " Line":
        route = route[:-5]
    if route[:3] == "CR-":
        route = route[3:]


    paths = []
    path = []
    with open(stationorder_csv) as f:
        reader = csv.DictReader(f)

        prev_direction_id = None
        prev_branch = None
        last_trunk_row = None
        for row in reader:
            if route in row["route_long_name"]:
                direction_id = int(row["direction_id"])
                lat = float(row["stop_lat"])
                lon = float(row["stop_lon"])
                branch = row["Branch"]

                if prev_direction_id is not None and prev_direction_id != direction_id:
                    paths.append(path)
                    path = []
                elif prev_branch != branch:
                    if prev_branch == "Trunk":
                        # continue as normal onto branch
                        pass
                    elif prev_branch is not None:
                        # finish this branch and start a new one from end of trunk
                        paths.append(path)
                        path = [(float(last_trunk_row["stop_lat"]),
                                 float(last_trunk_row["stop_lon"]))]
                        
                path.append((lat, lon))
                prev_direction_id = direction_id
                prev_branch = branch
                if branch == "Trunk":
                    last_trunk_row = row

    return paths

def main():
    parser = argparse.ArgumentParser(description='Write CSV of paths')
    parser.add_argument("stationorder_csv", type=str)
    parser.add_argument("gtfs_path", type=str)
    parser.add_argument("route", type=str)
    args = parser.parse_args()

    gtfs_map = GtfsMap(args.gtfs_path)
    paths = calculate_path(args.route, gtfs_map, args.stationorder_csv)

    print "id,lat,lon"
    for i, path in enumerate(paths):
        for lat, lon in path:
            print "%d,%f,%f" % (i, lat, lon)

if __name__ == "__main__":
    main()
