package boston.Bus.Map.data;

import java.io.IOException;

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
	
	public Prediction(int minutes, long epochTime, int vehicleId,
			String direction, String routeName) {
		this.minutes = minutes;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.direction = direction;
		this.routeName = routeName;
	}

	public String makeSnippet() {
		if (minutes < 0)
		{
			return "";
		}
		else
		{
			String ret = "Route " + routeName;
			ret += ", Bus " + vehicleId;
			
			if (direction != null)
			{
				ret += "\n" + direction;
			}
			
			if (minutes == 0)
			{
				return ret + "\narriving now!";
			}
			else
			{
				Time time = new Time();
				time.setToNow();
				
				return ret + "\narriving in " + minutes + " min at " + time.format(hourMinuteFormatString);
			}
		}			
	}

	@Override
	public int compareTo(Prediction another) {
		return new Integer(minutes).compareTo(another.minutes);
		
	}

	public String getRouteName() {
		return routeName;
	}
}
