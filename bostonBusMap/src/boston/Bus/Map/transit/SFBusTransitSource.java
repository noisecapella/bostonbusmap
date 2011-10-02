package boston.Bus.Map.transit;

import android.graphics.drawable.Drawable;

public class SFBusTransitSource extends NextBusTransitSource
{
	public SFBusTransitSource(TransitSystem system, Drawable busStop, Drawable bus, Drawable arrow)
	{
		super(system, busStop, bus, arrow, "sf-muni", com.schneeloch.sftransit.R.raw.routeconfig);
	}

	@Override
	protected void addRoutes()
	{
		throw new RuntimeException("TODO");
	}

	@Override
	protected int getInitialContentLength() {
		throw new RuntimeException("TODO");
	}

}
