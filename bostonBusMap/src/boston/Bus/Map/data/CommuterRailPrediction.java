package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import boston.Bus.Map.transit.TransitSystem;

public class CommuterRailPrediction extends Prediction
{
	/**
	 * Record has scheduled time but not the vehicle lateness. May have vehicle ID and position.
	 */
	public static final int FLAG_Sch = 1;
	/**
	 *  Record has scheduled time and the vehicle’s current lateness and position
	 */
	public static final int FLAG_Pre = 2;
	/**
	 * Approaching the station now. Record has vehicle location but not lateness.
	 */
	public static final int FLAG_App = 3;
	/**
	 *  Arriving at the station now. Record has vehicle location but not lateness
	 */
	public static final int FLAG_Arr = 4;
	/**
	 * Departing the station now or has departed the station. Record has vehicle location but not 
lateness. Used at the trip’s origin.
	 */
	public static final int FLAG_Dep = 5;
	/**
	 *  “Delayed.” Vehicle is late and is not moving. The lateness may or may not be included
	 */
	public static final int FLAG_Del = 6;
	
	
	public static int toFlagEnum(String informationType)
	{
		if (informationType == null)
		{
			return FLAG_Sch;
		}
		else
		{
			informationType = informationType.toLowerCase();
			if (informationType.equals("del"))
			{
				return FLAG_Del;
			}
			else if (informationType.equals("pre"))
			{
				return FLAG_Pre;
			}
			else if (informationType.equals("app"))
			{
				return FLAG_App;
			}
			else if (informationType.equals("arr"))
			{
				return FLAG_Arr;
			}
			else if (informationType.equals("dep"))
			{
				return FLAG_Dep;
			}
			else
			{
				return FLAG_Sch;
			}
		}
	}

	private final int flag; 
	
	public CommuterRailPrediction(int minutes, int vehicleId, String direction,
			String routeName, boolean affectedByLayover, boolean isDelayed,
			int lateness, int flag)
	{
		super(minutes, vehicleId, direction, routeName, affectedByLayover, isDelayed,
				lateness);
		this.flag = flag;
	}

	@Override
	public String makeSnippet(HashMap<String, String> routeKeysToTitles, Context context) {
		String ret;
		
		int minutes = getMinutes();
		if (minutes < 0)
		{
			ret = "";
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			
			builder.append("Line <b>").append(routeKeysToTitles.get(routeName)).append("</b>");
			if (vehicleId != 0)
			{
				builder.append(", Train <b>").append(vehicleId).append("</b>");
			}

			if (direction != null)
			{
				builder.append("<br />").append(direction);
			}

			DateFormat dateFormat = TransitSystem.getDefaultTimeFormat();

			Date scheduledArrival = new Date(arrivalTimeMillis - TransitSystem.getTimeZone().getOffset(arrivalTimeMillis));

			if (dateFormat != null)
			{
				String formatted = dateFormat.format(scheduledArrival);

				builder.append("<br />Scheduled arrival at ").append(formatted.trim());
			}
			
			if (lateness > 5*60)
			{
				builder.append("<br /><b>Delayed ").append(lateness/60).append(" minutes</b>");
			}
			
			ret = builder.toString();
		}
		return ret;
	}

	
}
