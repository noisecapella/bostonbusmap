package boston.Bus.Map.data;

import boston.Bus.Map.main.MoreInfo;

import com.google.common.collect.ImmutableMap;

import android.content.Context;
import android.os.Parcelable;
import android.text.Spanned;

public interface IPrediction extends Comparable<IPrediction>, Parcelable {

    String getRouteName();

    boolean isInvalid();

    void makeSnippet(Context context, StringBuilder ret, boolean showRunNumber);

    String getRouteTitle();

    ImmutableMap<String, Spanned> makeSnippetMap(Context context);
}