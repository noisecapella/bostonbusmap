package boston.Bus.Map.data;

import java.io.IOException;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.transit.TransitSystem;
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
	public static final int NULL_LATENESS = -1;
	private final int vehicleId;
	private final String direction;
	private final String routeName;
	private final long arrivalTimeMillis;
	private final boolean affectedByLayover;
	private final boolean isDelayed;
	private final int lateness;
	
	public Prediction(int minutes, int vehicleId,
			String direction, String routeName, boolean affectedByLayover, boolean isDelayed, int lateness)
	{
		this.vehicleId = vehicleId;
		this.direction = direction;
		this.routeName = routeName;

		arrivalTimeMillis = TransitSystem.currentTimeMillis() + minutes * 60 * 1000;
		
		
		this.affectedByLayover = affectedByLayover;
		this.isDelayed = isDelayed;
		this.lateness = lateness;
	}

	public String makeSnippet(HashMap<String, String> routeKeysToTitles, Context context) {
		String ret;
		
		int minutes = getMinutes();
		if (false && minutes < 0)
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
			
			if (lateness != NULL_LATENESS)
			{
				ret += "<br />Seconds late: " + lateness;
			}
			
			if (minutes == 0)
			{
				ret += "<br />Arriving <b>now</b>!";
			}
			else
			{
				DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);

				Date date = new Date(arrivalTimeMillis - TransitSystem.getTimeZone().getOffset(arrivalTimeMillis));
				String formatted = dateFormat.format(date);
				ret += "<br />Arriving in <b>" + minutes + " min</b> at " + formatted.trim();
			}
		}
		return ret;
	}

	@Override
	public int compareTo(Prediction another)
	{
		return new Long(arrivalTimeMillis).compareTo(another.arrivalTimeMillis);
		
	}

	@Override
	public int hashCode() {
		return (int) (arrivalTimeMillis ^ vehicleId);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Prediction)
		{
			Prediction other = (Prediction)o;
			if (other.arrivalTimeMillis == arrivalTimeMillis && other.vehicleId == vehicleId)
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

	public static int calcMinutes(long arrivalTimeMillis)
	{
		return (int)(arrivalTimeMillis - TransitSystem.currentTimeMillis()) / 1000 / 60;
	}
	
	public int getMinutes()
	{
		return calcMinutes(arrivalTimeMillis);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(arrivalTimeMillis);
		dest.writeInt(vehicleId);
		dest.writeString(direction);
		dest.writeString(routeName);
		writeBoolean(dest, affectedByLayover);
		writeBoolean(dest, isDelayed);
		dest.writeInt(lateness);
	}
	
	public static final Parcelable.Creator<Prediction> CREATOR = new Creator<Prediction>() {
		
		@Override
		public Prediction[] newArray(int size) {
			return new Prediction[size];
		}
		
		@Override
		public Prediction createFromParcel(Parcel source) {
			long arrivalTimeMillis = source.readLong();
			int vehicleId = source.readInt();
			String direction = source.readString();
			String routeName = source.readString();
			boolean affectedByLayover = readBoolean(source);
			boolean isDelayed = readBoolean(source);
			int lateness = source.readInt();
			
			int minutes = calcMinutes(arrivalTimeMillis);
			Prediction prediction = new Prediction(minutes, vehicleId, direction, routeName, affectedByLayover, isDelayed, lateness);
			return prediction;
		}
	};
	public static final int NULL_LATENESS = -1;

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
