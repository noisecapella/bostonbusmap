package boston.Bus.Map.transit;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.TransitDrawables;
import android.graphics.drawable.Drawable;

public class LABusTransitSource extends NextBusTransitSource
{
	public LABusTransitSource(TransitSystem system, TransitDrawables drawables)
	{
		super(system, drawables, "lametro", com.schneeloch.latransit.R.raw.routeconfig);
	}

	@Override
	protected void addRoutes()
	{
		addRoute("2", "2 Downtown LA - Pacific Palisades Via");
		addRoute("4", "4 Downtown LA - Santa Monica Via Santa");
		addRoute("10", "10 W Hollywood-Dtwn LA -Avalon Sta Via");
		addRoute("14", "14 Beverly Hlls-Dtwn LA-Wash/Fairfax Vi");
		addRoute("16", "16 Downtown LA - Century City Via West");
		addRoute("18", "18 Wilshire Ctr-Montebello Via 6th St-W");
		addRoute("20", "20 Downtown LA - Santa Monica Via Wilsh");
		addRoute("28", "28 Downtown LA - Century City Via West");
		addRoute("30", "30 W Hollywood - Dtwn LA - Indiana Sta");
		addRoute("33", "33 Downtown LA - Santa Monica Via Venic");
		addRoute("35", "35 Downtown LA- WLA Via Washington Bl &amp;");
		addRoute("37", "37 Beverly Hlls-Dtwn LA-Wash/Fairfax Vi");
		addRoute("38", "38 Downtown LA- WLA Via Washington Bl &amp;");
		addRoute("40", "40 Downtown LA-Sbay Galleria Via King B");
		addRoute("45", "45 Lincoln Heights - Dwntwn LA- Rosewoo");
		addRoute("48", "48 W Hollywood-Dtwn LA -Avalon Sta Via");
		addRoute("51", "51 Wilshr Ctr-Dtwn LA-Compton-Artesia T");
		addRoute("52", "52 Wilshr Ctr-Dtwn LA-Compton-Artesia T");
		addRoute("53", "53 Downtown LA - Csu Domiguez Hills Via");
		addRoute("55", "55 Downtown LA - Willowbrook Sta Via Co");
		addRoute("60", "60 Downtown LA - Artesia Station Via Lo");
		addRoute("62", "62 Downtown LA - Hawaiian Gardens Via T");
		addRoute("66", "66 Wilshire Ctr-Dtwn LA-Montebello Via");
		addRoute("68", "68 Eagle Rck-Dtw LA-Montbll Via Eagl Rc");
		addRoute("70", "70 Downtown LA - El Monte Via Marengo S");
		addRoute("71", "71 Downtown LA - Csu LA - Lac+usc Med C");
		addRoute("76", "76 Downtown LA - El Monte Via Valley Bl");
		addRoute("78", "78 Downtown LA - Arcadia Via Las Tunas-");
		addRoute("79", "79 Downtown LA - Arcadia Via Las Tunas-");
		addRoute("81", "81 Eagle Rock- Dwntwn LA- Harbor Fwy St");
		addRoute("83", "83 Downtown LA - Eagle Rock Via York Bl");
		addRoute("84", "84 Eagle Rck-Dtw LA-Montbll Via Eagl Rc");
		addRoute("90", "90 Downtown LA- Sunland Via Foothill Bl");
		addRoute("91", "91 Downtown LA- Sunland Via Foothill Bl");
		addRoute("92", "92 Dwntwn LA- Burbank Sta Via Glendale");
		addRoute("94", "94 Downtown LA - Sun Valley Via San Fer");
		addRoute("96", "96 Dtwn LA- Burbank Sta Via Griffith Pk");
		addRoute("102", "102 LAX Cty Bus Ctr - S Gate Via LA Tije");
		addRoute("105", "105 W Hollywood - Vernon Via La Cienega");
		addRoute("108", "108 Marina Del Rey - Pico Rivera Via Sla");
		addRoute("110", "110 Playa Vista- Bell Gardens Via Jeffer");
		addRoute("111", "111 LAX City Bus Ctr - Norwalk Sta Via F");
		addRoute("115", "115 Playa Del Rey - Norwalk Via Manchest");
		addRoute("117", "117 LAX Cty Bus Ctr-Downey Via Century B");
		addRoute("120", "120 Aviation Station- Whittwood Mall Via");
		addRoute("125", "125 El Segundo - Norwalk Station Via Ros");
		addRoute("126", "126 Manhattn Beach- Hawthn/Lennox Sta Vi");
		addRoute("127", "127 Compton Sta - Downey Via Compton - S");
		addRoute("128", "128 Compton Sta - Cerritos Towne Center");
		addRoute("130", "130 Redondo Beach - Los Cerritos Center");
		addRoute("150", "150 Canoga Pk-Northridge-Universal City");
		addRoute("152", "152 Woodland Hills - N Hollywood Sta Via");
		addRoute("154", "154 Tarzana - Burbank Sta Via Oxnard St");
		addRoute("155", "155 Sherman Oaks-Burbank Sta Via Riversi");
		addRoute("156", "156 Panorama -Van Nuys-Hollywood Via Cha");
		addRoute("158", "158 Chatsworth Sta-Sherman Oaks Via Devo");
		addRoute("161", "161 Thousand Oaks -Agoura Hills -Calabas");
		addRoute("162", "162 West Hills -Sun Valley-N Hollywood V");
		addRoute("163", "163 West Hills -Sun Valley-N Hollywood V");
		addRoute("164", "164 West Hills - Burbank Via Victory Bl");
		addRoute("165", "165 West Hills - Burbank Via Vanowen St");
		addRoute("166", "166 Chatsworth Sta -Sun Valley Via Nordh");
		addRoute("167", "167 Chatsworth Sta-Studio Cty-Csun Via P");
		addRoute("169", "169 West Hills -Sunland Via Saticoy St -");
		addRoute("175", "175 Silverlake - Hollywood Via Hyperion");
		addRoute("176", "176 Highland Park - Montebello Via Missi");
		addRoute("177", "177 JPL-Pasadena-Pcc College Via Califor");
		addRoute("180", "180 Hollywood-Glendale-Pasadena Via Los");
		addRoute("181", "181 Hollywood-Glendale-Pasadena Via Los");
		addRoute("183", "183 Sherman Oaks - Glendale Via Magnolia");
		addRoute("190", "190 El Monte Sta - Cal Poly Pomona Via R");
		addRoute("194", "194 El Monte Sta - Cal Poly Pomona Via R");
		addRoute("200", "200 Echo Park- Exposition Park Via Alvar");
		addRoute("201", "201 Glendale - Koreatown Via Silverlake");
		addRoute("202", "202 Willowbrook - Compton- Wilmington Vi");
		addRoute("204", "204 Hollywood -Athens Via Vermont Av");
		addRoute("205", "205 Willowbrook Sta-San Pedro Via Wilmgt");
		addRoute("206", "206 Hollywood - Athens Via Normandie Av");
		addRoute("207", "207 Hollywood - Athens Via Western Av");
		addRoute("209", "209 Wilshire Center - Athens Via Van Nes");
		addRoute("210", "210 Hllywd/Vine Sta - So Bay Galleria Vi");
		addRoute("211", "211 Inglewood-South Bay Galleria Via Pra");
		addRoute("212", "212 Hllywd/Vine Sta - Hawthorne/Lennox S");
		addRoute("215", "215 Inglewood-South Bay Galleria Via Pra");
		addRoute("217", "217 Hlywd/Vine Sta-Culver City Tc Via Hl");
		addRoute("218", "218 Studio Cty-Cedars Sinai Med Via Laur");
		addRoute("220", "220 Beverly Ctr - Culver City Sta Via Ro");
		addRoute("222", "222 Sun Valley - Hollywood Via Hollywood");
		addRoute("224", "224 Sylmar-Universal Cty Via San Fernand");
		addRoute("230", "230 Mission College - Studio City Via LA");
		addRoute("232", "232 LAX City Bus Ctr-Long Beach-Via Sepu");
		addRoute("233", "233 Lake View Terr - Sherman Oaks Via Va");
		addRoute("234", "234 Sylmar - Sherman Oaks Via Sepulveda");
		addRoute("236", "236 Sylmar Sta-Encino- Sherman Oaks Via");
		addRoute("237", "237 Sylmar Sta-Encino- Sherman Oaks Via");
		addRoute("239", "239 Sylmar Sta - Encino Via White Oak -");
		addRoute("240", "240 Canoga Pk-Northridge-Universal City");
		addRoute("242", "242 Porter Rnch-Woodland Hils Via Tampa");
		addRoute("243", "243 Porter Rnch-Woodland Hils Via Tampa");
		addRoute("244", "244 Chatsworth Sta-Woodlnd Hls Via De So");
		addRoute("245", "245 Chatsworth Sta-Woodlnd Hls Via De So");
		addRoute("246", "246 San Pedro - Artesia Transit Center V");
		addRoute("251", "251 Cypress Park- Long Bch Green Line St");
		addRoute("252", "252 Lincoln Heights - Boyle Heights Via");
		addRoute("254", "254 Boyle Hghts-103rd/Watts Twrs Sta Vi");
		addRoute("256", "256 Eastern Av - Avenue 64 - North Hill");
		addRoute("258", "258 Alhambra - Paramount Via Fremont - E");
		addRoute("260", "260 Altadena - Artesia Sta Via Fair Oaks");
		addRoute("264", "264 Pasadena - Altadena - City Of Hope -");
		addRoute("265", "265 Pico Rivera - Lakewood Ctr Mall Via");
		addRoute("266", "266 S Madre Villa Sta-Lakewood Ctr Mall");
		addRoute("267", "267 Pasadena - Altadena - City Of Hope -");
		addRoute("268", "268 Altadena - El Monte Sta Via Washingt");
		addRoute("270", "270 Monrovia - Norwalk Sta Via Workman M");
		addRoute("290", "290 Sylmar - Sunland Via Foothill Bl");
		addRoute("292", "292 Sylmar Sta-Burbank Sta Via Glenoaks");
		addRoute("302", "302 Downtown LA - Pacific Palisades Via");
		addRoute("311", "311 LAX City Bus Ctr - Norwalk Sta Via F");
		addRoute("312", "312 Hllywd/Vine Sta - Hawthorne/Lennox S");
		addRoute("316", "316 Downtown LA - Century City Via West");
		addRoute("330", "330 W Hollywood - Dtwn LA - Indiana Sta");
		addRoute("344", "344 Artesia Transit Ctr- Palos Verdes Vi");
		addRoute("352", "352 Wilshr Ctr-Dtwn LA-Compton-Artesia T");
		addRoute("353", "353 Woodland Hills - N Hollywood Sta Via");
		addRoute("355", "355 Downtown LA - Willowbrook Sta Via Co");
		addRoute("358", "358 Marina Del Rey - Pico Rivera Via Sla");
		addRoute("364", "364 Chatsworth Sta -Sun Valley Via Nordh");
		addRoute("378", "378 Downtown LA - Arcadia Via Las Tunas-");
		addRoute("442", "442 Downtown LA - Hawthorne/Lennox Sta-");
		addRoute("450", "450 Downtown LA - San Pedro Via Harbor T");
		addRoute("460", "460 Downtown LA - Disneyland Via Harbor");
		addRoute("485", "485 Downtown LA - Altadena Via Fremont -");
		addRoute("487", "487 Downtown LA - S Madre Villa Sta - El");
		addRoute("489", "489 Downtown LA - S Madre Villa Sta - El");
		addRoute("534", "534 Malibu - Washington/Fairfax Via Paci");
		addRoute("550", "550 Jefferson Park - San Pedro Via Harbo");
		addRoute("577", "577 El Monte Sta- Long Beach Va Med Ctr");
		addRoute("603", "603 Glendale-Grand Sta Via San Fernando-");
		addRoute("605", "605 Lac+usc Med Ctr Out Patient - Olympi");
		addRoute("607", "607 Windsor Hills - Inglewood Shuttle");
		addRoute("611", "611 Huntington Park Shuttle");
		addRoute("612", "612 South Gate Shuttle");
		addRoute("620", "620 Boyle Heights Shuttle");
		addRoute("625", "625 Metro Green Line Shuttle");
		addRoute("645", "645 West Hills-Warner Ctr Via Valley Cir");
		addRoute("656", "656 Panorama -Van Nuys-Hollywood Via Cha");
		addRoute("665", "665 Cal State LA - City Terrace Shuttle");
		addRoute("685", "685 Glendale College - Glassell Park Vi");
		addRoute("686", "686 Altadena-Pasadena Via Fair Oaks-Los");
		addRoute("687", "687 Altadena-Pasadena Via Fair Oaks-Los");
		addRoute("704", "704 Downtown LA - Santa Monica Via Santa");
		addRoute("705", "705 W Hollywood - Vernon Via La Cienega");
		addRoute("710", "710 Wilshire Ctr - South Bay Galleria Vi");
		addRoute("720", "720 Santa Monica-Commerce Via Wilshire -");
		addRoute("728", "728 Downtown LA - Century City Via West");
		addRoute("733", "733 Downtown LA - Santa Monica Via Venic");
		addRoute("734", "734 Sylmar Sta - Sherman Oaks Via Sepulv");
		addRoute("740", "740 Expo/Crenshaw Sta - So Bay Galleria");
		addRoute("741", "741 Northridge - Tarzana - Csun Via Rese");
		addRoute("745", "745 Downtown LA - Harbor Freeway Station");
		addRoute("750", "750 Warner Ctr - Universal City Via Vent");
		addRoute("751", "751 Cypress Park - Huntington Park Via S");
		addRoute("754", "754 Hollywood - Athens Via Vermont Av");
		addRoute("757", "757 Hollywood - Crenshaw Station Via Wes");
		addRoute("760", "760 Downtown LA - Long Beach Gl Sta Via");
		addRoute("761", "761 Pacoima-Westwood Via Van Nuys Bl - S");
		addRoute("762", "762 Pasadena - Artesia Station Via Atlan");
		addRoute("770", "770 Downtown LA - El Monte Sta Via Garve");
		addRoute("780", "780 Pasadena-Washtn/Fairfax Via Colorado");
		addRoute("794", "794 Downtown LA - Sylmar Sta Via San Fer");
		addRoute("901", "901 Metro Orange Line");
		addRoute("910", "910 El Monte Sta - Downtown LA - Artesa");
	}

	@Override
	protected int getInitialContentLength() {
		return 521633;
	}

	@Override
	protected void parseAlert(RouteConfig routeConfig)
			throws ClientProtocolException, IOException, SAXException {
		// alerts are not currently supported
		
	}

}
