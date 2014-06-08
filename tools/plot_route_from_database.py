import argparse
import sqlite3
import tempfile
import csv
import subprocess
from generate.schema import Box

def main():
    parser = argparse.ArgumentParser(description="Plot a route from database")
    parser.add_argument("sqlite_path")
    parser.add_argument("route")
    args = parser.parse_args()

    with sqlite3.connect(args.sqlite_path) as conn:
        cur = conn.cursor()
        rows = cur.execute("SELECT pathblob FROM routes where route = ?", (args.route,))
        first_row = next(rows)

        blob = first_row[0]
        paths = Box().from_bytes(blob)

        with tempfile.NamedTemporaryFile() as temp_file:
            with open(temp_file.name, "w") as f:
                writer = csv.writer(f, delimiter='\t')
                writer.writerow(['i', 'lat', 'lon'])
                for i, path in enumerate(paths):
                    for path_tuple in path:
                        writer.writerow([i, path_tuple[0], path_tuple[1]])
            subprocess.check_call(["Rscript", "plot_shape.R", temp_file.name, "out.png"])


if __name__ == "__main__":
    main()
    
    
