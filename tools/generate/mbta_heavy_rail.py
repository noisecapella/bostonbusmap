import csv
import argparse
import os.path
import schema
import itertools
from collections import defaultdict
from simplify_path import simplify_path
import sqlite3

class MbtaHeavyRail:
    def write_sql(self, cur, startorder, route_ids, as_route, gtfs_map):
        route_rows = [list(gtfs_map.find_routes_by_id(route_id))[0] for route_id in route_ids]
        route_color = [route_row["route_color"] for route_row in route_rows][0]

        shape_rows = itertools.chain.from_iterable((gtfs_map.find_sorted_shapes_by_route(item) for item in route_ids))

        # this stores a list of list of lat, lon pairs
        print("Appending paths for %s" % as_route)
        paths = []
        shape_rows = list(sorted(shape_rows, key=lambda shape: shape["shape_id"]))
        print("Adding shapes...")

        # todo: sorted?
        for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape["shape_id"]):
            path = [(float(row["shape_pt_lat"]), float(row["shape_pt_lon"])) for row in group_rows]
            path = simplify_path(path)
            paths.append(path)

        stop_rows = itertools.chain.from_iterable(gtfs_map.find_stops_by_route(route) for route in route_ids)


        pathblob = schema.Box(paths).get_blob_string()
    
        print("Inserting route information for %s" % as_route)
        # insert route information
        obj = schema.getSchemaAsObject()
        obj.routes.route.value = as_route
        obj.routes.routetitle.value = as_route
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
                obj.stops.parent.value = stop_row["parent_station"]
                cur.execute(obj.stops.insert())

                obj.stopmapping.route.value = as_route
                obj.stopmapping.tag.value = stop_row["stop_id"]
                cur.execute(obj.stopmapping.insert())

                stop_ids.add(stop_id)

        for route_id in route_ids:
            print("Adding directions for {}...".format(route_id))
            for trip_row in gtfs_map.find_trips_by_route(route_id):
                obj.directions.dirTag.value = trip_row["trip_id"]
                obj.directions.dirTitleKey.value = trip_row["trip_headsign"]
                obj.directions.dirRouteKey.value = as_route
                obj.directions.dirNameKey.value = ""
                obj.directions.useAsUI.value = 1
                cur.execute(obj.directions.insert())
        
                
        print("Done for %s" % as_route)
        return (1)

    def generate(self, conn, startorder, gtfs_map):

        cur = conn.cursor()
        startorder += self.write_sql(cur, startorder, ["Red"], "Red", gtfs_map) 
        startorder += self.write_sql(cur, startorder, ["Orange"], "Orange", gtfs_map) 
        startorder += self.write_sql(cur, startorder, ["Blue"], "Blue", gtfs_map) 
        startorder += self.write_sql(cur, startorder, ["Green-B", "Green-C", "Green-D", "Green-E"], "Green", gtfs_map)
        startorder += self.write_sql(cur, startorder, ["Mattapan"], "Mattapan", gtfs_map)

        conn.commit()
        cur.close()
        return startorder


