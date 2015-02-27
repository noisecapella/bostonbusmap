package com.schneeloch.bostonbusmap_library.transit;


import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;

public class TorontoBusTransitSource extends NextBusTransitSource {

    public TorontoBusTransitSource(TransitSystem transitSystem,
                                   ITransitDrawables drawables, TransitSourceTitles transitSourceTitles,
                                   RouteTitles allRouteTitles) {
        super(transitSystem, drawables, "ttc", transitSourceTitles, allRouteTitles);
    }

}