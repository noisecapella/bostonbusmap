RedLine = "Red"
OrangeLine = "Orange"
BlueLine = "Blue"
GreenLine = "Green"

import csv
import argparse
import os.path
import schema
import itertools
from collections import defaultdict

import sqlite3

class MbtaHeavyRail:
    def write_sql(self, cur, startorder, route, gtfs_map):
        supported_route_description = route + " Line"
        route_rows = list(gtfs_map.find_routes_by_name(supported_route_description))
        route_ids = set([route_row["route_id"] for route_row in route_rows])
        route_color = [route_row["route_color"] for route_row in route_rows][0]

        shape_rows = itertools.chain.from_iterable((gtfs_map.find_shapes_by_route(item) for item in route_ids))

        # this stores a list of list of lat, lon pairs
        print("Appending paths for %s" % supported_route_description)
        paths = []
        shape_rows = list(sorted(shape_rows, key=lambda shape: shape["shape_id"]))
        print("Adding shapes...")
        for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape["shape_id"]):
            path = [(float(row["shape_pt_lat"]), float(row["shape_pt_lon"])) for row in group_rows]
            paths.append(path)

        stop_rows = itertools.chain.from_iterable(gtfs_map.find_stops_by_route(route) for route in route_ids)


        pathblob = schema.Box(paths).get_blob_string()
    
        print("Inserting route information for %s" % supported_route_description)
        # insert route information
        obj = schema.getSchemaAsObject()
        obj.routes.route.value = route
        obj.routes.routetitle.value = route
        obj.routes.color.value = int("0x%s" % route_color, 0)
        obj.routes.oppositecolor.value = int("0x%s" % route_color, 0)
        obj.routes.listorder.value = startorder
        obj.routes.agencyid.value = schema.SubwayAgencyId
        obj.routes.pathblob.value = pathblob
        cur.execute(obj.routes.insert())

        print("Adding stops...")
        stop_ids = set()
        for stop_row in stop_rows:
            stop_id = stop_row["stop_id"]
            if stop_id not in stop_ids:
                obj.stops.tag.value = stop_row["stop_id"]
                obj.stops.title.value = stop_row["stop_name"]
                obj.stops.lat.value = float(stop_row["stop_lat"])
                obj.stops.lon.value = float(stop_row["stop_lon"])
                cur.execute(obj.stops.insert())

                obj.stopmapping.route.value = route
                obj.stopmapping.tag.value = stop_row["stop_id"]
                cur.execute(obj.stopmapping.insert())

                stop_ids.add(stop_id)

        print("Done for %s" % supported_route_description)
        return (1)

    def generate(self, conn, startorder, gtfs_map):

        count = 0
        cur = conn.cursor()
        for route in ["Red", "Orange", "Blue", "Green"]:

            count += self.write_sql(cur, startorder + count, route, gtfs_map)

        conn.commit()
        cur.close()
        return (count + startorder)


