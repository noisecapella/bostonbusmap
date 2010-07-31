package boston.Bus.Map.data;

import java.io.IOException;

import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;
import android.os.Parcel;
import android.os.Parcelable;

public class Prediction implements Comparable<Prediction>
{
	private final int minutes;
	private final long epochTime;
	private final int vehicleId;
	private final String directionToShow;
	private final RouteConfig routeConfig;
	
	public Prediction(int minutes, long epochTime, int vehicleId,
			String directionToShow, RouteConfig routeConfig) {
		this.minutes = minutes;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.directionToShow = directionToShow;
		this.routeConfig = routeConfig;
	}

	@Override
	public String toString() {
		if (minutes < 0)
		{
			return "";
		}
		else
		{
			String ret = "Bus " + vehicleId;
			
			ret += ", Route " + routeConfig.getRouteName();
			
			if (directionToShow != null && directionToShow.length() != 0)
			{
				ret += " " + directionToShow;
			}

			
			
			if (minutes == 0)
			{
				return ret + "\narriving now!";
			}
			else
			{
				return ret + "\narriving in " + minutes + " min";
			}
		}			
	}

	@Override
	public int compareTo(Prediction another) {
		return new Integer(minutes).compareTo(another.minutes);
		
	}

	public RouteConfig getRoute() {
		return routeConfig;
	}
}
