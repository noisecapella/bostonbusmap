package boston.Bus.Map;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class BusOverlayItem extends OverlayItem {

	public BusOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setMarker(Drawable marker) {
		// TODO Auto-generated method stub
		super.setMarker(marker);
	}
}
