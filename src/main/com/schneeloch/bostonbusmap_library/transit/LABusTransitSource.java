package com.schneeloch.bostonbusmap_library.transit;

import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;

/**
 * Created by georgeandroid on 2/27/15.
 */
public class LABusTransitSource extends NextBusTransitSource
{
    public LABusTransitSource(TransitSystem system, ITransitDrawables drawables,
                              TransitSourceTitles transitSourceTitles, RouteTitles allRouteTitles)
    {
        super(system, drawables, "lametro", transitSourceTitles, allRouteTitles);
    }
}
