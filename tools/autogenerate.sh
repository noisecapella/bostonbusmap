#!/bin/bash
set -e
PROGNAME=$(basename $0)
GTFS_DIR=$(cd gtfs/ttc; pwd)

set -e
echo "Generate Schema.java..."
python generate_schema.py > ../bostonbusmap-library/src/main/java/com/schneeloch/bostonbusmap_library/database/Schema.java
echo "Generating database..."
(cd generate; python generate_database.py ../new.db "$GTFS_DIR")
rm -f new.db.gz
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

