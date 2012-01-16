package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class SubwayTrainLocation extends BusLocation {

	public SubwayTrainLocation(float latitude, float longitude, String id,
			long lastFeedUpdateInMillis, long lastUpdateInMillis,
			String heading, boolean predictable, String dirTag,
			String inferBusRoute, Drawable bus, Drawable arrow,
			String routeName, Directions directions, String routeTitle,
			boolean disappearAfterRefresh,
			int arrowTopDiff) {
		super(latitude, longitude, id, lastFeedUpdateInMillis, lastUpdateInMillis,
				heading, predictable, dirTag, inferBusRoute, bus, arrow, routeName,
				directions, routeTitle, disappearAfterRefresh, arrowTopDiff);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected String getBusNumberMessage() {
		return "";
	}

}
