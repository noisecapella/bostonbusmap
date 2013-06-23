#BostonBusMap
This app shows locations and predicts bus times in Boston and nearby cities, using GPS info from the MBTA.
Please email bostonbusmap@gmail.com if you have any problem, and please report a crash if you see one! Twitter feed for bug updates: @bostonbusmap

#Basic usage
- choose a mode by clicking the button on the upper right corner. You can see all vehicles at once, vehicles for a single route, bus predictions for a single route, or only bus predictions on favorited stops
- choose a route by clicking the menu button and clicking "Routes". You can also type in the route in the search textbox
- click on a stop to show the next few buses that are driving by.
- click 'more info' to show more predictions for that stop
- click 'report problem' to report a problem with the predictions
- click the star in the popup (see screenshot) to mark a stop as a favorite
- choose a favorite stop by pressing the menu button and clicking "Favorite stops"
- center on your current location by clicking the menu button and choosing "My Location". The speed and accuracy of location information depends on your phone location settings. You can tweak those in Home -> Settings -> Location & Security
- draw the path along a route by clicking the menu button, going to Settings and selecting "Show route path"
- this app should work for all touchscreen devices that are Android 1.6 and up (which means pretty much all Android phones and tablets). Please let me know if something doesn't work

#Build
You'll need to `git clone git@github.com:bostonbusmap/android-mapviewballoons.git`. This project references that code to draw the mapview balloons.

You'll also need to download [the MBTA's GTFS data](http://www.mbta.com/rider_tools/developers/default.asp?id=21895), and unzip it into tools/gtfs.

Then, `cd` into the `tools` directory, then run `autogenerate.sh` to create and populate the database file, which gets copied to `res/raw/databasegz`. `routeconfig.xml` and `routeLists` come from data collected from nextbus.com for a particular transit agency. For the MBTA, see their developer website for how to get their subway and commuter rail info.

Otherwise this is a standard Android project. You might need to change the paths a little bit but you shouldn't need to install anything unexpected.
Note that you may need a separate Google Maps API key to test with

#Branches

There are a few important git branches corresponding to different apps for different cities:

* `master` - where all code gets merged
* `mbta` - This is the same as master except with updated MBTA-specific transit data.
* `la` - This is master with MBTA-specific commuter rail and subway code removed, and with Los Angeles NextBus data added. This also changes the namespace and some of the graphics to fit the Los Angelbus app.
* `toronto` - Similar to `la` branch
* `umich` - This was branched off an early build of BostonBusMap. It focuses on the University of Michigan's bus system for Ann Arbor.