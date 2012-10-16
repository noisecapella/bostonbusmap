package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import skylight1.opengl.files.QuickParseUtil;

import android.content.OperationApplicationException;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import boston.Bus.Map.data.CommuterRailStopLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.data.RouteConfig.Builder;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.CommuterRailTransitSource;

/**
 * This is a fake parser until something more automated is implemented
 * @author schneg
 *
 */
public class CommuterRailRouteConfigParser
{
	private final Directions directions;
	private final Map<String, RouteConfig.Builder> map = Maps.newHashMap();
	private final CommuterRailTransitSource source;
	

	
	
	private final Map<String, Integer> indexes = Maps.newHashMap();
	
	public CommuterRailRouteConfigParser(Directions directions,
			CommuterRailTransitSource source) 
	{
		this.directions = directions;
		this.source = source;
	}

	public void writeToDatabase(RoutePool routeMapping, UpdateAsyncTask task, boolean silent) throws IOException, RemoteException, OperationApplicationException
	{
		ImmutableMap.Builder<String, RouteConfig> builder = ImmutableMap.builder();
		for (String routeTag : map.keySet()) {
			RouteConfig.Builder routeBuilder = map.get(routeTag);
			builder.put(routeTag, routeBuilder.build());
		}
		routeMapping.writeToDatabase(builder.build(), task, silent);
		directions.writeToDatabase();
	}

	private void populateStops(Reader inputStreamReader) throws IOException
	{
		BufferedReader reader = new BufferedReader(inputStreamReader, 2048);
		String[] definitions = reader.readLine().split(",");
		
		for (int i = 0; i < definitions.length; i++)
		{
			indexes.put(definitions[i], i);
		}
		
		
		/**route to a mapping of
	     direction + branch to a mapping of
	       platform order numbers to stops
		 * 
		 */
		// TODO: replace with Table
		Map<String, Map<String, SortedMap<Short, StopLocation>>> orderedStations =
			Maps.newHashMap();

		RouteTitles routeKeysToTitles = source.getRouteKeysToTitles();
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] array = line.split(",");
			String routeTitle = array[indexes.get("route_long_name")];
			if (routeTitle.endsWith(" Line"))
			{
				routeTitle = routeTitle.substring(0, routeTitle.length() - 5);
			}
			String routeKey = routeKeysToTitles.getKey(routeTitle);
			RouteConfig.Builder route = map.get(routeKey);
			
			float lat = QuickParseUtil.parseFloat(array[indexes.get("stop_lat")]);
			float lon = QuickParseUtil.parseFloat(array[indexes.get("stop_lon")]);
			String stopTitle = array[indexes.get("stop_id")];
			String direction = array[indexes.get("direction_id")];
			String stopTag = CommuterRailTransitSource.stopTagPrefix + stopTitle;
			short platformOrder = Short.parseShort(array[indexes.get("stop_sequence")]);
			String branch = array[indexes.get("Branch")];
			
			StopLocation stopLocation = route.getStop(stopTag);
			if (stopLocation == null)
			{
				stopLocation =
						new CommuterRailStopLocation.CommuterRailBuilder(
								lat, lon, source.getDrawables(), stopTag, stopTitle, 
								platformOrder, branch).build();
				route.addStop(stopTag, stopLocation);
			}
			
			stopLocation.addRoute(routeKey);
			
			Map<String, SortedMap<Short, StopLocation>> innerMapping = orderedStations.get(routeKey);
			if (innerMapping == null)
			{
				innerMapping = Maps.newHashMap();
				orderedStations.put(routeKey, innerMapping);
			}
			
			//mapping of (direction plus branch plus platform order) to a stop
			//for example, key is NBAshmont3 for fields corner
			
			String combinedDirectionBranch = createDirectionHash(direction, branch);
			SortedMap<Short, StopLocation> innerInnerMapping = innerMapping.get(combinedDirectionBranch);
			if (innerInnerMapping == null)
			{
				innerInnerMapping = Maps.newTreeMap();
				innerMapping.put(combinedDirectionBranch, innerInnerMapping);
			}
			
			innerInnerMapping.put(platformOrder, stopLocation);
		}
		
		//TODO: workarounds
		
		//path
		for (String route : orderedStations.keySet())
		{
			
			Map<String, SortedMap<Short, StopLocation>> innerMapping = orderedStations.get(route);

			
			for (String directionHash : innerMapping.keySet())
			{
				SortedMap<Short, StopLocation> stations = innerMapping.get(directionHash);

				ArrayList<Float> floats = new ArrayList<Float>();
				for (Short platformOrder : stations.keySet())
				{
					StopLocation station = stations.get(platformOrder);

					floats.add((float)station.getLatitudeAsDegrees());
					floats.add((float)station.getLongitudeAsDegrees());
				}
				
				Path path = new Path(floats);
				
				map.get(route).addPaths(path);
			}
			
			//match other branches to main branch if possible
			HashSet<String> alreadyHandledDirections = new HashSet<String>();
			
			final String trunkBranch = "Trunk";
			for (String directionHash : innerMapping.keySet())
			{
				String[] array = directionHash.split("\\|");
				String direction = array[0];
				String branch = array[1];
				
				if (alreadyHandledDirections.contains(direction))
				{
					continue;
				}
				
				if (trunkBranch.equals(branch))
				{
					continue;
				}
				
				SortedMap<Short, StopLocation> branchInnerMapping = innerMapping.get(directionHash);
				String trunkDirectionHash = createDirectionHash(direction, trunkBranch);
				SortedMap<Short, StopLocation> trunkInnerMapping = innerMapping.get(trunkDirectionHash);
				
				int minBranchOrder = -1;
				for (Short order : branchInnerMapping.keySet())
				{
					if (minBranchOrder == -1)
					{
						minBranchOrder = order;
					}
					else
					{
						minBranchOrder = Math.min(order, minBranchOrder);
					}
				}
				
				int maxTrunkOrder = 0;
				for (Short order : trunkInnerMapping.keySet())
				{
					if (order < minBranchOrder)
					{
						maxTrunkOrder = Math.max(order, maxTrunkOrder);
					}
				}
				
				ArrayList<Float> points = new ArrayList<Float>();
				
				StopLocation branchStop = branchInnerMapping.get((short)minBranchOrder);
				StopLocation trunkStop = trunkInnerMapping.get((short)maxTrunkOrder);
				
				if (trunkStop != null && branchStop != null)
				{
					points.add(trunkStop.getLatitudeAsDegrees());
					points.add(trunkStop.getLongitudeAsDegrees());
					points.add(branchStop.getLatitudeAsDegrees());
					points.add(branchStop.getLongitudeAsDegrees());

					Path path = new Path(points);
					map.get(route).addPaths(path);
				}
			}
		}

	}
	
	private static String createDirectionHash(String direction, String branch)
	{
		return direction + "|" + branch;
	}
	
	public void runParse(Reader stream) throws IOException
	{
		RouteTitles routeKeysToTitles = source.getRouteKeysToTitles();
		for (String route : routeKeysToTitles.routeTags())
		{
			String routeTitle = routeKeysToTitles.getTitle(route);
			map.put(route, new RouteConfig.Builder(route, routeTitle, 0, 0, source));
		}

		populateStops(stream);
		
	}
	
}
