RedLine = "Red"
OrangeLine = "Orange"
BlueLine = "Blue"

subway_color = {RedLine: 0xff0000,
                OrangeLine: 0xf88017,
                BlueLine: 0x0000ff}

import csv
import argparse
import os.path
import schema
import itertools
from collections import defaultdict

def read_rows(path):
    ret = {}

    with open(path) as f:
        reader = csv.DictReader(f)

        for row in reader:
            yield row

def write_sql(startorder, route, trips, stops, routes, stop_times, shapes):
    supported_route_description = route + " Line"
    route_rows = [route_row for route_row in routes
                  if (route_row["route_long_name"] == supported_route_description or
                      route_row["route_short_name"] == supported_route_description)]
    route_ids = set([route["route_id"] for route in route_rows])

    trip_rows = [trip_row for trip_row in trips
                 if trip_row["route_id"] in route_ids]
    trip_ids = set([trip["trip_id"] for trip in trip_rows])

    shape_ids = set([trip["shape_id"] for trip in trip_rows])
    shape_rows = [shape_row for shape_row in shapes
                  if shape_row["shape_id"] in shape_ids]

    # this stores a list of list of lat, lon pairs
    paths = []
    shape_rows = list(sorted(shape_rows, key=lambda shape: (shape["shape_id"], shape["shape_pt_sequence"])))
    for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape["shape_id"]):
        path = [(float(row["shape_pt_lat"]), float(row["shape_pt_lon"])) for row in group_rows]
        paths.append(path)

    print "BEFORE"
    stop_times_rows = [stop_times_row for stop_times_row in stop_times
                       if stop_times_row["trip_id"] in trip_ids]
    print "AFTER"
    stop_times_ids = set([stop_times_row["stop_id"] for stop_times_row in stop_times_rows])

    stop_rows = [stop_row for stop_row in stops
                 if stop_row["stop_id"] in stop_times_ids]

    
    pathblob = schema.Box(paths).get_blob_string()
    
    # insert route information
    obj = schema.getSchemaAsObject()
    obj.routes.route.value = route
    obj.routes.routetitle.value = route
    obj.routes.color.value = subway_color[route]
    obj.routes.oppositecolor.value = subway_color[route]
    obj.routes.listorder.value = startorder
    obj.routes.agencyid.value = schema.SubwayAgencyId
    obj.routes.pathblob.value = pathblob
    obj.routes.insert()

    for stop_row in stop_rows:
        obj.stops.tag.value = stop_row["stop_id"]
        obj.stops.title.value = stop_row["stop_name"]
        obj.stops.lat.value = float(stop_row["stop_lat"])
        obj.stops.lon.value = float(stop_row["stop_lon"])
        obj.stops.insert()

        obj.subway.platformorder.value = -1
        obj.subway.branch.value = "Unknown"
        obj.subway.tag.value = stop_row["stop_id"]
        obj.subway.insert()

        obj.stopmapping.route.value = route
        obj.stopmapping.tag.value = stop_row["stop_id"]
        obj.stopmapping.dirTag.value = None
        obj.stopmapping.insert()

        
    
def main():
    parser = argparse.ArgumentParser(description='Parse subway data')
    parser.add_argument('gtfs_path', type=str)
    parser.add_argument('order', type=int)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "%s is not a directory" % args.gtfs_path
        exit(-1)

    print("BEGIN TRANSACTION;")
    count = 0
    startorder = args.order
    for route in ["Red", "Orange", "Blue"]:

        trip_path = os.path.join(args.gtfs_path, "trips.txt")
        trips = read_rows(trip_path)

        stop_path = os.path.join(args.gtfs_path, "stops.txt")
        stops = read_rows(stop_path)

        route_path = os.path.join(args.gtfs_path, "routes.txt")
        routes = read_rows(route_path)

        stop_times_path = os.path.join(args.gtfs_path, "stop_times.txt")
        stop_times = read_rows(stop_times_path)

        shapes_path = os.path.join(args.gtfs_path, "shapes.txt")
        shapes = read_rows(shapes_path)

        write_sql(startorder + count, route, trips, stops, routes, stop_times, shapes)
        count += 1
    print("END TRANSACTION;")


if __name__ == "__main__":
    main()
