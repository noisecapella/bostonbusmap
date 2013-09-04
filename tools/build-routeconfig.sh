#!/bin/sh
prefix="webservices"

echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?> " > routeconfig_full.xml
echo "<body copyright=\"All data copyright $agency 2011.\">" >> routeconfig_full.xml

for agency in mbta
do
    echo "Downloading for agency $agency..."

    wget "http://$prefix.nextbus.com/service/publicXMLFeed?a=$agency&command=routeList" -O routeList_$agency --quiet
    cat routeList_$agency | grep route | awk -F"\"" '{ print $2 }' > routes_$agency

    for each in `cat routes_$agency`
    do
	if wget "http://$prefix.nextbus.com/service/publicXMLFeed?a=$agency&command=routeConfig&verbose&r=$each" -O routeConfig$each --quiet
	then
	    echo "Downloaded $each"
	else
	    echo "Failed to download $each"
	fi
	sleep 10
    done

    touch routeconfig_full.xml

    for each in `cat routes_$agency`
    do 
	cat routeConfig$each | awk 'BEGIN { in_path = 0 } $0 ~ /<body/ { next } $0 ~ /body>/ { next } $0 ~ /<\?xml/ { next } { print }' >> routeconfig_full.xml
    done
    
done
echo "</body>" >> routeconfig_full.xml

gzip -f routeconfig_full.xml
