package com.schneeloch.bostonbusmap_library.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * Created by schneg on 11/10/14.
 */
public class VehicleSnippetView extends PredictionView {
    private final String snippet;
    private final String snippetTitle;
    private final ImmutableCollection<Alert> alerts;

    private static final SimplePredictionView EMPTY;

    public VehicleSnippetView(String snippet, String snippetTitle, ImmutableCollection<Alert> alerts) {
        this.snippet = snippet;
        this.snippetTitle = snippetTitle;
        this.alerts = alerts;
    }

    static {
        ImmutableCollection<Alert> nullAlerts = ImmutableList.of();
        EMPTY = new SimplePredictionView("", "", nullAlerts);
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Override
    public String getSnippetTitle() {
        return snippetTitle;
    }

    @Override
    public ImmutableCollection<Alert> getAlerts() {
        return alerts;
    }

    public static SimplePredictionView empty() {
        return EMPTY;
    }
}
