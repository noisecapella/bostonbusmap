package boston.Bus.Map.parser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.CommuterRailPrediction;
import boston.Bus.Map.data.CommuterTrainLocation;
import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.transit.CommuterRailTransitSource;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.LogUtil;

public class CommuterRailPredictionsFeedParser
{
	private final RouteConfig routeConfig;
	private final Directions directions;

	private final Map<String, Integer> indexes = Maps.newHashMap();

	
	private final ConcurrentHashMap<String, BusLocation> busMapping;
	private final RouteTitles routeKeysToTitles;

	public CommuterRailPredictionsFeedParser(RouteConfig routeConfig, Directions directions,
			ConcurrentHashMap<String, BusLocation> busMapping, RouteTitles routeKeysToTitles)
	{
		this.routeConfig = routeConfig;
		this.directions = directions;
		this.busMapping = busMapping;
		this.routeKeysToTitles = routeKeysToTitles;

	}

	private void clearPredictions(String route) throws IOException
	{
		if (routeConfig != null)
		{
			for (StopLocation stopLocation : routeConfig.getStops())
			{
				stopLocation.clearPredictions(routeConfig);
			}
		}
	}

	public void runParse(Reader data) throws IOException
	{
		TODO
	}
}
