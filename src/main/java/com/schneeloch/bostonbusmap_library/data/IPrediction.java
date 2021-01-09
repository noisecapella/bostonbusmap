package com.schneeloch.bostonbusmap_library.data;

import com.google.common.collect.ImmutableMap;

import android.os.Parcelable;
import android.text.Spanned;

public interface IPrediction extends Comparable<IPrediction>, Parcelable {

    String getRouteName();

    boolean isInvalid();

    void makeSnippet(StringBuilder ret, boolean showRunNumber);

    String getRouteTitle();

    ImmutableMap<String, Spanned> makeSnippetMap();

}