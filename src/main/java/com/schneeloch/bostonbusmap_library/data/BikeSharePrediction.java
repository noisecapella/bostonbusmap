package com.schneeloch.bostonbusmap_library.data;

import android.os.Parcel;
import android.text.Html;
import android.text.Spanned;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.util.MoreInfoConstants;

public class BikeSharePrediction implements IPrediction {
	public final int numBikes;
	public final int numEmptyDocks;
	public final boolean locked;
	public final boolean installed;
	private final String routeName;
	private final String routeTitle;

	public BikeSharePrediction(String routeName, String routeTitle, int numBikes, int numEmptyDocks, boolean locked, boolean installed) {
		this.routeName = routeName;
		this.routeTitle = routeTitle;
		this.numBikes = numBikes;
		this.numEmptyDocks = numEmptyDocks;
		this.locked = locked;
		this.installed = installed;
	}

	@Override
	public int compareTo(IPrediction another) {
		if (another instanceof BikeSharePrediction) {
			BikeSharePrediction bikeSharePrediction = (BikeSharePrediction)another;
			return ComparisonChain.start()
					.compare(numBikes, bikeSharePrediction.numBikes)
					.compare(numEmptyDocks, bikeSharePrediction.numEmptyDocks)
					.compareTrueFirst(locked, bikeSharePrediction.locked)
					.compareTrueFirst(installed, bikeSharePrediction.installed)
					.result();
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
		dest.writeInt(numBikes);
		dest.writeInt(numEmptyDocks);
		dest.writeInt(locked ? 1 : 0);
		dest.writeInt(installed ? 1 : 0);
	}

	public static final Creator<BikeSharePrediction> CREATOR = new Creator<BikeSharePrediction>() {
		
		@Override
		public BikeSharePrediction[] newArray(int size) {
			return new BikeSharePrediction[size];
		}
		
		@Override
		public BikeSharePrediction createFromParcel(Parcel source) {
			String routeName = source.readString();
			String routeTitle = source.readString();
			int numBikes = source.readInt();
			int numEmptyDocks = source.readInt();
			boolean locked = source.readInt() != 0;
			boolean installed = source.readInt() != 0;
			return new BikeSharePrediction(routeName, routeTitle, numBikes, numEmptyDocks, locked, installed);
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
		ret.append("Bikes: ").append(numBikes).append("<br />");
		ret.append("Empty Docks: ").append(numEmptyDocks).append("<br />");
		if (locked) {
			ret.append("<b>Locked</b><br />");
		}
		if (!installed) {
			ret.append("<b>Not installed</b><br />");
		}
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
		return Objects.hashCode(numBikes, numEmptyDocks, locked, installed, routeName);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BikeSharePrediction) {
			BikeSharePrediction another = (BikeSharePrediction)o;
			return Objects.equal(numBikes, another.numBikes) &&
					Objects.equal(numEmptyDocks, another.numEmptyDocks) &&
					Objects.equal(locked, another.locked) &&
					Objects.equal(installed, another.installed) &&
					Objects.equal(routeName, another.routeName);
		}
		else
		{
			return false;
		}
	}
}
