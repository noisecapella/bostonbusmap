package boston.Bus.Map.transit;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import boston.Bus.Map.data.RouteConfig;
import android.graphics.drawable.Drawable;

public class SFBusTransitSource extends NextBusTransitSource
{
	public SFBusTransitSource(TransitSystem system, Drawable busStop, Drawable busStopUpdated, Drawable bus, Drawable arrow)
	{
		super(system, busStop, busStopUpdated, bus, arrow, "sf-muni", com.schneeloch.sftransit.R.raw.routeconfig);
	}

	@Override
	protected void addRoutes()
	{
		addRoute("F", "F-Market &amp; Wharves");
		addRoute("J", "J-Church");
		addRoute("KT", "KT-Ingleside/Third Street");
		addRoute("L", "L-Taraval");
		addRoute("M", "M-Ocean View");
		addRoute("N", "N-Judah");
		addRoute("NX", "NX-N Express");
		addRoute("1", "1-California");
		addRoute("1AX", "1AX-California A Express");
		addRoute("1BX", "1BX-California B Express");
		addRoute("2", "2-Clement");
		addRoute("3", "3-Jackson");
		addRoute("5", "5-Fulton");
		addRoute("6", "6-Parnassus");
		addRoute("8X", "8X-Bayshore Exp");
		addRoute("8AX", "8AX-Bayshore A Exp");
		addRoute("8BX", "8BX-Bayshore B Exp");
		addRoute("9", "9-San Bruno");
		addRoute("9L", "9L-San Bruno Limited");
		addRoute("10", "10-Townsend");
		addRoute("12", "12-Folsom/Pacific");
		addRoute("14", "14-Mission");
		addRoute("14L", "14L-Mission Limited");
		addRoute("14X", "14X-Mission Express");
		addRoute("16X", "16X-Noriega Express");
		addRoute("17", "17-Park Merced");
		addRoute("18", "18-46th Avenue");
		addRoute("19", "19-Polk");
		addRoute("21", "21-Hayes");
		addRoute("22", "22-Fillmore");
		addRoute("23", "23-Monterey");
		addRoute("24", "24-Divisadero");
		addRoute("27", "27-Bryant");
		addRoute("28", "28-19th Avenue");
		addRoute("28L", "28L-19th Avenue Limited");
		addRoute("29", "29-Sunset");
		addRoute("30", "30-Stockton");
		addRoute("30X", "30X-Marina Express");
		addRoute("31", "31-Balboa");
		addRoute("31AX", "31AX-Balboa A Express");
		addRoute("31BX", "31BX-Balboa B Express");
		addRoute("33", "33-Stanyan");
		addRoute("35", "35-Eureka");
		addRoute("36", "36-Teresita");
		addRoute("37", "37-Corbett");
		addRoute("38", "38-Geary");
		addRoute("38AX", "38AX-Geary A Express");
		addRoute("38BX", "38BX-Geary B Express");
		addRoute("38L", "38L-Geary Limited");
		addRoute("39", "39-Coit");
		addRoute("41", "41-Union");
		addRoute("43", "43-Masonic");
		addRoute("44", "44-O&apos;Shaughnessy");
		addRoute("45", "45-Union/Stockton");
		addRoute("47", "47-Van Ness");
		addRoute("48", "48-Quintara - 24th Street");
		addRoute("49", "49-Mission-Van Ness");
		addRoute("52", "52-Excelsior");
		addRoute("54", "54-Felton");
		addRoute("56", "56-Rutland");
		addRoute("66", "66-Quintara");
		addRoute("67", "67-Bernal Heights");
		addRoute("71", "71-Haight-Noriega");
		addRoute("71L", "71L-Haight-Noriega Limited");
		addRoute("76", "76-Marin Headlands");
		addRoute("80X", "80X-Gateway Express");
		addRoute("81X", "81X-Caltrain Express");
		addRoute("82X", "82X-Levi Plaza Express");
		addRoute("88", "88-B.A.R.T. Shuttle");
		addRoute("90", "90-San Bruno Owl");
		addRoute("91", "91-Owl");
		addRoute("108", "108-Treasure Island");
		addRoute("K OWL", "K-Owl");
		addRoute("L OWL", "L-Owl");
		addRoute("M OWL", "M-Owl");
		addRoute("N OWL", "N-Owl");
		addRoute("T OWL", "T-Owl");
		addRoute("59", "Powell/Mason Cable Car");
		addRoute("60", "Powell/Hyde Cable Car");
		addRoute("61", "California Cable Car");
		
	}

	@Override
	protected int getInitialContentLength() {
		return 125167;
	}

	@Override
	protected void parseAlert(RouteConfig routeConfig)
			throws ClientProtocolException, IOException, SAXException {
		// do nothing
		
	}

}
