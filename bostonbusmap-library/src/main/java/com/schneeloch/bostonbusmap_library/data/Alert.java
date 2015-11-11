package com.schneeloch.bostonbusmap_library.data;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schneeloch.bostonbusmap_library.util.AlertInfoConstants;

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

	public Alert(Date date, CharSequence title, CharSequence description)
	{
		this.date = date;
		this.title = title.toString();
		this.description = description.toString();
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

	public static Map<String, Spanned> makeSnippetMap(List<Alert> alerts)
	{
		String ret = Alert.makeSnippet(alerts);
		
		return ImmutableMap.of(AlertInfoConstants.textKey, Html.fromHtml(ret));
	}

	/**
	 *
	 * @param alerts Some collection of alerts with the same description
	 * @return Pseudo HTML which is shown in the AlertInfo screen
	 */
	private static String makeSnippet(List<Alert> alerts)
	{
		StringBuilder builder = new StringBuilder();
		if (alerts.size() == 0) {
			return "";
		}

		Set<String> titles = Sets.newTreeSet();
        for (Alert alert : alerts) {
			String title = alert.getTitle();
			if (title != null && title.length() != 0) {
                titles.add(title);
			}
		}

		builder.append("<b>").append(Joiner.on("<br />").join(titles)).append("</b><br />");

		Alert firstAlert = alerts.get(0);
		String description = firstAlert.getDescription();
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

			Alert alert = new Alert(date, title, description);
			return alert;
		}
	};

	@Override
	public int compareTo(Alert another) {
		return ComparisonChain.start().compare(date, another.date)
				.compare(title, another.title)
				.compare(description, another.description).result();
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(date, title, description);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Alert) {
			Alert another = (Alert)o;
			return Objects.equal(date, another.date) &&
					Objects.equal(title, another.title) &&
					Objects.equal(description, another.description);
		}
		else
		{
			return false;
		}
	}
}
