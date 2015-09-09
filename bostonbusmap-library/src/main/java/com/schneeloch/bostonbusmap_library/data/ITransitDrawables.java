package com.schneeloch.bostonbusmap_library.data;

import com.google.android.gms.maps.model.BitmapDescriptor;

/**
 * Created by schneg on 1/3/15.
 */
public interface ITransitDrawables {
    int getBitmapDescriptor(Location location, boolean isSelected);
}
