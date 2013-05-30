import schema
import sys
import xml.sax.handler
import xml.sax
import routetitleshandler
import argparse
import os
import csv

purple = 0x940088

def make_index_map(array):
    ret = {}
    for i, item in enumerate(array):
        ret[item] = i
    return ret

def read_rows(path):
    ret = {}

    with open(path) as f:
        reader = csv.reader(f)

        header = make_index_map(next(reader))
        return header, [row for row in reader]

def createDirectionHash(a, b):
    return a + "|" + b

def write_sql(startorder, trips_tups, stops_tups, routes_tups, stop_times_tups, shapes_tups):
    trips_header, trips = trips_tups
    stops_header, stops = stops_tups
    routes_header, routes = routes_tups
    stop_times_header, stop_times = stop_times_tups
    shapes_header, shapes = shapes_tups

    # this is a workaround
    route_order = {
        "CR-Greenbush": 1,
        "CR-Kingston": 2,
        "CR-Middleborough": 3,
        "CR-Fairmount": 4,
        "CR-Providence": 5,
        "CR-Franklin": 6,
        "CR-Needham": 7,
        "CR-Worcester": 8,
        "CR-Fitchburg": 9,
        "CR-Lowell": 10,
        "CR-Haverhill": 11,
        "CR-Newburyport": 12}

    stops_inserted = set()

    route_rows = [route_row for route_row in routes
                  if int(route_row[routes_header["route_type"]]) == schema.CommuterRailAgencyId]
    for route_row in route_rows:
        route_id = route_row[routes_header["route_id"]]
        route_title = route_row[routes_header["route_short_name"]]
        if not route_title:
            route_title = route_row[routes_header["route_long_name"]]
        
        trip_rows = [trip_row for trip_row in trips
                     if trip_row[trips_header["route_id"]] == route_id]
        trip_ids = set([trip[trips_header["trip_id"]] for trip in trip_rows])

        # TODO: when shape data appears in GTFS, add it here
        # for now use stop_sequence instead

        all_stop_times_rows = [stop_times_row for stop_times_row in stop_times
                               if stop_times_row[stop_times_header["trip_id"]] in trip_ids]
        all_stop_times_ids = set([stop_times_row[stop_times_header["stop_id"]] for stop_times_row in all_stop_times_rows])
        stop_rows = [stop_row for stop_row in stops
                     if stop_row[stops_header["stop_id"]] in all_stop_times_ids]
        stops_to_lat_lon = {}
        for stop_row in stop_rows:
            lat = float(stop_row[stops_header["stop_lat"]])
            lon = float(stop_row[stops_header["stop_lon"]])
            stop_id = stop_row[stops_header["stop_id"]]
            stops_to_lat_lon[stop_id] = (lat, lon)

        longest_sequences = {}
        for trip_id in trip_ids: 
            stop_times_rows = [stop_times_row for stop_times_row in all_stop_times_rows
                               if stop_times_row[stop_times_header["trip_id"]] == trip_id]
            stop_times_rows = sorted(stop_times_rows, key=lambda stop_times_row: stop_times_row[stop_times_header["stop_sequence"]])
            endpoints = (stop_times_rows[0][stop_times_header["stop_id"]], stop_times_rows[-1][stop_times_header["stop_id"]])
            if endpoints not in longest_sequences or len(longest_sequences[endpoints]) < len(stop_times_rows):
                longest_sequences[endpoints] = [row[stop_times_header["stop_id"]] for row in stop_times_rows]


        # a list of lat, lon pairs
        paths = []
        for endpoints, sequence in longest_sequences.items():
            stops_path = [stops_to_lat_lon[stop] for stop in sequence]
            paths.append(stops_path)

        pathblob = schema.Box(paths).get_blob_string()

        # insert route information
        obj = schema.getSchemaAsObject()
        obj.routes.route.value = route_id
        obj.routes.routetitle.value = route_title
        obj.routes.color.value = purple
        obj.routes.oppositecolor.value = purple
        obj.routes.listorder.value = startorder + route_order[route_id] - 1
        obj.routes.agencyid.value = schema.CommuterRailAgencyId
        obj.routes.pathblob.value = pathblob
        obj.routes.insert()

        for stop_row in stop_rows:
            stop_id = stop_row[stops_header["stop_id"]]
            if stop_id not in stops_inserted:
                stops_inserted.add(stop_id)

                obj.stops.tag.value = stop_id
                obj.stops.title.value = stop_row[stops_header["stop_name"]]
                obj.stops.lat.value = float(stop_row[stops_header["stop_lat"]])
                obj.stops.lon.value = float(stop_row[stops_header["stop_lon"]])
                obj.stops.insert()

                obj.subway.platformorder.value = -1
                obj.subway.branch.value = "Unknown"
                obj.subway.tag.value = stop_row[stops_header["stop_id"]]
                obj.subway.insert()

            obj.stopmapping.route.value = route_id
            obj.stopmapping.tag.value = stop_row[stops_header["stop_id"]]
            obj.stopmapping.dirTag.value = None
            obj.stopmapping.insert()
            

def main():
    parser = argparse.ArgumentParser(description='Parse commuterrail data into SQL')
    parser.add_argument("gtfs_path", type=str)
    parser.add_argument("order", type=int)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "%s is not a directory" % args.gtfs_path
        exit(-1)
        
    print("BEGIN TRANSACTION;")
    startorder = args.order
    
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



    write_sql(startorder, trips, stops, routes, stop_times, shapes)
    print("END TRANSACTION;")

if __name__ == "__main__":
    main()
