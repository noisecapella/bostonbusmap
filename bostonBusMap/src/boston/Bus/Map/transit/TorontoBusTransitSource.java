package boston.Bus.Map.transit;


import com.schneeloch.torontotransit.R;

import android.graphics.drawable.Drawable;

public class TorontoBusTransitSource extends NextBusTransitSource {

	public TorontoBusTransitSource(TransitSystem transitSystem,
			Drawable busStop, Drawable bus, Drawable arrow) {
		super(transitSystem, busStop, bus, arrow, "ttc", R.raw.routeconfig);
	}

	@Override
	protected void addRoutes() {
		throw new RuntimeException();

	}

	@Override
	protected int getInitialContentLength() {
		throw new RuntimeException();
	}

}
