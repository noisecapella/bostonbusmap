package boston.Bus.Map.data;

import android.graphics.drawable.Drawable;

public class CommuterTrainLocation extends BusLocation {

	public CommuterTrainLocation(float latitude, float longitude, String id,
			long lastFeedUpdateInMillis, long lastUpdateInMillis,
			String heading, boolean predictable, String dirTag,
			String routeName, Directions directions, String routeTitle) {
		super(latitude, longitude, id, lastFeedUpdateInMillis, lastUpdateInMillis,
				heading, predictable, dirTag, routeName,
				directions, routeTitle);
		// TODO Auto-generated constructor stub
	}

	private static final String experimentalString = "<font color='red' size='1'>Commuter rail predictions are experimental</font>";
	
	@Override
	protected String getBusNumberMessage() {
		return "Train number: " + busId + "<br />\n";
	}
	
	@Override
	public boolean isDisappearAfterRefresh() {
		return true;
	}
}
