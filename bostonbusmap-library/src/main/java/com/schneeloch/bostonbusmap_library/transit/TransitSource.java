package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.schneeloch.bostonbusmap_library.data.BusLocation;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;

public interface TransitSource {

	void refreshData(RouteConfig routeConfig, Selection selection,
			int maxStops, double centerLatitude, double centerLongitude,
			VehicleLocations busMapping,
			RoutePool routePool, Directions directions, Locations locationsObj)
	throws IOException, ParserConfigurationException, SAXException;

	boolean hasPaths();

	String searchForRoute(String indexingQuery, String lowercaseQuery);

	ITransitDrawables getDrawables();

	StopLocation createStop(float latitude, float longitude, String stopTag,
			String stopTitle, String route, Optional<String> parent);

	TransitSourceTitles getRouteTitles();
	
	/**
	 * The order in which to load transit sources. Lower numbers go first. Must be unique!
	 * @return
	 */
	int getLoadOrder();

	Schema.Routes.SourceId[] getTransitSourceIds();

	/**
	 * Do we need to look at the Schema.Subway table to get branch
	 * and platform information?
	 */
	boolean requiresSubwayTable();

	IAlerts getAlerts();

	String getDescription();
}
