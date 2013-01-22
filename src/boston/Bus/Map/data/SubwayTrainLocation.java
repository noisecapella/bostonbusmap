package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class SubwayTrainLocation extends BusLocation {

	public SubwayTrainLocation(float latitude, float longitude, String id,
			long lastFeedUpdateInMillis, long lastUpdateInMillis,
			String heading, boolean predictable, String dirTag,
			String inferBusRoute, TransitDrawables drawables,
			String routeName, Directions directions, String routeTitle,
			boolean disappearAfterRefresh) {
		super(latitude, longitude, id, lastFeedUpdateInMillis, lastUpdateInMillis,
				heading, predictable, dirTag, inferBusRoute, drawables, routeName,
				directions, routeTitle, disappearAfterRefresh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected String getBusNumberMessage() {
		return "";
	}

}
