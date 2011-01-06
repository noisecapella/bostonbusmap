#!/bin/sh
wget "http://webservices.nextbus.com/service/publicXMLFeed?a=mbta&command=routeList" -O routeList
cat routeList | grep route | awk -F"\"" '{ print $2 }' > routes

for each in `cat routes`; do wget "http://webservices.nextbus.com/service/publicXMLFeed?a=mbta&command=routeConfig&terse&r=$each" -O routeConfig$each; sleep 10; done

touch routeconfig_full.xml
echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?> " >> routeconfig_full.xml
echo "<body copyright=\"All data copyright MBTA 2011.\">" >> routeconfig_full.xml

for each in `cat routes`; do cat routeConfig$each | awk '$0 ~ /<body/ { next } $0 ~ /body>/ { next } $0 ~ /<\?xml/ { next } { print }' >> routeconfig_full.xml; done

echo "</body>" >> routeconfig_full.xml

gzip routeconfig_full.xml
