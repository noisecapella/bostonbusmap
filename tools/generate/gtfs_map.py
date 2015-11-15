import os
import csv
from datetime import datetime, timedelta
import sqlite3
from collections import namedtuple

Prediction = namedtuple('Prediction', ['stop_id', 'trip_id', 'estimated_minutes'])
Location = namedtuple('Location', ['trip_id', 'lat', 'lon', 'stop_id'])

class GtfsMap(object):
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

        self._drop_table("trips")
        self._create_table(gtfs_path, "trips", {"route_id" : "TEXT",
                                                "service_id" : "TEXT",
                                                "trip_id" : "TEXT PRIMARY KEY",
                                                "trip_headsign": "TEXT",
                                                "trip_short_name" : "TEXT",
                                                "direction_id" : "INTEGER",
                                                "block_id" : "TEXT",
                                                "shape_id" : "TEXT"})
        self._drop_table("stops")
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
        self._drop_table("routes")
        self._create_table(gtfs_path, "routes", {"route_id": "TEXT PRIMARY KEY",
                                                 "agency_id": "TEXT",
                                                 "route_short_name": "TEXT",
                                                 "route_long_name": "TEXT",
                                                 "route_desc": "TEXT",
                                                 "route_type": "INTEGER",
                                                 "route_url": "TEXT",
                                                 "route_color": "TEXT",
                                                 "route_text_color": "TEXT"})
        self._drop_table("stop_times")
        self._create_table(gtfs_path, "stop_times", {"trip_id": "TEXT",
                                                     "arrival_time": "TEXT",
                                                     "departure_time": "TEXT",
                                                     "stop_id": "TEXT",
                                                     "stop_sequence": "INTEGER",
                                                     "stop_headsign": "TEXT",
                                                     "pickup_type": "INTEGER",
                                                     "drop_off_type": "INTEGER"})
        self._drop_table("shapes")
        self._create_table(gtfs_path, "shapes", {"shape_id": "TEXT",
                                                 "shape_pt_lat": "TEXT",
                                                 "shape_pt_lon": "TEXT",
                                                 "shape_pt_sequence": "INTEGER",
                                                 "shape_dist_traveled": "TEXT"})
        self._drop_table("calendar")
        self._create_table(gtfs_path, "calendar", {"service_id" : "TEXT",
                                                   "monday" : "INTEGER",
                                                   "tuesday" : "INTEGER",
                                                   "wednesday" : "INTEGER",
                                                   "thursday" : "INTEGER",
                                                   "friday" : "INTEGER",
                                                   "saturday" : "INTEGER",
                                                   "sunday" : "INTEGER",
                                                   "start_date" : "TEXT",
                                                   "end_date" : "TEXT"})
        self._drop_table("calendar_dates")
        self._create_table(gtfs_path, "calendar_dates", {"service_id" : "TEXT",
                                                         "date" : "TEXT",
                                                         "exception_type" : "INTEGER"})
                                                   

        self._import_table(gtfs_path, "trips")
        self._create_index("trips", "trip_id")
        self._create_index("trips", "shape_id")
        self._create_index("trips", "route_id")
        self._create_index("trips", "service_id")
        self._import_table(gtfs_path, "stops")
        self._create_index("stops", "stop_id")
        self._import_table(gtfs_path, "routes")
        self._create_index("routes", "route_id")
        if not skip_stop_times:
            self._import_table(gtfs_path, "stop_times")
            self._create_index("stop_times", "stop_id")
            self._create_index("stop_times", "trip_id")
            self._create_index("stop_times", "departure_time")
        self._import_table(gtfs_path, "shapes")
        self._create_index("shapes", "shape_id")
        self._import_table(gtfs_path, "calendar")
        self._import_table(gtfs_path, "calendar_dates")
        
    def _import_table(self, gtfs_path, table):
        path = os.path.join(gtfs_path, table + ".txt")
        if not os.path.exists(path):
            return
        with open(path) as f:
            reader = csv.reader(f)
            header = next(reader)
            
            joined_keys = ",".join(("'%s'" % item) for item in header)
            joined_values = ",".join("?" for item in header)
            
            query = "INSERT INTO %s (%s) VALUES (%s)" % (table, joined_keys, joined_values)
            self._db.executemany(query, reader)

    def _drop_table(self, table):
        self._db.execute("DROP TABLE IF EXISTS %s" % table)


    def _create_table(self, gtfs_path, table, types):
        path = os.path.join(gtfs_path, table + ".txt")
        if not os.path.exists(path):
            return
        with open(path) as f:
            reader = csv.reader(f)
            columns = next(reader)
            
            for column in columns:
                if column not in types:
                    print("Type for column not found: %s" % column)
                    type = "TEXT"
                else:
                    type = types[column]
            joined_columns = ",".join(["%s %s" % (column, type) for column in columns])
            self._db.execute("CREATE TABLE %s (%s)" % (table, joined_columns))


    def _create_index(self, table, column):
        self._db.execute("CREATE INDEX idx_%s_%s ON %s (%s)" % (table, column, table, column))
    
    def _query(self, query, parameters):
        return (dict(row) for row in self._db.execute(query, parameters))

    def find_routes_by_name(self, name):
        return self._query("SELECT * FROM routes WHERE route_long_name = ? OR route_short_name = ?", (name, name))

    def find_shapes_by_route(self, route):
        return self._query("SELECT DISTINCT shapes.* FROM shapes JOIN trips ON shapes.shape_id = trips.shape_id WHERE route_id = ?", (route,))

    def find_sorted_shapes_by_route(self, route):
        shape_rows = self.find_shapes_by_route(route)
        return sorted(shape_rows, key=lambda shape: (shape["shape_id"], int(shape["shape_pt_sequence"])))

    def find_shapes_by_shape(self, shape):
        return self._query("SELECT DISTINCT shapes.* FROM shapes WHERE shape_id = ?", (shape,))

    def find_routes_by_route_type(self, route_type):
        return self._query("SELECT routes.* FROM routes WHERE route_type = ?", (route_type,))

    def find_all_stops(self):
        return self._query("SELECT stops.* FROM stops")

    def find_stops_by_route(self, route):
        # not entirely sure why I need to do this in two different steps
        # you would think the stop -> stop_times join would be efficient enough
        # not to require this
        stop_ids = [row['stop_id'] for row in self._query("SELECT DISTINCT stop_times.stop_id FROM stop_times JOIN trips ON stop_times.trip_id = trips.trip_id WHERE route_id = ?", (route,))]
        question_marks = ", ".join("?" for stop_id in stop_ids)

        return self._query("SELECT stops.* FROM stops WHERE stop_id IN (%s)" % question_marks, stop_ids)

    def find_trips_by_route(self, route):
        return self._query("SELECT trips.* FROM trips WHERE route_id = ?", (route,))

    def find_trips_for_datetime(self, date):
        day_of_week = date.weekday()
        query = "SELECT s_t.*, route_id FROM calendar AS c JOIN trips AS t ON c.service_id = t.service_id JOIN stop_times AS s_t ON s_t.trip_id = t.trip_id "
        if day_of_week == 0:
            query += "WHERE monday = 1"
        elif day_of_week == 1:
            query += "WHERE tuesday = 1"
        elif day_of_week == 2:
            query += "WHERE wednesday = 1"
        elif day_of_week == 3:
            query += "WHERE thursday = 1"
        elif day_of_week == 4:
            query += "WHERE friday = 1"
        elif day_of_week == 5:
            query += "WHERE saturday = 1"
        elif day_of_week == 6:
            query += "WHERE sunday = 1"


        # TODO: appropriate time zone handling for times, handling of times past midnight
        # TODO: calendar_dates
        date_string = date.strftime("%Y%m%d")
        query += " AND start_date <= ? AND end_date >= ? ORDER BY departure_time ASC"

        return self._query(query, (date_string, date_string))

 
    def __del__(self):
        self._db.commit()
        self._db.close()
