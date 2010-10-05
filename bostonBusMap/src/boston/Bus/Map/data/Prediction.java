package boston.Bus.Map.data;

import java.io.IOException;
import java.util.HashMap;

import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.Time;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

public class Prediction implements Comparable<Prediction>, Parcelable
{
	private static final String hourMinuteFormatString = "%l:%M%P";
	private final int minutes;
	private final long epochTime;
	private final int vehicleId;
	private final String direction;
	private final String routeName;
	private final Time arrivalTime;
	
	public Prediction(int minutes, long epochTime, int vehicleId,
			String direction, String routeName) {
		this.minutes = minutes;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.direction = direction;
		this.routeName = routeName;
		arrivalTime = new Time();
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

	public int getMinutes() {
		return minutes;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(minutes);
		dest.writeLong(epochTime);
		dest.writeInt(vehicleId);
		dest.writeString(direction);
		dest.writeString(routeName);
	}
	
	public static final Parcelable.Creator<Prediction> CREATOR = new Creator<Prediction>() {
		
		@Override
		public Prediction[] newArray(int size) {
			return new Prediction[size];
		}
		
		@Override
		public Prediction createFromParcel(Parcel source) {
			int minutes = source.readInt();
			long epochTime = source.readLong();
			int vehicleId = source.readInt();
			String direction = source.readString();
			String routeName = source.readString();
			
			Prediction prediction = new Prediction(minutes, epochTime, vehicleId, direction, routeName);
			return prediction;
		}
	};

	public HashMap<String, Spanned> makeSnippetMap(HashMap<String, String> routeKeysToTitles) {
		HashMap<String, Spanned> map = new HashMap<String, Spanned>();
		
		String ret = "Route <b>" + routeKeysToTitles.get(routeName) + "</b>";
		if (vehicleId != 0)
		{
			ret += ", Bus <b>" + vehicleId + "</b>";
		}
		
		if (direction != null)
		{
			ret += "<br />" + direction.replace("\n", "<br />");
		}
		
		if (minutes == 0)
		{
			ret += "<br />Arriving now!";
		}
		else
		{
			ret += "<br />Arriving in " + minutes + " min at " + arrivalTime.format(hourMinuteFormatString).trim();
		}
		
		map.put(MoreInfo.textKey, Html.fromHtml(ret));
		
		return map;
	}
}
