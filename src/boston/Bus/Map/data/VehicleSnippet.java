package boston.Bus.Map.data;

import android.content.Context;
import android.os.Parcel;
import android.text.Spanned;

import com.google.common.collect.ImmutableMap;

/**
 * Created by schneg on 11/10/14.
 */
public class VehicleSnippet implements IPrediction {
    @Override
    public String getRouteName() {
        return null;
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public void makeSnippet(Context context, StringBuilder ret, boolean showRunNumber) {

    }

    @Override
    public String getRouteTitle() {
        return null;
    }

    @Override
    public ImmutableMap<String, Spanned> makeSnippetMap(Context context) {
        return null;
    }

    @Override
    public int compareTo(IPrediction another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
