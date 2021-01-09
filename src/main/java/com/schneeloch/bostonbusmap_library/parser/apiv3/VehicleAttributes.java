package com.schneeloch.bostonbusmap_library.parser.apiv3;

import com.schneeloch.bostonbusmap_library.parser.apiv3.Timestamp;

/**
 * Created by schneg on 12/16/17.
 */

public class VehicleAttributes {
    public float longitude;
    public float latitude;
    public Timestamp last_updated;
    public String label;
    public float bearing;
    public String headsign;
    public int direction_id;
    public String current_status;
}
