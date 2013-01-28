package boston.Bus.Map.ui;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class Camera {
	public static void animateCamera(GoogleMap map, boston.Bus.Map.data.Location location) {
		LatLng latlng = new LatLng(location.getLatitudeAsDegrees(), location.getLongitudeAsDegrees());

		animateCamera(map, latlng);
	}

	public static void animateCamera(GoogleMap map, LatLng latlng) {
		map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
		map.moveCamera(CameraUpdateFactory.scrollBy(0, -100));

		
	}

}
