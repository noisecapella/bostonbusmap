package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import boston.Bus.Map.transit.TransitSystem;

public class CommuterRailPrediction extends Prediction {

	public CommuterRailPrediction(int minutes, int vehicleId, String direction,
			String routeName, boolean affectedByLayover, boolean isDelayed,
			int lateness) {
		super(minutes, vehicleId, direction, routeName, affectedByLayover, isDelayed,
				lateness);
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

			if (isDelayed)
			{
				builder.append("<br /><b>Delayed</b>");
			}
			
			if (affectedByLayover)
			{
				//hmm...
			}
			
			if (lateness != NULL_LATENESS)
			{
				builder.append("<br />Seconds late: ").append(lateness);
			}
			
			if (minutes == 0)
			{
				builder.append("<br />Arriving <b>now</b>!");
			}
			else
			{
				DateFormat dateFormat = TransitSystem.getDefaultTimeFormat();

				Date date = new Date(arrivalTimeMillis - TransitSystem.getTimeZone().getOffset(arrivalTimeMillis));
				if (dateFormat != null)
				{
					//the vast majority of the time this should be true but someone reported an exception where it's not
					String formatted = dateFormat.format(date);
					builder.append("<br />Arriving in <b>").append(minutes);
					builder.append(" min</b> at ").append(formatted.trim());
				}
				else
				{
					builder.append("<br />Arriving in <b>").append(minutes).append(" min</b>");
				}
			}
			
			ret = builder.toString();
		}
		return ret;
	}
	
}
