package com.schneeloch.bostonbusmap_library.data;

import com.google.common.base.Objects;
import com.schneeloch.bostonbusmap_library.util.Constants;

/**
 * Key for a group of Locations
 */
public class GroupKey {
    private final String parent;
    private final long hash;

    public GroupKey(Location location) {
        if (location.getParent().isPresent()) {
            this.parent = location.getParent().get();
            hash = 0;
        }
        else {
            parent = null;
            hash = calculateHash(location);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parent, hash);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GroupKey) {
            GroupKey other = (GroupKey)o;
            if (hash != other.hash) {
                return false;
            }
            if (parent == null) {
                return other.parent == null;
            } else {
                return parent.equals(other.parent);
            }
        }
        else {
            return false;
        }
    }

    /**
     * Create 64 bit hash of latitude and longitude
     * @param location
     * @return
     */
    public static long calculateHash(Location location) {
        final int latInt = (int)(location.getLatitudeAsDegrees() * Constants.E6);
        final int lonInt = (int)(location.getLongitudeAsDegrees() * Constants.E6);

        //make a hash to easily compare this location's position against others
        //get around sign extension issues by making them all positive numbers
        final int latIntHash = (latInt < 0 ? -latInt : latInt);
        final int lonIntHash = (lonInt < 0 ? -lonInt : lonInt);
        return ((long)latIntHash << 32) | (long)lonIntHash;
    }
}
