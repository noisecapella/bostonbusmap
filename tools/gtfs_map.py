import os
import csv
class GtfsMap:
    def __init__(self, gtfs_path):
        trip_path = os.path.join(gtfs_path, "trips.txt")
        trips_tup = self.read_rows(trip_path)
        self.trips_header, self.trips = trips_tup

        stop_path = os.path.join(gtfs_path, "stops.txt")
        stops_tup = self.read_rows(stop_path)
        self.stops_header, self.stops = stops_tup
        
        route_path = os.path.join(gtfs_path, "routes.txt")
        routes_tup = self.read_rows(route_path)
        self.routes_header, self.routes = routes_tup

        stop_times_path = os.path.join(gtfs_path, "stop_times.txt")
        stop_times_tup = self.read_rows(stop_times_path)
        self.stop_times_header, self.stop_times = stop_times_tup

        shapes_path = os.path.join(gtfs_path, "shapes.txt")
        shapes_tup = self.read_rows(shapes_path)
        self.shapes_header, self.shapes = shapes_tup


    def make_index_map(self, array):
        ret = {}
        for i, item in enumerate(array):
            ret[item] = i
        return ret


    def read_rows(self, path):
        ret = {}

        with open(path) as f:
            reader = csv.reader(f)

            header = self.make_index_map(next(reader))
            return header, [row for row in reader]
        
