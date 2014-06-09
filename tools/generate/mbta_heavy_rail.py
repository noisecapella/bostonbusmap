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

import sqlite3

class MbtaHeavyRail:
    def write_sql(self, cur, startorder, route, gtfs_map):
        supported_route_description = route + " Line"
        route_rows = gtfs_map.find_routes_by_name(supported_route_description)
        route_ids = set([route_row["route_id"] for route_row in route_rows])

        shape_rows = itertools.chain.from_iterable((gtfs_map.find_shapes_by_route(item) for item in route_ids))

        # this stores a list of list of lat, lon pairs
        paths = []
        shape_rows = list(sorted(shape_rows, key=lambda shape: shape["shape_id"]))
        for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape["shape_id"]):
            path = [(float(row["shape_pt_lat"]), float(row["shape_pt_lon"])) for row in group_rows]
            paths.append(path)

        stop_rows = itertools.chain.from_iterable(gtfs_map.find_stops_by_route(route) for route in route_ids)


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
        cur.execute(obj.routes.insert())

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

        return (1)

    def generate(self, conn, startorder, gtfs_map):

        count = 0
        cur = conn.cursor()
        for route in ["Red", "Orange", "Blue"]:

            count += self.write_sql(cur, startorder + count, route, gtfs_map)

        conn.commit()
        cur.close()
        return (count + startorder)


