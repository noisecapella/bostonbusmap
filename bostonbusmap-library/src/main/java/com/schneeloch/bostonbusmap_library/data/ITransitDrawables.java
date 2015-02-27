package com.schneeloch.bostonbusmap_library.data;

import android.graphics.drawable.Drawable;

/**
 * Created by schneg on 1/3/15.
 */
public interface ITransitDrawables {
    Drawable getVehicle(int heading);

    Drawable getDrawable(Location location);
}
