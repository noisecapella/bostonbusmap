#!/bin/bash
set -e
PROGNAME=$(basename $0)
GTFS_DIR=$(cd gtfs/mbta; pwd)

set -e
echo "Generate Schema.java..."
python3 generate_schema.py > ../bostonbusmap-library/src/main/java/com/schneeloch/bostonbusmap_library/database/Schema.java
echo "Generating database..."
(cd generate; python3 generate_database.py ../new.db "$GTFS_DIR")
rm -f new.db.gz
gzip new.db
cp new.db.gz ../res/raw/databasegz
echo "Done!"

