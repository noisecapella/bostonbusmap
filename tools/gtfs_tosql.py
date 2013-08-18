import csv
import argparse
import os.path
import schema
import itertools
from collections import defaultdict
from gtfs_map import GtfsMap

from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor

from mta_routes_available import get_available_routes

default_color = 0xff0000

def write_sql(startorder, route, gtfs_map, stops_already_inserted, routes_already_inserted):
    trips_header, trips = gtfs_map.trips_header, gtfs_map.trips
    stops_header, stops = gtfs_map.stops_header, gtfs_map.stops
    routes_header, routes = gtfs_map.routes_header, gtfs_map.routes
    stop_times_header, stop_times = gtfs_map.stop_times_header, gtfs_map.stop_times
    shapes_header, shapes = gtfs_map.shapes_header, gtfs_map.shapes
    

    route_rows = [route_row for route_row in routes
                  if (route_row[routes_header["route_id"]] == route)]
    route_ids = set([route_row[routes_header["route_id"]] for route_row in route_rows])

    trip_rows = [trip_row for trip_row in trips
                 if trip_row[trips_header["route_id"]] in route_ids]
    trip_ids = set([trip[trips_header["trip_id"]] for trip in trip_rows])

    shape_ids = set([trip[trips_header["shape_id"]] for trip in trip_rows])
    shape_rows = [shape_row for shape_row in shapes
                  if shape_row[shapes_header["shape_id"]] in shape_ids]

    # this stores a list of list of lat, lon pairs
    paths = []
    shape_rows = list(sorted(shape_rows, key=lambda shape: shape[shapes_header["shape_id"]]))
    for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape[shapes_header["shape_id"]]):
        path = [(float(row[shapes_header["shape_pt_lat"]]), float(row[shapes_header["shape_pt_lon"]])) for row in group_rows]
        paths.append(path)

    stop_times_rows = [stop_times_row for stop_times_row in stop_times
                       if stop_times_row[stop_times_header["trip_id"]] in trip_ids]
    stop_times_ids = set([stop_times_row[stop_times_header["stop_id"]] for stop_times_row in stop_times_rows])
    stop_rows = [stop_row for stop_row in stops
                 if stop_row[stops_header["stop_id"]] in stop_times_ids]

    pathblob = schema.Box(paths).get_blob_string()
    
    # insert route information
    obj = schema.getSchemaAsObject()
    if route not in routes_already_inserted:
        routes_already_inserted.add(route)

        obj.routes.route.value = route
        obj.routes.routetitle.value = route
        obj.routes.color.value = default_color
        obj.routes.oppositecolor.value = default_color
        obj.routes.listorder.value = startorder
        obj.routes.agencyid.value = schema.SubwayAgencyId
        obj.routes.pathblob.value = pathblob
        obj.routes.insert()

    for stop_row in stop_rows:
        stop_id = stop_row[stops_header["stop_id"]]
        if stop_id not in stops_already_inserted:
            stops_already_inserted.add(stop_id)

            obj.stops.tag.value = stop_id
            obj.stops.title.value = stop_row[stops_header["stop_name"]]
            obj.stops.lat.value = float(stop_row[stops_header["stop_lat"]])
            obj.stops.lon.value = float(stop_row[stops_header["stop_lon"]])
            obj.stops.insert()

        obj.stopmapping.route.value = route
        obj.stopmapping.tag.value = stop_row[stops_header["stop_id"]]
        obj.stopmapping.insert()

        

@inlineCallbacks
def main():
    parser = argparse.ArgumentParser(description="Parse gtfs into SQL statements")
    parser.add_argument('gtfs_path', type=str)
    parser.add_argument('order', type=int)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "%s is not a directory" % args.gtfs_path
        exit(-1)

    print("BEGIN TRANSACTION;")
    count = 0
    startorder = args.order

    # all routes.txt files are the same. TODO: add explicit check

    stops_already_inserted = set()
    routes_already_inserted = set()

    available_routes = yield get_available_routes()

    for borough in ["bronx", "brooklyn", "queens", "manhattan", "staten_island"]:

        gtfs_map = GtfsMap(os.path.join(args.gtfs_path, borough))
        routes = set([trip_row[gtfs_map.trips_header["route_id"]] for trip_row in gtfs_map.trips])
        routes = filter(lambda route: route in available_routes, routes)
        for route in routes:
            write_sql(startorder + count, route, gtfs_map, stops_already_inserted, routes_already_inserted)

    print("END TRANSACTION;")

if __name__ == "__main__":
    main().addBoth(lambda x: reactor.stop())

    reactor.run()
