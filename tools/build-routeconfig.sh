#!/bin/sh
prefix="webservices"


for agency in ttc
do
    echo "Downloading for agency $agency..."

    echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?> " > routeconfig_full_${agency}.xml
    echo "<body copyright=\"All data copyright $agency 2011.\">" >> routeconfig_full_${agency}.xml

    wget "http://$prefix.nextbus.com/service/publicXMLFeed?a=$agency&command=routeList" -O routeList_$agency --quiet
    cat routeList_$agency | grep route | awk -F"\"" '{ print $2 }' > routes_$agency

    for each in `cat routes_$agency`
    do
	if wget "http://$prefix.nextbus.com/service/publicXMLFeed?a=$agency&command=routeConfig&verbose&r=$each" -O routeConfig${each}_${agency} --quiet
	then
	    echo "Downloaded $each"
	else
	    echo "Failed to download $each"
	fi
	sleep 10
    done

    touch routeconfig_full_${agency}.xml

    for each in `cat routes_$agency`
    do 
	cat routeConfig${each}_${agency} | awk 'BEGIN { in_path = 0 } $0 ~ /<body/ { next } $0 ~ /body>/ { next } $0 ~ /<\?xml/ { next } { print }' >> routeconfig_full_${agency}.xml
    done
    
done
echo "</body>" >> routeconfig_full_${agency}.xml

gzip -f routeconfig_full_${agency}.xml
