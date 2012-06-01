#!/bin/sh
agency="lametro"
prefix="webservices"

wget "http://$prefix.nextbus.com/service/publicXMLFeed?a=$agency&command=routeList" -O routeList
cat routeList | grep route | awk -F"\"" '{ print $2 }' > routes

for each in `cat routes`; do wget "http://$prefix.nextbus.com/service/publicXMLFeed?a=$agency&command=routeConfig&verbose&r=$each" -O routeConfig$each; sleep 10; done

touch routeconfig_full.xml
echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?> " >> routeconfig_full.xml
echo "<body copyright=\"All data copyright $agency 2011.\">" >> routeconfig_full.xml

#for each in `cat routes`; do cat routeConfig$each | awk '$0 ~ /<body/ { next } $0 ~ /body>/ { next } $0 ~ /<\?xml/ { next } { print }' >> routeconfig_full.xml; done
for each in `cat routes`; do cat routeConfig$each | awk 'BEGIN { in_path = 0 } $0 ~ /<body/ { next } $0 ~ /body>/ { next } $0 ~ /<\?xml/ { next } $0 ~ /<path/ { in_path=1; next; } $0 ~ /path>/ { in_path=0; next; } { if (!in_path || in_path) { print } }' >> routeconfig_full.xml; done

echo "</body>" >> routeconfig_full.xml

gzip -f routeconfig_full.xml
