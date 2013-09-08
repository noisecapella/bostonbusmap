from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor
import schema
import argparse
import os

from gtfs_map import GtfsMap
import itertools
purple = 0x940088


import sqlite3
class MbtaCommuterRail:
    @inlineCallbacks
    def write_sql(self, cur, startorder, gtfs_map):
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

            shape_ids = set([trip[trips_header["shape_id"]] for trip in trip_rows])
            shape_rows = [shape_row for shape_row in shapes
                          if shape_row[shapes_header["shape_id"]] in shape_ids]

            # this stores a list of list of lat, lon pairs
            paths = []
            shape_rows = list(sorted(shape_rows, key=lambda shape: shape[shapes_header["shape_id"]]))
            for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape[shapes_header["shape_id"]]):
                path = [(float(row[shapes_header["shape_pt_lat"]]), float(row[shapes_header["shape_pt_lon"]])) for row in group_rows]
                paths.append(path)

            all_stop_times_rows = [stop_times_row for stop_times_row in stop_times
                                   if stop_times_row[stop_times_header["trip_id"]] in trip_ids]
            all_stop_times_ids = set([stop_times_row[stop_times_header["stop_id"]] for stop_times_row in all_stop_times_rows])
            stop_rows = [stop_row for stop_row in stops
                         if stop_row[stops_header["stop_id"]] in all_stop_times_ids]

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
            cur.execute(obj.routes.insert())

            for stop_row in stop_rows:
                stop_id = stop_row[stops_header["stop_id"]]
                if stop_id not in stops_inserted:
                    stops_inserted.add(stop_id)

                    obj.stops.tag.value = stop_id
                    obj.stops.title.value = stop_row[stops_header["stop_name"]]
                    obj.stops.lat.value = float(stop_row[stops_header["stop_lat"]])
                    obj.stops.lon.value = float(stop_row[stops_header["stop_lon"]])
                    cur.execute(obj.stops.insert())

                obj.stopmapping.route.value = route_id
                obj.stopmapping.tag.value = stop_row[stops_header["stop_id"]]
                cur.execute(obj.stopmapping.insert())

        yield len(route_rows)
        returnValue(len(route_rows))
            
    @inlineCallbacks
    def generate(self, conn, startorder, gtfs_map):
        cur = conn.cursor()
        count = yield self.write_sql(cur, startorder, gtfs_map)
        conn.commit()
        cur.close()
        returnValue(count + startorder)
