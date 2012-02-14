package boston.Bus.Map.transit;


import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import boston.Bus.Map.data.RouteConfig;

import com.schneeloch.torontotransit.R;

import android.graphics.drawable.Drawable;

public class TorontoBusTransitSource extends NextBusTransitSource {

	public TorontoBusTransitSource(TransitSystem transitSystem,
			Drawable busStop, Drawable busStopUpdated, Drawable bus, Drawable arrow) {
		super(transitSystem, busStop, busStopUpdated, bus, arrow, "ttc", R.raw.routeconfig);
	}

	@Override
	protected void addRoutes() 
	{
		addRoute("1S", "1S-Yonge Subway Shuttle");
		addRoute("5", "5-Avenue Rd");
		addRoute("6", "6-Bay");
		addRoute("7", "7-Bathurst");
		addRoute("8", "8-Broadview");
		addRoute("9", "9-Bellamy");
		addRoute("10", "10-Van Horne");
		addRoute("11", "11-Bayview");
		addRoute("12", "12-Kingston Rd");
		addRoute("14", "14-Glencairn");
		addRoute("15", "15-Evans");
		addRoute("16", "16-Mccowan");
		addRoute("17", "17-Birchmount");
		addRoute("20", "20-Cliffside");
		addRoute("21", "21-Brimley");
		addRoute("22", "22-Coxwell");
		addRoute("23", "23-Dawes");
		addRoute("24", "24-Victoria Park");
		addRoute("25", "25-Don Mills");
		addRoute("26", "26-Dupont");
		addRoute("28", "28-Davisville");
		addRoute("29", "29-Dufferin");
		addRoute("30", "30-Lambton");
		addRoute("31", "31-Greenwood");
		addRoute("32", "32-Eglinton West");
		addRoute("33", "33-Forest Hill");
		addRoute("34", "34-Eglinton East");
		addRoute("35", "35-Jane");
		addRoute("36", "36-Finch West");
		addRoute("37", "37-Islington");
		addRoute("38", "38-Highland Creek");
		addRoute("39", "39-Finch East");
		addRoute("40", "40-Junction");
		addRoute("41", "41-Keele");
		addRoute("42", "42-Cummer");
		addRoute("43", "43-Kennedy");
		addRoute("44", "44-Kipling South");
		addRoute("45", "45-Kipling");
		addRoute("46", "46-Martin Grove");
		addRoute("47", "47-Lansdowne");
		addRoute("48", "48-Rathburn");
		addRoute("49", "49-Bloor West");
		addRoute("50", "50-Burnhamthorpe");
		addRoute("51", "51-Leslie");
		addRoute("52", "52-Lawrence West");
		addRoute("53", "53-Steeles East");
		addRoute("54", "54-Lawrence East");
		addRoute("55", "55-Warren Park");
		addRoute("56", "56-Leaside");
		addRoute("57", "57-Midland");
		addRoute("58", "58-Malton");
		addRoute("59", "59-Maple Leaf");
		addRoute("60", "60-Steeles West");
		addRoute("61", "61-Avenue Rd North");
		addRoute("62", "62-Mortimer");
		addRoute("63", "63-Ossington");
		addRoute("64", "64-Main");
		addRoute("65", "65-Parliament");
		addRoute("66", "66-Prince Edward");
		addRoute("67", "67-Pharmacy");
		addRoute("68", "68-Warden");
		addRoute("69", "69-Warden South");
		addRoute("70", "70-O'Connor");
		addRoute("71", "71-Runnymede");
		addRoute("72", "72-Pape");
		addRoute("73", "73-Royal York");
		addRoute("74", "74-Mt Pleasant");
		addRoute("75", "75-Sherbourne");
		addRoute("76", "76-Royal York South");
		addRoute("77", "77-Swansea");
		addRoute("78", "78-St Andrews");
		addRoute("79", "79-Scarlett Rd");
		addRoute("80", "80-Queensway");
		addRoute("81", "81-Thorncliffe Park");
		addRoute("82", "82-Rosedale");
		addRoute("83", "83-Jones");
		addRoute("84", "84-Sheppard West");
		addRoute("85", "85-Sheppard East");
		addRoute("86", "86-Scarborough");
		addRoute("87", "87-Cosburn");
		addRoute("88", "88-South Leaside");
		addRoute("89", "89-Weston");
		addRoute("90", "90-Vaughan");
		addRoute("91", "91-Woodbine");
		addRoute("92", "92-Woodbine South");
		addRoute("94", "94-Wellesley");
		addRoute("95", "95-York Mills");
		addRoute("96", "96-Wilson");
		addRoute("97", "97-Yonge");
		addRoute("98", "98-Willowdale - Senlac");
		addRoute("99", "99-Arrow Road");
		addRoute("100", "100-Flemingdon Park");
		addRoute("101", "101-Downsview Park");
		addRoute("102", "102-Markham Rd");
		addRoute("103", "103-Mt Pleasant North");
		addRoute("104", "104-Faywood");
		addRoute("105", "105-Dufferin North");
		addRoute("106", "106-York University");
		addRoute("107", "107-Keele North");
		addRoute("108", "108-Downsview");
		addRoute("109", "109-Ranee");
		addRoute("110", "110-Islington South");
		addRoute("111", "111-East Mall");
		addRoute("112", "112-West Mall");
		addRoute("113", "113-Danforth");
		addRoute("115", "115-Silver Hills");
		addRoute("116", "116-Morningside");
		addRoute("117", "117-Alness");
		addRoute("120", "120-Calvington");
		addRoute("122", "122-Graydon Hall");
		addRoute("123", "123-Shorncliffe");
		addRoute("124", "124-Sunnybrook");
		addRoute("125", "125-Drewry");
		addRoute("126", "126-Christie");
		addRoute("127", "127-Davenport");
		addRoute("129", "129-Mccowan North");
		addRoute("130", "130-Middlefield");
		addRoute("131", "131-Nugget");
		addRoute("132", "132-Milner");
		addRoute("133", "133-Neilson");
		addRoute("134", "134-Progress");
		addRoute("135", "135-Gerrard");
		addRoute("139", "139-Finch - Don Mills");
		addRoute("141", "141-Downtown/Mt Pleasant Express");
		addRoute("142", "142-Downtown/Avenue Rd Express");
		addRoute("143", "143-Downtown/Beach Express");
		addRoute("144", "144-Downtown/Don Valley Express");
		addRoute("145", "145-Downtown/Humber Bay Express");
		addRoute("160", "160-Bathurst North");
		addRoute("161", "161-Rogers Rd");
		addRoute("162", "162-Lawrence - Donway");
		addRoute("165", "165-Weston Rd North");
		addRoute("167", "167-Pharmacy North");
		addRoute("168", "168-Symington");
		addRoute("169", "169-Huntingwood");
		addRoute("171", "171-Mount Dennis");
		addRoute("190", "190-Scarborough Centre Rocket");
		addRoute("191", "191-Highway 27 Rocket");
		addRoute("192", "192-Airport Rocket");
		addRoute("196", "196-York University Rocket");
		addRoute("199", "199-Finch Rocket");
		addRoute("224", "224-Victoria Park North");
		addRoute("300", "300-Bloor - Danforth");
		addRoute("301", "301-Queen");
		addRoute("302", "302-Danforth Rd - Mccowan");
		addRoute("303", "303-Don Mills");
		addRoute("305", "305-Eglinton East");
		addRoute("306", "306-Carlton");
		addRoute("307", "307-Eglinton West");
		addRoute("308", "308-Finch East");
		addRoute("309", "309-Finch West");
		addRoute("310", "310-Bathurst");
		addRoute("311", "311-Islington");
		addRoute("312", "312-St Clair");
		addRoute("313", "313-Jane");
		addRoute("316", "316-Ossington");
		addRoute("319", "319-Wilson");
		addRoute("320", "320-Yonge");
		addRoute("321", "321-York Mills");
		addRoute("322", "322-Coxwell");
		addRoute("324", "324-Victoria Park");
		addRoute("329", "329-Dufferin");
		addRoute("352", "352-Lawrence West");
		addRoute("353", "353-Steeles East");
		addRoute("354", "354-Lawrence East");
		addRoute("385", "385-Sheppard East");
		addRoute("501", "501-Queen");
		addRoute("502", "502-Downtowner");
		addRoute("503", "503-Kingston Rd");
		addRoute("504", "504-King");
		addRoute("505", "505-Dundas");
		addRoute("506", "506-Carlton");
		addRoute("508", "508-Lake Shore Rd");
		addRoute("509", "509-Harbourfront");
		addRoute("510", "510-Spadina");
		addRoute("511", "511-Bathurst");
		addRoute("512", "512-St Clair");
	}

	@Override
	protected int getInitialContentLength() {
		return 445356;
	}

	@Override
	protected void parseAlert(RouteConfig routeConfig)
			throws ClientProtocolException, IOException, SAXException {
		// we don't support alerts for toronto right now
	}

}