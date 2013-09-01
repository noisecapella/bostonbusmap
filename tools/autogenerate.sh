#!/bin/bash
PROGNAME=$(basename $0)
GTFS_DIR=gtfs/nyc

set -e
echo "Generating schema..."
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
echo "Create tables..."
python create_tables.py > sql.dump
echo "Parsing bus data..."
# gtfs files from MTA organized such that ./gtfs/nyc/bronx/calendar.txt exists, etc
python gtfs_tosql.py "$GTFS_DIR" 0 >> sql.dump
#echo "Calculating bound times..."
#python calculate_times.py "$GTFS_DIR"/stop_times.txt "$GTFS_DIR"/trips.txt "$GTFS_DIR"/calendar.txt >> sql.dump
echo "Dumping into sqlite..."
rm new.db* || true
sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

