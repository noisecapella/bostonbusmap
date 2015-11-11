package com.schneeloch.bostonbusmap_library.data;

import java.text.DateFormat;
import java.util.Date;


import android.os.Parcel;
import android.os.Parcelable;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;

public class CommuterRailPrediction extends TimePrediction implements Parcelable
{
	public static enum Flag {
		/**
		 * Record has scheduled time but not the vehicle lateness. May have vehicle ID and position.
		 */
		Sch("sch"),
		/**
		 *  Record has scheduled time and the vehicle’s current lateness and position
		 */
		Pre("pre"),
		/**
		 * Approaching the station now. Record has vehicle location but not lateness.
		 */
		App("app"),
		/**
		 *  Arriving at the station now. Record has vehicle location but not lateness
		 */
		Arr("arr"),
		/**
		 * Departing the station now or has departed the station. Record has vehicle location but not
		 lateness. Used at the trip’s origin.
		 */
		Dep("dep"),
		/**
		 *  “Delayed.” Vehicle is late and is not moving. The lateness may or may not be included
		 */
		Del("del");

		public final String name;

		Flag(String name) {
			this.name = name;
		}

		public static Flag toFlagEnum(String informationType)
		{
			if (informationType == null)
			{
				return Sch;
			}
			else
			{
				informationType = informationType.toLowerCase();
				for (Flag flag : Flag.values()) {
					if (flag.name.equals(informationType)) {
						return flag;
					}
				}
				return null;
			}
		}

	}

	
	private final Flag flag;
	
	public CommuterRailPrediction(long arrivalTimeMillis, String vehicleId, String direction,
			String routeName, String routeTitle, boolean affectedByLayover, boolean isDelayed,
			int lateness, String block, String stopId, Flag flag)
	{
		super(arrivalTimeMillis, vehicleId, direction, routeName, routeTitle, affectedByLayover, isDelayed,
				lateness, stopId, block);
		this.flag = flag;
	}
	
	@Override
	public void makeSnippet(StringBuilder builder, boolean showRunNumber) {
		int minutes = getMinutes();
		if (minutes < 0)
		{
			return;
		}
		else
		{
            builder.append("Route <b>").append(routeTitle).append("</b>");
            if (vehicleId != null)
            {
                builder.append("<br /><b>").append(vehicleId).append("</b>");
            }

			if (direction != null)
			{
				builder.append("<br />").append(direction);
			}

			DateFormat dateFormat = TransitSystem.getDefaultTimeFormat();

			Date scheduledArrival = new Date(arrivalTimeMillis);

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
		dest.writeString(flag.name());
	}
	
	public static final Creator<CommuterRailPrediction> CREATOR = new Creator<CommuterRailPrediction>() {
		
		@Override
		public CommuterRailPrediction[] newArray(int size) {
			return new CommuterRailPrediction[size];
		}
		
		@Override
		public CommuterRailPrediction createFromParcel(Parcel source)
		{
			//NOTE: if this changes you must also change TimePrediction.CREATOR.createFromParcel
			long arrivalTimeMillis = source.readLong();
			String vehicleId = source.readString();
			if (vehicleId.length() == 0)
			{
				vehicleId = null;
			}
			String direction = source.readString();
			String routeName = source.readString();
			String routeTitle = source.readString();
			boolean affectedByLayover = readBoolean(source);
			boolean isDelayed = readBoolean(source);
			int lateness = source.readInt();
			String block = source.readString();
			String stopId = source.readString();
			
			Flag flag = Flag.toFlagEnum(source.readString());
			
			CommuterRailPrediction prediction = new CommuterRailPrediction(arrivalTimeMillis, vehicleId, direction, routeName, routeTitle, affectedByLayover, isDelayed, lateness, block, stopId, flag);
			return prediction;
		}
	};
}
