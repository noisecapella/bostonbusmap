import schema
import argparse
import os

from commuterrail_paths import calculate_path
from gtfs_map import GtfsMap

purple = 0x940088


def createDirectionHash(a, b):
    return a + "|" + b

def write_sql(startorder, gtfs_map, stationorder_csv):
    trips_header, trips = gtfs_map.trips_header, gtfs_map.trips
    stops_header, stops = gtfs_map.stops_header, gtfs_map.stops
    routes_header, routes = gtfs_map.routes_header, gtfs_map.routes
    stop_times_header, stop_times = gtfs_map.stop_times_header, gtfs_map.stop_times
    shapes_header, shapes = gtfs_map.shapes_header, gtfs_map.shapes

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

        paths = calculate_path(route_id, gtfs_map, stationorder_csv)

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
    parser.add_argument("stationorder_csv", type=str)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "%s is not a directory" % args.gtfs_path
        exit(-1)
        
    print("BEGIN TRANSACTION;")
    startorder = args.order

    gtfs_map = GtfsMap(args.gtfs_path)

    write_sql(startorder, gtfs_map, args.stationorder_csv)
    print("END TRANSACTION;")

if __name__ == "__main__":
    main()
