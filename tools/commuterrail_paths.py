import os
import argparse
from gtfs_map import GtfsMap

def calculate_path(route, gtfs_map):
    trips_header, trips = gtfs_map.trips_header, gtfs_map.trips
    stops_header, stops = gtfs_map.stops_header, gtfs_map.stops
    routes_header, routes = gtfs_map.routes_header, gtfs_map.routes
    stop_times_header, stop_times = gtfs_map.stop_times_header, gtfs_map.stop_times
    shapes_header, shapes = gtfs_map.shapes_header, gtfs_map.shapes


    trip_rows = [trip_row for trip_row in trips
                 if trip_row[trips_header["route_id"]] == route]
    trip_ids = set([trip[trips_header["trip_id"]] for trip in trip_rows])

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

    sequences = {}
    for trip_id in trip_ids: 
        stop_times_rows = [stop_times_row for stop_times_row in all_stop_times_rows
                           if stop_times_row[stop_times_header["trip_id"]] == trip_id]
        stop_times_rows = sorted(stop_times_rows, key=lambda stop_times_row: int(stop_times_row[stop_times_header["stop_sequence"]]))
        
        sequences[trip_id] = [(row[stop_times_header["stop_id"]], int(row[stop_times_header["stop_sequence"]])) for row in stop_times_rows]

    # a list of lat, lon pairs
    paths = []
    for trip_id, sequence in sequences.items():
        #stops_path = [stops_to_lat_lon[stop] for stop in sequence]
        stops_path = []
        prev_sequence_num = None
        # break up sequence into contiguous pieces. If two stops are not
        # contiguous we should not draw a line connecting the stops
        for stop, sequence_num in sequence:
            if prev_sequence_num is not None and prev_sequence_num != sequence_num - 1:
                #print "Break: %s, %s, %d, %d" % (trip_id, stop, prev_sequence_num, sequence_num)
                if len(stops_path) > 1:
                    paths.append(stops_path)
                stops_path = []
            stops_path.append(stops_to_lat_lon[stop])
            prev_sequence_num = sequence_num
                
        paths.append(stops_path)
    return paths

def main():
    parser = argparse.ArgumentParser(description='Write CSV of paths')
    parser.add_argument("gtfs_path", type=str)
    parser.add_argument("route", type=str)
    args = parser.parse_args()

    if not os.path.isdir(args.gtfs_path):
        print "%s is not a directory" % args.gtfs_path
        exit(-1)
        
    gtfs_map = GtfsMap(args.gtfs_path)
    paths = calculate_path(args.route, gtfs_map)

    print "id,lat,lon"
    for i, path in enumerate(paths):
        for lat, lon in path:
            print "%d,%f,%f" % (i, lat, lon)

if __name__ == "__main__":
    main()
