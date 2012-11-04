package boston.Bus.Map.transit;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;
import android.graphics.drawable.Drawable;

public class LABusTransitSource extends NextBusTransitSource
{
	public LABusTransitSource(TransitSystem system, TransitDrawables drawables,
			TransitSourceTitles transitSourceTitles, RouteTitles allRouteTitles)
	{
		super(system, drawables, "lametro", transitSourceTitles, allRouteTitles);
	}

	@Override
	protected void parseAlert(RouteConfig routeConfig,
			AlertsMapping alertMapping) throws ClientProtocolException,
			IOException, SAXException {
		// alerts are currently not supported for Los Angeles

	}
}
