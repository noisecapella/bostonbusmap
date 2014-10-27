package boston.Bus.Map.data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Sets;

public class VehicleLocations {
	private final ConcurrentHashMap<String, BusLocation> locations = new ConcurrentHashMap<String, BusLocation>();

	public void remove(String vehicleId) {
		locations.remove(vehicleId);
	}

	public Set<String> copyVehicleIds() {
		return Sets.newHashSet(locations.keySet());
	}

	public void put(String trip, BusLocation location) {
		locations.put(trip, location);
	}

	public void putAll(VehicleLocations busMapping) {
		locations.putAll(busMapping.locations);
	}

	public BusLocation get(String id) {
		return locations.get(id);
	}
}
