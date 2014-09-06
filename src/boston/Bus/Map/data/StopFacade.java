package boston.Bus.Map.data;

import com.google.common.collect.ImmutableMap;

/**
 * Created by schneg on 7/21/14.
 */
public class StopFacade {
    private volatile ImmutableMap<String, StopLocation> stops;

    public StopFacade(ImmutableMap<String, StopLocation> stops) {
        if (stops == null) {
            throw new IllegalArgumentException("stops must be not null");
        }
        this.stops = stops;
    }

    public void replaceStops(ImmutableMap<String, StopLocation> stops) {
        this.stops = stops;
    }

    public ImmutableMap<String, StopLocation> getStops() {
        return stops;
    }
}
