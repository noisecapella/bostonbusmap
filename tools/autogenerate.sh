#!/bin/bash
PROGNAME=$(basename $0)

set -e
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
python create_tables.py > sql.dump
python commuterrail_tosql.py commuterRailRouteList 0 >> sql.dump
python subway_tosql.py subwayRouteList 12 >> sql.dump
python tosql.py routeconfig.xml routeList 15 >> sql.dump
python alerts_tosql.py routeList subwayRouteList commuterRailRouteList >> sql.dump
python calculate_times.py gtfs/stop_times.txt gtfs/trips.txt gtfs/calendar.txt >> sql.dump
rm new.db* || true
sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz

