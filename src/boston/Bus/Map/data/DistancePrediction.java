package boston.Bus.Map.data;

import java.text.DateFormat;
import java.util.Date;

import boston.Bus.Map.main.MoreInfo;
import boston.Bus.Map.transit.TransitSystem;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;

import android.content.Context;
import android.os.Parcel;
import android.text.Html;
import android.text.Spanned;

public class DistancePrediction implements IPrediction {
	private final String routeName;
	private final String routeTitle;
	private final String direction;
	private final String presentableDistance;
	private final float distanceInMeters;
	private final String vehicleName;
	
	public DistancePrediction(String presentableDistance, String vehicleName,
			String direction, String routeName, String routeTitle, float distanceInMeters) {
		this.routeName = routeName;
		this.routeTitle = routeTitle;
		this.direction = direction;
		this.presentableDistance = presentableDistance;
		this.vehicleName = vehicleName;
		this.distanceInMeters = distanceInMeters;
	}

	@Override
	public int compareTo(IPrediction anotherObj) {
		if (anotherObj instanceof DistancePrediction) {
			DistancePrediction another = (DistancePrediction)anotherObj;
			return ComparisonChain.start().compare(distanceInMeters, another.distanceInMeters)
					.result();
		}
		else
		{
			throw new RuntimeException("Can't compare time and distance predictions");
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String getRouteName() {
		return routeName;
	}

	@Override
	public String getRouteTitle() {
		return routeTitle;
	}

	@Override
	public boolean isInvalid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void makeSnippet(Context context, StringBuilder builder, boolean isMoreInfo) {
		if (isInvalid())
		{
			return;
		}

		builder.append("Route <b>").append(routeTitle).append("</b>");
		if (vehicleName != null)
		{
			builder.append(", Vehicle <b>").append(vehicleName).append("</b>");
		}

		if (direction != null)
		{
			builder.append("<br />").append(direction);
		}

		builder.append("<br />").append(presentableDistance);
	}

	@Override
	public ImmutableMap<String, Spanned> makeSnippetMap(Context context) {
		StringBuilder ret = new StringBuilder();
		makeSnippet(context, ret, true);
		
		ImmutableMap<String, Spanned> map = ImmutableMap.of(MoreInfo.textKey, Html.fromHtml(ret.toString()));
		
		return map;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(routeName, direction, presentableDistance, distanceInMeters, vehicleName);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DistancePrediction) {
			DistancePrediction another = (DistancePrediction)o;
			return Objects.equal(routeName, another.routeName) &&
					Objects.equal(direction, another.direction) &&
					Objects.equal(presentableDistance, another.presentableDistance) &&
					Objects.equal(distanceInMeters, another.distanceInMeters) &&
					Objects.equal(vehicleName, another.vehicleName);
		}
		else
		{
			return false;
		}
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(presentableDistance);
		dest.writeString(vehicleName);
		dest.writeString(direction);
		dest.writeString(routeName);
		dest.writeString(routeTitle);
		dest.writeFloat(distanceInMeters);
	}
	
	public static final Creator<DistancePrediction> CREATOR = new Creator<DistancePrediction>() {
		
		@Override
		public DistancePrediction[] newArray(int size) {
			return new DistancePrediction[size];
		}
		
		@Override
		public DistancePrediction createFromParcel(Parcel source) {
			String presentableDistance = source.readString();
			String vehicleName = source.readString();
			String direction = source.readString();
			String routeName = source.readString();
			String routeTitle = source.readString();
			float distanceInMeters = source.readFloat();
			return new DistancePrediction(presentableDistance, vehicleName, direction, routeName, routeTitle, distanceInMeters);
		}
	};
}
