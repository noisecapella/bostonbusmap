#!/bin/bash
PROGNAME=$(basename $0)

set -e
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
python create_tables.py > sql.dump
python tosql.py routeconfig.xml routeList 0 >> sql.dump
rm new.db* || true
sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz

