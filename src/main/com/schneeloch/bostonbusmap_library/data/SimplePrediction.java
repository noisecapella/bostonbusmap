package com.schneeloch.bostonbusmap_library.data;

import android.os.Parcel;
import android.text.Html;
import android.text.Spanned;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.util.MoreInfoConstants;

public class SimplePrediction implements IPrediction {
	private final String text;
	private final String routeName;
	private final String routeTitle;

	public SimplePrediction(String routeName, String routeTitle, String text) {
		this.routeName = routeName;
		this.routeTitle = routeTitle;
		this.text = text;
	}

	@Override
	public int compareTo(IPrediction another) {
		if (another instanceof SimplePrediction) {
			SimplePrediction simplePrediction = (SimplePrediction)another;
			return ComparisonChain.start().compare(text, simplePrediction.text).result();
		}
		else
		{
			throw new RuntimeException("Can't compare text Predictions with other predictions");
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(routeName);
		dest.writeString(routeTitle);
		dest.writeString(text);
	}

	public static final Creator<SimplePrediction> CREATOR = new Creator<SimplePrediction>() {
		
		@Override
		public SimplePrediction[] newArray(int size) {
			return new SimplePrediction[size];
		}
		
		@Override
		public SimplePrediction createFromParcel(Parcel source) {
			String routeName = source.readString();
			String routeTitle = source.readString();
			String text = source.readString();
			return new SimplePrediction(routeName, routeTitle, text);
		}
	};
	
	@Override
	public String getRouteName() {
		return routeName;
	}

	@Override
	public boolean isInvalid() {
		return false;
	}

	@Override
	public void makeSnippet(StringBuilder ret, boolean showRunNumber) {
		ret.append(text);
	}

	@Override
	public String getRouteTitle() {
		return routeTitle;
	}

	@Override
	public ImmutableMap<String, Spanned> makeSnippetMap() {
		StringBuilder ret = new StringBuilder();
		makeSnippet(ret, TransitSystem.showRunNumber());
		
		ImmutableMap<String, Spanned> map = ImmutableMap.of(MoreInfoConstants.textKey, Html.fromHtml(ret.toString()));
		
		return map;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(text, routeName);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SimplePrediction) {
			SimplePrediction another = (SimplePrediction)o;
			return Objects.equal(text, another.text) && Objects.equal(routeName, another.routeName);
		}
		else
		{
			return false;
		}
	}
}
