package com.schneeloch.bostonbusmap_library.data;

import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.database.Schema;

/**
 * Created by schneg on 1/3/15.
 */
public interface ITransitDrawables {
    int getBitmapDescriptor(Location location, boolean isSelected, ImmutableMap<String, Schema.Routes.SourceId> sourceIdMap);
}
