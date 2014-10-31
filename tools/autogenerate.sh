#!/bin/bash
set -e
PROGNAME=$(basename $0)
GTFS_DIR=$(cd gtfs/ttc; pwd)

set -e
echo "Generate Schema.java..."
python generate_schema.py > ../src/boston/Bus/Map/database/Schema.java
echo "Generating database..."
(cd generate; python generate_database.py ../new.db "$GTFS_DIR")
rm -f new.db.gz
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

