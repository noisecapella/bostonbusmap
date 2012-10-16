#!/bin/sh

python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
python create_tables.py > sql.dump
python tosql.py routeconfig.xml routeList >> sql.dump
rm new.db
sqlite3 new.db < sql.dump
gzip new.db
cp new.db.gz ../res/raw/databasegz

