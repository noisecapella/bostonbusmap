package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import boston.Bus.Map.main.AlertInfo;
import boston.Bus.Map.transit.TransitSystem;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

/**
 * Immutable alert data
 * 
 * @author schneg
 *
 */
public class Alert implements Parcelable, Comparable<Alert>
{
	private final Date date;
	private final String title;
	private final String description;
	private final String delay;
	
	public Alert(Date date, CharSequence title, CharSequence description, CharSequence delay)
	{
		this.date = date;
		this.title = title.toString();
		this.description = description.toString();
		this.delay = delay.toString();
	}

	public Date getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getDelay() {
		return delay;
	}

	public HashMap<String, Spanned> makeSnippetMap(Context context, Calendar yesterday, Calendar now)
	{
		HashMap<String, Spanned> map = new HashMap<String, Spanned>();
		
		String ret = makeSnippet(context, yesterday, now);
		
		map.put(AlertInfo.textKey, Html.fromHtml(ret));
		
		return map;
	}

	private String makeSnippet(Context context, Calendar yesterday, Calendar now)
	{
		StringBuilder builder = new StringBuilder();
		
		if (title != null && title.length() != 0)
		{
			builder.append("<b>").append(title).append("</b><br />");
		}
		if (delay != null && delay.length() != 0)
		{
			builder.append("<b>Delay: </b>").append(delay).append("<br />");
		}
		if (description != null && description.length() != 0)
		{
			String newDescription = description.replace("\n", "<br/>");
			builder.append(newDescription).append("<br />");
		}
		
		return builder.toString();
	}

	private boolean sameDay(Calendar date1, Calendar date2)
	{
		return date1.get(Calendar.DATE) == date2.get(Calendar.DATE) &&
				date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
				date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR); 
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		long time;
		if (date != null)
		{
			time = date.getTime();
		}
		else
		{
			time = 0;
		}

		dest.writeLong(time);
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(delay);
	}
	
	public static final Creator<Alert> CREATOR = new Creator<Alert>() {
		
		@Override
		public Alert[] newArray(int size) {
			return new Alert[size];
		}
		
		@Override
		public Alert createFromParcel(Parcel source) {
			long epoch = source.readLong();
			Date date = epoch == 0 ? null : new Date(epoch);
			String title = source.readString();
			String description = source.readString();
			String delay = source.readString();
			
			Alert alert = new Alert(date, title, description, delay);
			return alert;
		}
	};

	@Override
	public int compareTo(Alert another) {
		return ComparisonChain.start().compare(date, another.date)
				.compare(title, another.title)
				.compare(description, another.description)
				.compare(delay, another.delay).result();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(date, title, description, delay);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Alert) {
			Alert another = (Alert)o;
			return Objects.equal(date, another.date) &&
					Objects.equal(title, another.title) &&
					Objects.equal(description, another.description) &&
					Objects.equal(delay, another.delay);
		}
		else
		{
			return false;
		}
	}
}
