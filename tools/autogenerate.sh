#!/bin/bash
PROGNAME=$(basename $0)

set -e
echo "Generating schema..."
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
echo "Create tables..."
python create_tables.py > sql.dump
echo "Creating SQL from transit data..."
python create_sql.py routeconfig.xml routeList gtfs StationOrder.csv >> sql.dump
echo "Calculating bound times..."
python calculate_times.py gtfs/stop_times.txt gtfs/trips.txt gtfs/calendar.txt >> sql.dump
echo "Dumping into sqlite..."
rm new.db* || true
sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

