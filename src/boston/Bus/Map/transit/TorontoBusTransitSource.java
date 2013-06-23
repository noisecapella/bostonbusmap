package boston.Bus.Map.transit;


import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.data.TransitSourceTitles;

import com.schneeloch.torontotransit.R;

import android.graphics.drawable.Drawable;

public class TorontoBusTransitSource extends NextBusTransitSource {

	public TorontoBusTransitSource(TransitSystem transitSystem,
			TransitDrawables drawables, TransitSourceTitles transitSourceTitles,
			RouteTitles allRouteTitles) {
		super(transitSystem, drawables, "ttc", transitSourceTitles, allRouteTitles);
	}
	
}