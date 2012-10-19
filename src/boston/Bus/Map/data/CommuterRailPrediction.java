package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Date;

import com.google.common.collect.ImmutableMap;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import boston.Bus.Map.transit.TransitSystem;

public class CommuterRailPrediction extends Prediction implements Parcelable
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
	
	public CommuterRailPrediction(int minutes, String vehicleId, String direction,
			String routeName, boolean affectedByLayover, boolean isDelayed,
			int lateness, int flag)
	{
		super(minutes, vehicleId, direction, routeName, affectedByLayover, isDelayed,
				lateness);
		this.flag = flag;
	}
	
	@Override
	public void makeSnippet(TransitSourceTitles routeKeysToTitles, Context context, StringBuilder builder) {
		int minutes = getMinutes();
		if (minutes < 0)
		{
			return;
		}
		else
		{
			builder.append("Line <b>").append(routeKeysToTitles.getTitle(routeName)).append("</b>");
			if (vehicleId != null)
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
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(flag);
	}
	
	public static final Creator<CommuterRailPrediction> CREATOR = new Creator<CommuterRailPrediction>() {
		
		@Override
		public CommuterRailPrediction[] newArray(int size) {
			return new CommuterRailPrediction[size];
		}
		
		@Override
		public CommuterRailPrediction createFromParcel(Parcel source)
		{
			//NOTE: if this changes you must also change Prediction.CREATOR.createFromParcel
			long arrivalTimeMillis = source.readLong();
			String vehicleId = source.readString();
			if (vehicleId.length() == 0)
			{
				vehicleId = null;
			}
			String direction = source.readString();
			String routeName = source.readString();
			boolean affectedByLayover = readBoolean(source);
			boolean isDelayed = readBoolean(source);
			int lateness = source.readInt();
			
			int minutes = calcMinutes(arrivalTimeMillis);
			
			int flag = source.readInt();
			
			CommuterRailPrediction prediction = new CommuterRailPrediction(minutes, vehicleId, direction, routeName, affectedByLayover, isDelayed, lateness, flag);
			return prediction;
		}
	};
}
