import schema
import argparse
import os
import requests

from gtfs_map import GtfsMap
import itertools
purple = 0x940088

from simplify_path import simplify_path


import sqlite3
class MbtaCommuterRail:
    def write_sql(self, cur, startorder, gtfs_map):
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
            "CR-Newburyport": 12,
            "CapeFlyer": 13
        }

        stops_inserted = set()

        route_rows = list(gtfs_map.find_routes_by_route_type(schema.CommuterRailAgencyId))

        for route_row in route_rows:
            route_id = route_row["route_id"]
            route_title = route_row["route_short_name"]
            if not route_title:
                route_title = route_row["route_long_name"]
        
            trip_rows = gtfs_map.find_trips_by_route(route_id)
            trip_ids = set([trip["trip_id"] for trip in trip_rows])

            shape_rows = gtfs_map.find_sorted_shapes_by_route(route_id)

            # this stores a list of list of lat, lon pairs
            paths = []
            for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape["shape_id"]):
                path = [(float(row["shape_pt_lat"]), float(row["shape_pt_lon"])) for row in group_rows]
                path = simplify_path(path)
                paths.append(path)

            stop_rows = gtfs_map.find_stops_by_route(route_id)

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
                stop_id = stop_row["stop_id"]
                if stop_id not in stops_inserted:
                    stops_inserted.add(stop_id)

                    obj.stops.tag.value = stop_id
                    obj.stops.title.value = stop_row["stop_name"]
                    obj.stops.lat.value = float(stop_row["stop_lat"])
                    obj.stops.lon.value = float(stop_row["stop_lon"])
                    obj.stops.parent.value = stop_row["parent_station"]
                    cur.execute(obj.stops.insert())

                obj.stopmapping.route.value = route_id
                obj.stopmapping.tag.value = stop_row["stop_id"]
                cur.execute(obj.stopmapping.insert())

            print("Adding directions... for {}".format(route_id))
            for trip_row in gtfs_map.find_trips_by_route(route_id):
                obj.directions.dirTag.value = trip_row["trip_id"]
                obj.directions.dirTitleKey.value = trip_row["trip_headsign"]
                obj.directions.dirRouteKey.value = route_id
                obj.directions.dirNameKey.value = ""
                obj.directions.useAsUI.value = 1
                cur.execute(obj.directions.insert())
        return len(route_rows)
            
    def generate(self, conn, startorder, gtfs_map):
        cur = conn.cursor()
        count = self.write_sql(cur, startorder, gtfs_map)
        conn.commit()
        cur.close()
        return (count + startorder)
