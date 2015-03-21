package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RoutePool;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.Selection;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.VehicleLocations;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;

public interface ITransitSystem {

	public abstract void setDefaultTransitSource(ITransitDrawables busDrawables,
			ITransitDrawables subwayDrawables,
			ITransitDrawables commuterRailDrawables,
			ITransitDrawables hubwayDrawables,
			IDatabaseAgent databaseAgent);

	public abstract TransitSource getDefaultTransitSource();

	public abstract TransitSource getTransitSource(String routeToUpdate);

	public abstract RouteTitles getRouteKeysToTitles();

	public abstract void refreshData(RouteConfig routeConfig,
			Selection selection, int maxStops, double centerLatitude,
			double centerLongitude,
			VehicleLocations busMapping,
			RoutePool routePool, Directions directions, Locations locations)
			throws IOException, ParserConfigurationException, SAXException;

	public abstract String searchForRoute(String indexingQuery,
			String lowercaseQuery);

	public abstract StopLocation createStop(float latitude, float longitude,
			String stopTag, String stopTitle,
			String route);

	IAlerts getAlerts();

    public void startObtainAlerts(IDatabaseAgent databaseAgent);

    TransitSource getTransitSourceByRouteType(Schema.Routes.SourceId routeType);
}