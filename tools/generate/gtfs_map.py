import os
import csv
from datetime import datetime
import sqlite3

class GtfsMap:
    def __init__(self, gtfs_path, reinitialize=True, skip_stop_times=False):
        self._db = sqlite3.connect("./temp_gtfs.db")
        self._db.row_factory = sqlite3.Row


        calendar_path = os.path.join(gtfs_path, "calendar.txt")
        self.last_date = None
        with open(calendar_path) as f:
            for row in csv.DictReader(f):
                date = datetime.strptime(row["end_date"], '%Y%m%d')
                if self.last_date is None or self.last_date < date:
                    self.last_date = date

        if not reinitialize:
            return

        self._db.execute("DROP TABLE IF EXISTS trips")
        self._db.execute("DROP TABLE IF EXISTS stops")
        self._db.execute("DROP TABLE IF EXISTS routes")
        self._db.execute("DROP TABLE IF EXISTS stop_times")
        self._db.execute("DROP TABLE IF EXISTS shapes")
        
        self._create_table(gtfs_path, "trips", {"route_id" : "TEXT",
                                                "service_id" : "TEXT",
                                                "trip_id" : "TEXT PRIMARY KEY",
                                                "trip_headsign": "TEXT",
                                                "trip_short_name" : "TEXT",
                                                "direction_id" : "INTEGER",
                                                "block_id" : "TEXT",
                                                "shape_id" : "TEXT"})
        self._create_table(gtfs_path, "stops", {"stop_id": "TEXT PRIMARY KEY",
                                                "stop_code": "TEXT",
                                                "stop_name": "TEXT",
                                                "stop_desc": "TEXT",
                                                "stop_lat": "TEXT",
                                                "stop_lon": "TEXT",
                                                "zone_id": "TEXT",
                                                "stop_url": "TEXT",
                                                "location_type": "INTEGER",
                                                "parent_station": "TEXT"})
        self._create_table(gtfs_path, "routes", {"route_id": "TEXT PRIMARY KEY",
                                                 "agency_id": "TEXT",
                                                 "route_short_name": "TEXT",
                                                 "route_long_name": "TEXT",
                                                 "route_desc": "TEXT",
                                                 "route_type": "INTEGER",
                                                 "route_url": "TEXT",
                                                 "route_color": "TEXT",
                                                 "route_text_color": "TEXT"})
        self._create_table(gtfs_path, "stop_times", {"trip_id": "TEXT",
                                                     "arrival_time": "TEXT",
                                                     "departure_time": "TEXT",
                                                     "stop_id": "TEXT",
                                                     "stop_sequence": "INTEGER",
                                                     "stop_headsign": "TEXT",
                                                     "pickup_type": "INTEGER",
                                                     "drop_off_type": "INTEGER"})
        self._create_table(gtfs_path, "shapes", {"shape_id": "TEXT",
                                                 "shape_pt_lat": "TEXT",
                                                 "shape_pt_lon": "TEXT",
                                                 "shape_pt_sequence": "INTEGER",
                                                 "shape_dist_traveled": "TEXT"})

        self._import_table(gtfs_path, "trips")
        self._create_index("trips", "shape_id")
        self._create_index("trips", "route_id")
        self._import_table(gtfs_path, "stops")
        self._import_table(gtfs_path, "routes")
        if not skip_stop_times:
            self._import_table(gtfs_path, "stop_times")
            self._create_index("stop_times", "stop_id")
            self._create_index("stop_times", "trip_id")
        self._import_table(gtfs_path, "shapes")
        self._create_index("shapes", "shape_id")
        
    def _import_table(self, gtfs_path, table):
        path = os.path.join(gtfs_path, table + ".txt")
        with open(path) as f:
            reader = csv.reader(f)
            header = next(reader)
            
            joined_keys = ",".join(("'%s'" % item) for item in header)
            joined_values = ",".join("?" for item in header)
            
            query = "INSERT INTO %s (%s) VALUES (%s)" % (table, joined_keys, joined_values)
            self._db.executemany(query, reader)

    def _create_table(self, gtfs_path, table, types):
        path = os.path.join(gtfs_path, table + ".txt")
        with open(path) as f:
            reader = csv.reader(f)
            columns = next(reader)
            
            for column in columns:
                if column not in types:
                    print ("Type for column not found: %s" % column)
                    type = "TEXT"
                else:
                    type = types[column]
            joined_columns = ",".join(["%s %s" % (column, type) for column in columns])
            self._db.execute("CREATE TABLE %s (%s)" % (table, joined_columns))


    def _create_index(self, table, column):
        self._db.execute("CREATE INDEX idx_%s_%s ON %s (%s)" % (table, column, table, column))
    def find_routes_by_name(self, name):
        return (dict(row) for row in self._db.execute("SELECT * FROM routes WHERE route_long_name = ? OR route_short_name = ?", (name, name)))

    def find_shapes_by_route(self, route):
        return (dict(row) for row in self._db.execute("SELECT DISTINCT shapes.* FROM shapes JOIN trips ON shapes.shape_id = trips.shape_id WHERE route_id = ?", (route,)))

    def find_routes_by_route_type(self, route_type):
        return (dict(row) for row in self._db.execute("SELECT routes.* FROM routes WHERE route_type = ?", (route_type,)))

    def find_stops_by_route(self, route):
        return (dict(row) for row in self._db.execute("SELECT DISTINCT stops.* FROM stops JOIN stop_times ON stop_times.stop_id = stops.stop_id JOIN trips ON stop_times.trip_id = trips.trip_id WHERE route_id = ?", (route,)))

    def find_trips_by_route(self, route):
        return (dict(row) for row in self._db.execute("SELECT trips.* FROM trips WHERE route_id = ?", (route,)))

    def find_stops_by_route_ids(self):
        pass

    def __del__(self):
        self._db.commit()
        self._db.close()
