package boston.Bus.Map.transit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import boston.Bus.Map.data.AlertsMapping;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.parser.AlertParser;
import boston.Bus.Map.util.DownloadHelper;

import android.graphics.drawable.Drawable;

public class BusTransitSource extends NextBusTransitSource {

	
	private final HashMap<String, Integer> alertKeys;

	public BusTransitSource(TransitSystem transitSystem, Drawable busStop, Drawable bus,
			Drawable arrow, AlertsMapping alertsMapping)
	{
		super(transitSystem, busStop, bus, arrow, "mbta", boston.Bus.Map.R.raw.routeconfig);
		
		alertKeys = alertsMapping.getAlertNumbers(getRoutes(), getRouteKeysToTitles());
	}
	
	@Override
	protected void addRoutes()
	{
		addRoute("602", "Green Shuttle");
		addRoute("701", "CT1");
		addRoute("747", "CT2");
		addRoute("708", "CT3");
		addRoute("741", "Silver Line SL1");
		addRoute("742", "Silver Line SL2");
		addRoute("751", "Silver Line SL4");
		addRoute("749", "Silver Line SL5");
		addRoute("746", "Silver Line SL");
		addRoute("1", "1");
		addRoute("4", "4");
		addRoute("5", "5");
		addRoute("7", "7");
		addRoute("8", "8");
		addRoute("9", "9");
		addRoute("10", "10");
		addRoute("11", "11");
		addRoute("14", "14");
		addRoute("15", "15");
		addRoute("16", "16");
		addRoute("17", "17");
		addRoute("18", "18");
		addRoute("19", "19");
		addRoute("21", "21");
		addRoute("22", "22");
		addRoute("23", "23");
		addRoute("24", "24");
		addRoute("2427", "24/27");
		addRoute("26", "26");
		addRoute("27", "27");
		addRoute("28", "28");
		addRoute("29", "29");
		addRoute("30", "30");
		addRoute("31", "31");
		addRoute("32", "32");
		addRoute("3233", "32/33");
		addRoute("33", "33");
		addRoute("34", "34");
		addRoute("34E", "34E");
		addRoute("35", "35");
		addRoute("36", "36");
		addRoute("37", "37");
		addRoute("38", "38");
		addRoute("3738", "37/38");
		addRoute("39", "39");
		addRoute("40", "40");
		addRoute("4050", "40/50");
		addRoute("41", "41");
		addRoute("42", "42");
		addRoute("43", "43");
		addRoute("44", "44");
		addRoute("45", "45");
		addRoute("47", "47");
		addRoute("48", "48");
		addRoute("50", "50");
		addRoute("51", "51");
		addRoute("52", "52");
		addRoute("55", "55");
		addRoute("57", "57");
		addRoute("57A", "57A");
		addRoute("59", "59");
		addRoute("60", "60");
		addRoute("62", "62");
		addRoute("627", "62/76");
		addRoute("64", "64");
		addRoute("65", "65");
		addRoute("66", "66");
		addRoute("67", "67");
		addRoute("68", "68");
		addRoute("69", "69");
		addRoute("70", "70");
		addRoute("70A", "70A");
		addRoute("71", "71");
		addRoute("72", "72");
		addRoute("725", "72/75");
		addRoute("73", "73");
		addRoute("74", "74");
		addRoute("75", "75");
		addRoute("76", "76");
		addRoute("77", "77");
		addRoute("78", "78");
		addRoute("79", "79");
		addRoute("80", "80");
		addRoute("83", "83");
		addRoute("84", "84");
		addRoute("85", "85");
		addRoute("86", "86");
		addRoute("87", "87");
		addRoute("88", "88");
		addRoute("89", "89");
		addRoute("8993", "89/93");
		addRoute("90", "90");
		addRoute("91", "91");
		addRoute("92", "92");
		addRoute("93", "93");
		addRoute("94", "94");
		addRoute("95", "95");
		addRoute("96", "96");
		addRoute("97", "97");
		addRoute("99", "99");
		addRoute("100", "100");
		addRoute("101", "101");
		addRoute("104", "104");
		addRoute("105", "105");
		addRoute("106", "106");
		addRoute("108", "108");
		addRoute("109", "109");
		addRoute("110", "110");
		addRoute("111", "111");
		addRoute("112", "112");
		addRoute("114", "114");
		addRoute("116", "116");
		addRoute("117", "117");
		addRoute("116117", "116/117");
		addRoute("119", "119");
		addRoute("120", "120");
		addRoute("121", "121");
		addRoute("131", "131");
		addRoute("132", "132");
		addRoute("134", "134");
		addRoute("136", "136");
		addRoute("137", "137");
		addRoute("170", "170");
		addRoute("171", "171");
		addRoute("191", "191");
		addRoute("192", "192");
		addRoute("193", "193");
		addRoute("201", "201");
		addRoute("202", "202");
		addRoute("210", "210");
		addRoute("211", "211");
		addRoute("212", "212");
		addRoute("214", "214");
		addRoute("214216", "214/216");
		addRoute("215", "215");
		addRoute("216", "216");
		addRoute("217", "217");
		addRoute("220", "220");
		addRoute("221", "221");
		addRoute("222", "222");
		addRoute("222P", "222P");
		addRoute("225", "225");
		addRoute("230", "230");
		addRoute("236", "236");
		addRoute("238", "238");
		addRoute("240", "240");
		addRoute("245", "245");
		addRoute("274", "274");
		addRoute("275", "275");
		addRoute("276", "276");
		addRoute("277", "277");
		addRoute("325", "325");
		addRoute("326", "326");
		addRoute("350", "350");
		addRoute("351", "351");
		addRoute("352", "352");
		addRoute("354", "354");
		addRoute("355", "355");
		addRoute("411", "411");
		addRoute("424", "424");
		addRoute("426", "426");
		addRoute("426439", "426/439");
		addRoute("426455", "426/455");
		addRoute("428", "428");
		addRoute("429", "429");
		addRoute("430", "430");
		addRoute("431", "431");
		addRoute("434", "434");
		addRoute("435", "435");
		addRoute("436", "436");
		addRoute("439", "439");
		addRoute("441", "441");
		addRoute("442", "442");
		addRoute("441442", "441/442");
		addRoute("448", "448");
		addRoute("449", "449");
		addRoute("450", "450");
		addRoute("451", "451");
		addRoute("455", "455");
		addRoute("456", "456");
		addRoute("459", "459");
		addRoute("465", "465");
		addRoute("468", "468");
		addRoute("500", "500");
		addRoute("501", "501");
		addRoute("502", "502");
		addRoute("503", "503");
		addRoute("504", "504");
		addRoute("505", "505");
		addRoute("553", "553");
		addRoute("554", "554");
		addRoute("555", "555");
		addRoute("556", "556");
		addRoute("558", "558");
		addRoute("9109", "9109");
		addRoute("9111", "9111");
		addRoute("9501", "9501");
		addRoute("9507", "9507");
		addRoute("9701", "9701");
		addRoute("9702", "9702");
		addRoute("9703", "9703");
		
		

	}

	@Override
	protected int getInitialContentLength() {
		return 339159;
	}

	@Override
	public String searchForRoute(String indexingQuery, String lowercaseQuery) {
		//TODO: don't hard code this
		if ("sl1".equals(lowercaseQuery) || 
				"sl2".equals(lowercaseQuery) ||
				"sl".equals(lowercaseQuery) ||
				"sl4".equals(lowercaseQuery) ||
				"sl5".equals(lowercaseQuery))
		{
			lowercaseQuery = "silverline" + lowercaseQuery;
		}
		else if (lowercaseQuery.startsWith("silver") && lowercaseQuery.contains("line") == false)
		{
			//ugh, what a hack
			lowercaseQuery = lowercaseQuery.substring(0, 6) + "line" + lowercaseQuery.substring(6);
		}
		
		return super.searchForRoute(indexingQuery, lowercaseQuery);
	}

	@Override
	protected void parseAlert(RouteConfig routeConfig) throws ClientProtocolException, IOException, SAXException {

		String routeName = routeConfig.getRouteName();
		if (alertKeys.containsKey(routeName) == false)
		{
			//can't do anything here
			return;
		}
		
		int alertKey = alertKeys.get(routeName);
		String url = AlertsMapping.alertUrlPrefix + alertKey;
		DownloadHelper downloadHelper = new DownloadHelper(url);
		downloadHelper.connect();

		InputStream stream = downloadHelper.getResponseData();
		InputStreamReader data = new InputStreamReader(stream);

		AlertParser parser = new AlertParser();
		parser.runParse(data);
		routeConfig.setAlerts(parser.getAlerts());
		data.close();

		
	}

}
