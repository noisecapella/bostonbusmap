#!/bin/bash
PROGNAME=$(basename $0)
GTFS_DIR=gtfs/mbta

set -e
echo "Generating schema..."
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
echo "Create tables..."
python create_tables.py > sql.dump
echo "Parsing commuter rail data..."
python commuterrail_tosql.py "$GTFS_DIR" 0 StationOrder.csv >> sql.dump
echo "Parsing subway data..."
python heavyrail_tosql.py "$GTFS_DIR" 12 >> sql.dump
echo "Parsing bus data..."
python tosql.py routeconfig.xml routeList 15 >> sql.dump
echo "Calculating bound times..."
python calculate_times.py "$GTFS_DIR"/stop_times.txt "$GTFS_DIR"/trips.txt "$GTFS_DIR"/calendar.txt >> sql.dump
echo "Dumping into sqlite..."
rm new.db* || true
sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

