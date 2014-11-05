package boston.Bus.Map.data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import boston.Bus.Map.data.VehicleLocations.Key;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class VehicleLocations {
	public static class Key {
		private final int transitSourceId;
		private final String id;
		
		public Key(int transitSourceId, String id) {
			this.transitSourceId = transitSourceId;
			this.id = id;
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(transitSourceId, id);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key other = (Key)obj;
				return Objects.equal(this.transitSourceId, other.transitSourceId) &&
						Objects.equal(this.id, other.id);
			}
			return false;
		}
	}
	
	private final ConcurrentHashMap<Key, BusLocation> locations = new ConcurrentHashMap<Key, BusLocation>();

    /**
     * Remove all vehicles with the given transitSourceId and replace with what's in newItems
     * @param transitSourceId
     * @param newItems
     */
    public void update(int transitSourceId, Map<Key, BusLocation> newItems) {
        Set<VehicleLocations.Key> toRemove = Sets.newHashSet();

        for (VehicleLocations.Key key : locations.keySet()) {
            if (key.transitSourceId == transitSourceId && locations.containsKey(key)) {
                toRemove.add(key);
            }
        }

        for (VehicleLocations.Key key : toRemove) {
            locations.remove(key);
        }

        locations.putAll(newItems);
    }

	public BusLocation get(VehicleLocations.Key id) {
		return locations.get(id);
	}

	public Collection<BusLocation> values() {
		return locations.values();
	}

	public boolean containsKey(Key key) {
		return locations.containsKey(key);
	}

	public Set<VehicleLocations.Key> keySet() {
		return locations.keySet();
	}
}
