package com.schneeloch.bostonbusmap_library.test;

import com.google.common.collect.ImmutableMap;
import com.schneeloch.bostonbusmap_library.data.ITransitDrawables;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.database.Schema;

/**
 * Created by schneg on 1/7/18.
 */

public class TestingTransitDrawables implements ITransitDrawables {
    @Override
    public int getBitmapDescriptor(Location location, boolean isSelected, ImmutableMap<String, Schema.Routes.SourceId> sourceIdMap) {
        return -1;
    }
}
