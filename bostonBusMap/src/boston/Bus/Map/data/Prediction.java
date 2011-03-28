package boston.Bus.Map.data;

import java.io.IOException;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.util.Box;
import boston.Bus.Map.util.CanBeSerialized;
import android.content.Context;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

/**
 * A container of a prediction.
 * 
 * @author schneg
 *
 */
public class Prediction implements Comparable<Prediction>, Parcelable
{
	private static final String hourMinuteFormatString = "%l:%M%P";
	private final int minutes;
	private final long epochTime;
	private final int vehicleId;
	private final String direction;
	private final String routeName;
	private final Date arrivalTime;
	private final boolean affectedByLayover;
	private final boolean isDelayed;
	
	public Prediction(int minutes, long epochTime, int vehicleId,
			String direction, String routeName, boolean affectedByLayover, boolean isDelayed) {
		this.minutes = minutes;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.direction = direction;
		this.routeName = routeName;
		arrivalTime = new Date(epochTime);
		this.affectedByLayover = affectedByLayover;
		this.isDelayed = isDelayed;
	}

	public String makeSnippet(HashMap<String, String> routeKeysToTitles, Context context) {
		String ret;
		if (minutes < 0)
		{
			ret = "";
		}
		else
		{
			ret = "Route <b>" + routeKeysToTitles.get(routeName) + "</b>";
			if (vehicleId != 0)
			{
				ret += ", Bus <b>" + vehicleId + "</b>";
			}

			if (direction != null)
			{
				ret += "<br />" + direction;
			}

			if (isDelayed)
			{
				ret += "<br /><b>Delayed</b>";
			}
			
			if (affectedByLayover)
			{
				//hmm...
			}
			
			if (minutes == 0)
			{
				ret += "<br />Arriving <b>now</b>!";
			}
			else
			{
				DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
				String formatted = dateFormat.format(arrivalTime);
				ret += "<br />Arriving in <b>" + minutes + " min</b> at " + formatted.trim();
			}
		}
		return ret;
	}

	@Override
	public int compareTo(Prediction another) {
		return new Integer(minutes).compareTo(another.minutes);
		
	}

	@Override
	public int hashCode() {
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
		writeBoolean(dest, affectedByLayover);
		writeBoolean(dest, isDelayed);
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
			boolean affectedByLayover = readBoolean(source);
			boolean isDelayed = readBoolean(source);
			
			Prediction prediction = new Prediction(minutes, epochTime, vehicleId, direction, routeName, affectedByLayover, isDelayed);
			return prediction;
		}
	};

	/**
	 * Create 
	 * @param routeKeysToTitles
	 * @return
	 */
	public HashMap<String, Spanned> makeSnippetMap(HashMap<String, String> routeKeysToTitles, Context context) {
		HashMap<String, Spanned> map = new HashMap<String, Spanned>();
		
		String ret = makeSnippet(routeKeysToTitles, context);
		
		map.put(MoreInfo.textKey, Html.fromHtml(ret));
		
		return map;
	}

	private static boolean readBoolean(Parcel source) {
		return source.readInt() == 1;
	}
	
	private static void writeBoolean(Parcel dest, boolean data)
	{
		dest.writeInt(data ? 1 : 0);
	}
}
