import argparse
from generate.schema import Box
from generate.gtfs_map import GtfsMap
import itertools
import tempfile
import json
import subprocess
import csv
from generate.simplify_path import simplify_path
def main():
    parser = argparse.ArgumentParser(description="Plot a route from GTFS")
    parser.add_argument("gtfs_path")
    parser.add_argument("route")
    args = parser.parse_args()

    gtfs_map = GtfsMap(args.gtfs_path, skip_stop_times=True)
    route_ids = [args.route]
    shape_rows = itertools.chain.from_iterable((gtfs_map.find_shapes_by_route(item) for item in route_ids))

    # this stores a list of list of lat, lon pairs
    paths = []
    shape_rows = list(sorted(shape_rows, key=lambda shape: (shape["shape_id"], int(shape["shape_pt_sequence"]))))
    for shape_id, group_rows in itertools.groupby(shape_rows, lambda shape: shape["shape_id"]):
        path = [(float(row["shape_pt_lat"]), float(row["shape_pt_lon"])) for row in group_rows]
        print(len(path))
        path = simplify_path(path)
        print(len(path))
        paths.append(path)

    
        with tempfile.NamedTemporaryFile() as temp_file:
            with open(temp_file.name, "w") as f:
                writer = csv.writer(f, delimiter='\t')
                writer.writerow(['i', 'lat', 'lon'])
                for i, path in enumerate(paths):
                    for path_tuple in path:
                        writer.writerow([i, path_tuple[0], path_tuple[1]])
            subprocess.check_call(["Rscript", "plot_shape.R", temp_file.name, "out.png"])
        break
    else:
        raise Exception("No path for route %s found" % args.route)
        
if __name__ == "__main__":
    main()
