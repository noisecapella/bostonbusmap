package boston.Bus.Map.data;

import java.io.IOException;
import java.util.HashMap;

import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

public class Prediction implements Comparable<Prediction>
{
	private static final String hourMinuteFormatString = "%l:%M%P";
	private final int minutes;
	private final long epochTime;
	private final int vehicleId;
	private final String direction;
	private final String routeName;
	private final Time arrivalTime = new Time();
	
	public Prediction(int minutes, long epochTime, int vehicleId,
			String direction, String routeName) {
		this.minutes = minutes;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.direction = direction;
		this.routeName = routeName;
		arrivalTime.set(System.currentTimeMillis() + ((minutes >= 0 ? minutes : 0) * 60 * 1000));
	}

	public String makeSnippet(HashMap<String, String> routeKeysToTitles) {
		if (minutes < 0)
		{
			return "";
		}
		else
		{
			String ret = "" + routeKeysToTitles.get(routeName);
			
			if (direction != null)
			{
				ret += "\n" + direction;
			}
			
			if (minutes == 0)
			{
				return ret + "\nArriving now!";
			}
			else
			{
				return ret + "\nArriving in " + minutes + " min at " + arrivalTime.format(hourMinuteFormatString).trim();
			}
		}
	}

	@Override
	public int compareTo(Prediction another) {
		return new Integer(minutes).compareTo(another.minutes);
		
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return (int) (minutes ^ epochTime ^ vehicleId);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Prediction)
		{
			Prediction other = (Prediction)o;
			if (other.minutes == minutes && other.epochTime == epochTime && other.vehicleId == vehicleId)
			{
				//most likely the same
				return true;
			}
		}
		return false;
	}
	
	public String getRouteName() {
		return routeName;
	}
}
