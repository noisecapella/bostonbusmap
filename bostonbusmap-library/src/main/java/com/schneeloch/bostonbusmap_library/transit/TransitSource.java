package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;

public interface TransitSource {

	void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			VehicleLocations busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, SAXException;

	boolean hasPaths();

	String searchForRoute(String indexingQuery, String lowercaseQuery);

	ITransitDrawables getDrawables(Schema.Routes.SourceId sourceId);

	TransitSourceTitles getRouteTitles();

	IAlerts getAlerts();
}
