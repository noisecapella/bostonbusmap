package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.transit.TransitSystem;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

/**
 * A container of a prediction.
 * 
 * @author schneg
 *
 */
public class Prediction implements Comparable<Prediction>, Parcelable
{
	public static final int NULL_LATENESS = -1;
	/**
	 * This may be null
	 */
	protected final String vehicleId;
	protected final String direction;
	protected final String routeName;
	protected final long arrivalTimeMillis;
	protected final boolean affectedByLayover;
	protected final boolean isDelayed;
	protected final int lateness;
	
	public Prediction(int minutes, String vehicleId,
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

	public String makeSnippet(MyHashMap<String, String> routeKeysToTitles, Context context) {
		String ret;
		
		int minutes = getMinutes();
		if (minutes < 0)
		{
			ret = "";
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			
			builder.append("Route <b>").append(routeKeysToTitles.get(routeName)).append("</b>");
			if (vehicleId != null)
			{
				builder.append(", Bus <b>").append(vehicleId).append("</b>");
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

	@Override
	public int compareTo(Prediction another)
	{
		return new Long(arrivalTimeMillis).compareTo(another.arrivalTimeMillis);
	}

	@Override
	public int hashCode() {
		int vehicleIdHash = 0;
		if (vehicleId != null)
		{
			vehicleIdHash = vehicleId.hashCode();
		}
		return (int) (arrivalTimeMillis ^ vehicleIdHash);
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
		dest.writeString(vehicleId != null ? vehicleId : "");
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
			//NOTE: if this changes you must also change CommuterRailPrediction.CREATOR.createFromParcel
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
			Prediction prediction = new Prediction(minutes, vehicleId, direction, routeName, affectedByLayover, isDelayed, lateness);
			return prediction;
		}
	};

	/**
	 * Create 
	 * @param routeKeysToTitles
	 * @return
	 */
	public HashMap<String, Spanned> makeSnippetMap(MyHashMap<String, String> routeKeysToTitles, Context context) {
		HashMap<String, Spanned> map = new HashMap<String, Spanned>();
		
		String ret = makeSnippet(routeKeysToTitles, context);
		
		map.put(MoreInfo.textKey, Html.fromHtml(ret));
		
		return map;
	}

	protected static boolean readBoolean(Parcel source) {
		return source.readInt() == 1;
	}
	
	protected static void writeBoolean(Parcel dest, boolean data)
	{
		dest.writeInt(data ? 1 : 0);
	}
}
