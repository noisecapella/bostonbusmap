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
from gtfs_map import GtfsMap

def write_route_sql(startorder, route, gtfs_map, latlonmap):
    trips_header, trips = gtfs_map.trips_header, gtfs_map.trips
    stops_header, stops = gtfs_map.stops_header, gtfs_map.stops
    routes_header, routes = gtfs_map.routes_header, gtfs_map.routes
    stop_times_header, stop_times = gtfs_map.stop_times_header, gtfs_map.stop_times
    shapes_header, shapes = gtfs_map.shapes_header, gtfs_map.shapes
    

    supported_route_description = route + " Line"
    route_rows = [route_row for route_row in routes
                  if (route_row[routes_header["route_long_name"]] == supported_route_description or
                      route_row[routes_header["route_short_name"]] == supported_route_description)]
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
    obj.routes.route.value = route
    obj.routes.routetitle.value = route
    obj.routes.color.value = subway_color[route]
    obj.routes.oppositecolor.value = subway_color[route]
    obj.routes.listorder.value = startorder
    obj.routes.agencyid.value = schema.SubwayAgencyId
    obj.routes.pathblob.value = pathblob
    obj.routes.insert()

    for stop_row in stop_rows:
        tup = (float(stop_row[stops_header["stop_lat"]]),
               float(stop_row[stops_header["stop_lon"]]))

        if tup not in latlonmap:
            groupid = len(latlonmap)
            obj.stopgroup.lat.value = tup[0]
            obj.stopgroup.lon.value = tup[1]
            obj.stopgroup.groupid.value = groupid
            obj.stopgroup.insert()
            latlonmap[tup] = groupid
        obj.stops.tag.value = stop_row[stops_header["stop_id"]]
        obj.stops.title.value = stop_row[stops_header["stop_name"]]
        obj.stops.groupid.value = latlonmap[tup]
        obj.stops.insert()

        obj.stopmapping.route.value = route
        obj.stopmapping.tag.value = stop_row[stops_header["stop_id"]]
        obj.stopmapping.insert()

        
def write_sql(gtfs_map, startorder, latlonmap):
    print("BEGIN TRANSACTION;")
    count = 0

    for route in ["Red", "Orange", "Blue"]:

        write_route_sql(startorder + count, route, gtfs_map, latlonmap)
        count += 1
    print("END TRANSACTION;")
    

    
def main():
    parser = argparse.ArgumentParser(description='Parse subway data')
    parser.add_argument('gtfs_path', type=str)
    parser.add_argument('order', type=int)
    args = parser.parse_args()



if __name__ == "__main__":
    main()
