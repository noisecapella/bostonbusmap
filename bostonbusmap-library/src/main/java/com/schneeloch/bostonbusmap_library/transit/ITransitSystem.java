package com.schneeloch.bostonbusmap_library.transit;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.schneeloch.bostonbusmap_library.data.Directions;
import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
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
			String route, Optional<String> parent);

	IAlerts getAlerts();

    public void startObtainAlerts(IDatabaseAgent databaseAgent, Runnable runnable);

    /**
     * Do we know anything about vehicles for a particular transit source type
     * @param transitSourceType
     * @return
     */
    boolean hasVehicles(Schema.Routes.SourceId transitSourceType);

	ImmutableSet<Schema.Routes.SourceId> getSourceIds(Collection<String> routes);

	String getTransitSourceDescription(Schema.Routes.SourceId routeType);

	ITransitDrawables getDrawables(Location location);

	Schema.Routes.SourceId getSourceId(String route);

	ImmutableMap<String,Schema.Routes.SourceId> getSourceIdMap();
}