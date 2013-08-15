package boston.Bus.Map.database;

import java.util.List;

import com.google.common.collect.Lists;

import android.content.ContentResolver;
import android.database.Cursor;
import boston.Bus.Map.data.CommuterRailPrediction;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.Prediction;
import boston.Bus.Map.data.Predictions;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.provider.InMemoryContentProvider;
import boston.Bus.Map.transit.CommuterRailTransitSource;

public class InMemoryAgent {
	/**
	 * Get predictions snippet for a stop and optionally a route.
	 * @param resolver
	 * @param stopTag
	 * @param route if not null, filter results to only include this route
	 * @return html representing snippet
	 */
	public static String getPredictions(ContentResolver resolver, String selectionRouteTitle,
			String stopTag) {
		Cursor cursor = null;
		StringBuilder ret = new StringBuilder();
		try
		{
			String selection;
			String[] selectionArgs;
			if (selectionRouteTitle != null) {
				selection = Schema.Predictions.routeTitleColumn + " = ?";
				selectionArgs = new String[] {selectionRouteTitle};
			}
			else
			{
				selection = null;
				selectionArgs = null;
			}
			cursor = resolver.query(InMemoryContentProvider.PREDICTIONS_URI,
					new String[]{Schema.Predictions.arrivalTimeInMillisColumn,
					Schema.Predictions.vehicleidColumn,
					Schema.Predictions.isDelayedColumn,
					Schema.Predictions.stopidColumn,
					Schema.Predictions.latenessColumn,
					Schema.Predictions.directionColumn,
					Schema.Predictions.routeTitleColumn,
					Schema.Predictions.agencyColumn
					},
					selection, selectionArgs, Schema.Predictions.arrivalTimeInMillisColumn);

			cursor.moveToFirst();
			List<String> toWrite = Lists.newArrayList();
			while (cursor.isAfterLast() == false)
			{
				long arrivalTimeInMillis = cursor.getLong(0);
				String vehicleId = cursor.getString(1);
				boolean isDelayed = Schema.fromInteger(cursor.getInt(2));
				String stopId = cursor.getString(3);
				int lateness = cursor.getInt(4);
				String direction = cursor.getString(5);
				String routeTitle = cursor.getString(6);
				int agency = cursor.getInt(7);
				
				if (agency == Schema.Routes.enumagencyidCommuterRail) {
					CommuterRailPrediction.makeCommuterRailSnippet(arrivalTimeInMillis,
							vehicleId, direction, routeTitle, false, isDelayed, lateness, ret);
				}
				else
				{
					Prediction.makeSnippet(arrivalTimeInMillis, vehicleId,
							direction, routeTitle, false,
							isDelayed, ret);
					
				}
				
				cursor.moveToNext();
			}
		}
		finally
		{
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static void clearPredictions(String stopTag,
			String routeTag) {
		
	}

	public static void addPrediction(String currentStopTag, int minutes,
			long epochTime, String vehicleId, String dirTag,
			String currentRouteTag, Directions directions,
			boolean affectedByLayover, boolean isDelayed, int nullLateness) {
		
		
	}
	
	public static void addAlert(String stopTag, String routeTag, String description) {
		
	}
}
