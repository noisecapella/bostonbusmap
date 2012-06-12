package boston.Bus.Map.data;

import java.util.List;

public interface VehicleLocationGroup extends LocationGroup {
	String getFirstVehicleNumber();
	List<String> getAllVehicleNumbers();
}
