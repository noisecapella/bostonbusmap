#!/bin/bash
set -e
PROGNAME=$(basename $0)
GTFS_DIR=$(cd gtfs/mbta; pwd)

set -e
echo "Generate Schema.java..."
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
echo "Generating database..."
(cd generate; python generate_database.py ../new.db "$GTFS_DIR")
#echo "Create tables..."
#python create_tables.py > sql.dump
#echo "Parsing Hubway data..."
#python hubway_tosql.py 0 >> sql.dump
#echo "Parsing commuter rail data..."
#python commuterrail_tosql.py "$GTFS_DIR" 1 StationOrder.csv >> sql.dump
#echo "Parsing subway data..."
#python heavyrail_tosql.py "$GTFS_DIR" 13 >> sql.dump
#echo "Parsing bus data..."
#python tosql.py routeconfig.xml routeList 16 >> sql.dump
#echo "Dumping into sqlite..."
#rm new.db* || true
#sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

