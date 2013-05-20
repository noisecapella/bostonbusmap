#!/usr/bin/env python

import argparse
import os.path
import csv
import itertools
import simplekml
from collections import defaultdict

def read_rows(path):
    ret = {}

    with open(path) as f:
        reader = csv.DictReader(f)

        for row in reader:
            yield row

def extract_paths(route, routes, trips, shapes):
    route_rows = [route_row for route_row in routes
                  if (route_row["route_long_name"] == route
                      or route_row["route_short_name"] == route
                      or route_row["route_id"] == route)]

    route_color = route_rows[0]["route_color"]
    route_ids = set(route_row["route_id"] for route_row in route_rows)

    trip_rows = [trip_row for trip_row in trips
                 if trip_row["route_id"] in route_ids]
    trip_ids = set(trip_row["trip_id"] for trip_row in trip_rows)
    shape_ids = set(trip_row["shape_id"] for trip_row in trip_rows)

    shape_rows = [shape_row for shape_row in shapes
                  if shape_row["shape_id"] in shape_ids]
    shape_rows = sorted(shape_rows, key=lambda shape_row: shape_row["shape_id"])

    shapes_by_id = itertools.groupby(shape_rows, lambda shape_row: shape_row["shape_id"])
    
    paths = [[(shape_row["shape_pt_lat"], shape_row["shape_pt_lon"]) for shape_row in group] for shape_id, group in shapes_by_id]
    return paths, route_color
    

def main():
    parser = argparse.ArgumentParser(description='Create a KML with the shapes for a given route.\nThis can be displayed in Google Earth or other KML visualizing programs')
    parser.add_argument('route', type=str)
    parser.add_argument('gtfs_path', type=str)
    parser.add_argument('kml_output', type=str)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "gtfs_path must be a directory"
        exit(-1)

    route_path = os.path.join(args.gtfs_path, "routes.txt")
    routes = read_rows(route_path)

    trip_path = os.path.join(args.gtfs_path, "trips.txt")
    trips = read_rows(trip_path)

    shape_path = os.path.join(args.gtfs_path, "shapes.txt")
    shapes = read_rows(shape_path)

    paths, color = extract_paths(args.route, routes, trips, shapes)

    kml = simplekml.Kml()

    for i, path in enumerate(paths):
        kml.newlinestring(name="Path %d" % i,
                          description="Path %d" % i,
                          coords=[(pair[1], pair[0]) for pair in path])

    kml.save(args.kml_output)
    
if __name__ == "__main__":
    main()
