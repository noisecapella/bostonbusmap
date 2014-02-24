package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Date;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;

import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.transit.TransitSystem;
import boston.Bus.Map.util.StringUtil;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

/**
 * A container of a prediction. Immutable
 * 
 * @author schneg
 *
 */
public class TimePrediction implements IPrediction
{
	public static final int NULL_LATENESS = -1;
	/**
	 * This may be null
	 */
	protected final String vehicleId;
	protected final String direction;
	protected final String routeName;
	protected final String routeTitle;
	protected final long arrivalTimeMillis;
	protected final boolean affectedByLayover;
	protected final boolean isDelayed;
	/**
	 * In seconds
	 */
	protected final int lateness;
	protected final String block;
	protected final String stopId;
	
	public TimePrediction(long arrivalTimeMillis, String vehicleId,
                          String direction, String routeName, String routeTitle,
                          boolean affectedByLayover, boolean isDelayed, int lateness,
						  String block, String stopId)
	{
		this.vehicleId = vehicleId;
		this.direction = direction;
		this.routeName = routeName;
		this.routeTitle = routeTitle;

		this.arrivalTimeMillis = arrivalTimeMillis;

		this.affectedByLayover = affectedByLayover;
		this.isDelayed = isDelayed;
		this.lateness = lateness;
		this.block = block;
		this.stopId = stopId;
	}

	public void makeSnippet(Context context, StringBuilder builder, boolean showRunNumber) {
		int minutes = getMinutes();
		if (minutes < 0)
		{
			return;
		}

		builder.append("Route <b>").append(routeTitle).append("</b>");
		if (vehicleId != null)
		{
			builder.append(", Vehicle <b>").append(vehicleId).append("</b>");
		}

		if (!StringUtil.isEmpty(block) && showRunNumber) {
			builder.append("<br />Run number <b>").append(block).append("</b>");
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

			Date date = new Date(arrivalTimeMillis);
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
	}

	@Override
	public int compareTo(IPrediction anotherObj)
	{
        if (anotherObj instanceof TimePrediction) {
            TimePrediction another = (TimePrediction)anotherObj;
		    return ComparisonChain.start().compare(arrivalTimeMillis, another.arrivalTimeMillis)
				.compare(vehicleId, another.vehicleId)
				.compare(direction, another.direction)
				.compare(routeName, another.routeName)
				.compareFalseFirst(affectedByLayover, another.affectedByLayover)
				.compareFalseFirst(isDelayed, another.isDelayed)
				.compare(lateness, another.lateness)
				.compare(block, another.block)
				.compare(stopId, another.stopId)
				.result();
        }
        else
        {
            throw new RuntimeException("Cannot compare distance prediction with time prediction");
        }
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(vehicleId, direction, routeName, arrivalTimeMillis,
				affectedByLayover, isDelayed, lateness, block, stopId);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TimePrediction) {
			TimePrediction another = (TimePrediction)o;
			return Objects.equal(arrivalTimeMillis, another.arrivalTimeMillis) &&
					Objects.equal(vehicleId, another.vehicleId) &&
					Objects.equal(direction, another.direction) &&
					Objects.equal(routeName, another.routeName) &&
					Objects.equal(affectedByLayover, another.affectedByLayover) &&
					Objects.equal(isDelayed, another.isDelayed) &&
					Objects.equal(lateness, another.lateness) &&
					Objects.equal(block, another.block) &&
					Objects.equal(stopId, another.stopId);
		}
		else
		{
			return false;
		}
	}
	
	public String getRouteName() {
		return routeName;
	}
	
	public String getRouteTitle() {
		return routeTitle;
	}

	public static int calcMinutes(long arrivalTimeMillis)
	{
		return (int)(arrivalTimeMillis - System.currentTimeMillis()) / 1000 / 60;
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
		dest.writeString(routeTitle);
		writeBoolean(dest, affectedByLayover);
		writeBoolean(dest, isDelayed);
		dest.writeInt(lateness);
		dest.writeString(block);
		dest.writeString(stopId);
	}
	
	public static final Parcelable.Creator<TimePrediction> CREATOR = new Creator<TimePrediction>() {
		
		@Override
		public TimePrediction[] newArray(int size) {
			return new TimePrediction[size];
		}
		
		@Override
		public TimePrediction createFromParcel(Parcel source) {
			//NOTE: if this changes you must also change CommuterRailPrediction.CREATOR.createFromParcel
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

			TimePrediction prediction = new TimePrediction(arrivalTimeMillis, vehicleId, direction, routeName, routeTitle, affectedByLayover, isDelayed, lateness, block, stopId);
			return prediction;
		}
	};

	/**
	 * Create 
	 * @param routeKeysToTitles
	 * @return
	 */
	public ImmutableMap<String, Spanned> makeSnippetMap(Context context) {
		StringBuilder ret = new StringBuilder();
		makeSnippet(context, ret, TransitSystem.showRunNumber());
		
		ImmutableMap<String, Spanned> map = ImmutableMap.of(MoreInfo.textKey, Html.fromHtml(ret.toString()));
		
		return map;
	}

	protected static boolean readBoolean(Parcel source) {
		return source.readInt() == 1;
	}
	
	protected static void writeBoolean(Parcel dest, boolean data)
	{
		dest.writeInt(data ? 1 : 0);
	}

    @Override
    public boolean isInvalid() {
        return getMinutes() < 0;
    }
}
