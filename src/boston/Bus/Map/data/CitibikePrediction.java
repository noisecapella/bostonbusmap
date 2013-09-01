package boston.Bus.Map.data;

import android.content.Context;
import android.os.Parcel;
import android.text.Html;
import android.text.Spanned;
import boston.Bus.Map.main.MoreInfo;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;

public class CitibikePrediction implements IPrediction {
	public final String text;
	
	public CitibikePrediction(String text) {
		this.text = text;
	}

	@Override
	public int compareTo(IPrediction another) {
		if (another instanceof CitibikePrediction) {
			CitibikePrediction citibikePrediction = (CitibikePrediction)another;
			return ComparisonChain.start().compare(text, citibikePrediction.text).result();
		}
		else
		{
			throw new RuntimeException("Can't compare Citibike Predictions with other predictions");
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(text);
	}

	public static final Creator<CitibikePrediction> CREATOR = new Creator<CitibikePrediction>() {
		
		@Override
		public CitibikePrediction[] newArray(int size) {
			return new CitibikePrediction[size];
		}
		
		@Override
		public CitibikePrediction createFromParcel(Parcel source) {
			String text = source.readString();
			return new CitibikePrediction(text);
		}
	};
	
	@Override
	public String getRouteName() {
		return "Citibike";
	}

	@Override
	public boolean isInvalid() {
		return false;
	}

	@Override
	public void makeSnippet(Context context, StringBuilder ret) {
		ret.append(text);
	}

	@Override
	public String getRouteTitle() {
		return "Citibike";
	}

	@Override
	public ImmutableMap<String, Spanned> makeSnippetMap(Context context) {
		StringBuilder ret = new StringBuilder();
		makeSnippet(context, ret);
		
		ImmutableMap<String, Spanned> map = ImmutableMap.of(MoreInfo.textKey, Html.fromHtml(ret.toString()));
		
		return map;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(text);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CitibikePrediction) {
			CitibikePrediction another = (CitibikePrediction)o;
			return Objects.equal(text, another.text);
		}
		else
		{
			return false;
		}
	}
}
