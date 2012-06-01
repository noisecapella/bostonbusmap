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
		addRoute("2", "2 Downtown LA - Pacific Palisades Via Sunset Bl");
		addRoute("4", "4 Downtown LA - Santa Monica Via Santa Monica Bl");
		addRoute("10", "10 W Hollywood-Dtwn LA -Avalon Sta Via Melrose-Avalon");
		addRoute("14", "14 Beverly Hlls-Dtwn LA-Wash/Fairfax Via Beverly-Adam");
		addRoute("16", "16 Downtown LA - Century City Via West 3rd St");
		addRoute("18", "18 Wilshire Ctr-Montebello Via 6th St-Whittier Bl");
		addRoute("20", "20 Downtown LA - Santa Monica Via Wilshire Bl");
		addRoute("28", "28 Downtown LA - Century City Via West Olympic Bl");
		addRoute("30", "30 Pico Rimpau- Dwntwn LA- Indiana Sta Via Pico-E 1st");
		addRoute("33", "33 Downtown LA - Santa Monica Via Venice Bl");
		addRoute("35", "35 Downtown LA- WLA Via Washington Bl & Jefferson Bl");
		addRoute("37", "37 Beverly Hlls-Dtwn LA-Wash/Fairfax Via Beverly-Adam");
		addRoute("38", "38 Downtown LA- WLA Via Washington Bl & Jefferson Bl");
		addRoute("40", "40 Dtwn LA-LAX-Sbay Gallria Via King-Latijera-Hawthrn");
		addRoute("42", "42 Dtwn LA-LAX-Sbay Gallria Via King-Latijera-Hawthrn");
		addRoute("45", "45 Lincoln Heights - Dwntwn LA- Rosewood Via Broadway");
		addRoute("48", "48 W Hollywood-Dtwn LA -Avalon Sta Via Melrose-Avalon");
		addRoute("51", "51 Wilshr Ctr-Dtwn LA-Compton-Artesia Tran Via Avalon");
		addRoute("52", "52 Wilshr Ctr-Dtwn LA-Compton-Artesia Tran Via Avalon");
		addRoute("53", "53 Downtown LA - Csu Domiguez Hills Via Central Av");
		addRoute("55", "55 Downtown LA - Imperial/Wilmngtn Sta Via Compton Av");
		addRoute("60", "60 Downtown LA - Artesia Station Via Long Beach Bl");
		addRoute("62", "62 Downtown LA - Hawaiian Gardens Via Telegraph Rd");
		addRoute("66", "66 Wilshire Ctr- Dwntwn LA-Montebello Via 8th-Olympic");
		addRoute("68", "68 Eagle Rock-Dtwn LA-Montebello Via Eagle Rck-Chavez");
		addRoute("70", "70 Downtown LA - El Monte Via Marengo St- Garvey Av");
		addRoute("71", "71 Downtown LA - Cal. State Univ. LA");
		addRoute("76", "76 Downtown LA - El Monte Via Valley Bl");
		addRoute("78", "78 Downtown LA - Arcadia Via Las Tunas-Huntington Drs");
		addRoute("79", "79 Downtown LA - Arcadia Via Las Tunas-Huntington Drs");
		addRoute("81", "81 Eagle Rock- Dwntwn LA- Harbor Fwy Sta Via Figueroa");
		addRoute("83", "83 Downtown LA - Eagle Rock Via York Bl - Pasadena Av");
		addRoute("84", "84 Eagle Rock-Dtwn LA-Montebello Via Eagle Rck-Chavez");
		addRoute("90", "90 Downtown LA- Sunland Via Foothill Bl - Glendale Av");
		addRoute("91", "91 Downtown LA- Sunland Via Foothill Bl - Glendale Av");
		addRoute("92", "92 Dwntwn LA- Burbank Sta Via Glendale - Glenoaks Bls");
		addRoute("94", "94 Downtown LA - Sun Valley Via San Fernando Rd");
		addRoute("96", "96 Dtwn LA- Burbank Sta Via Griffith Pk Dr");
		addRoute("102", "102 Baldwin Village - South Gate Via Coliseum St");
		addRoute("105", "105 W Hollywood - Vernon Via La Cienega Bl - Vernon Av");
		addRoute("108", "108 Marina Del Rey - Pico Rivera Via Slauson Av");
		addRoute("110", "110 Playa Vista- Bell Gardens Via Jefferson Bl-Gage Av");
		addRoute("111", "111 LAX City Bus Ctr - Norwalk Sta Via Florence Av");
		addRoute("115", "115 Playa Del Rey - Norwalk Via Manchester - Firestone");
		addRoute("117", "117 LAX Cty Bus Ctr-Downey Via Century Bl-Imperial Hwy");
		addRoute("120", "120 Aviation Station- Whittwood Mall Via Imperial Hwy");
		addRoute("125", "125 El Segundo - Norwalk Station Via Ros");
		addRoute("126", "126 Manhattan Beach- Hawthorne Sta Via Mnhttn Beach Bl");
		addRoute("127", "127 Compton Sta - Downey Via Compton - Somerset Bls");
		addRoute("128", "128 Compton Sta - Cerritos Towne Center");
		addRoute("130", "130 Redondo Beach - Los Cerritos Center");
		addRoute("150", "150 Canoga Pk-Northridge-Universal City Via Ventura Bl");
		addRoute("152", "152 Woodland Hills - N Hollywood Sta Via Roscoe Bl");
		addRoute("154", "154 Tarzana - Burbank Sta Via Oxnard St - Burbank Bl");
		addRoute("155", "155 Sherman Oaks-Burbank Sta Via Riverside Dr-Olive St");
		addRoute("156", "156 Panorama -Van Nuys-Hollywood Via Chandler-Cahuenga");
		addRoute("158", "158 Chatsworth Sta-Sherman Oaks Via Devonshire-Woodman");
		addRoute("161", "161 Thousand Oaks -Agoura Hills -Calabasas -Warner Ctr");
		addRoute("163", "163 West Hills -Sun Valley-N Hollywood Via Sherman Way");
		addRoute("164", "164 West Hills - Burbank Via Victory Bl");
		addRoute("165", "165 West Hills - Burbank Via Vanowen St");
		addRoute("166", "166 Chatsworth Sta -Sun Valley Via Nordhoff-Osborne St");
		addRoute("167", "167 Chatsworth Sta-Studio Cty Via Plummer-Coldwater Cn");
		addRoute("169", "169 West Hills -Sunland Via Saticoy St - Sunland Bl");
		addRoute("175", "175 Silverlake - Hollywood Via Hyperion - Fountain Avs");
		addRoute("176", "176 Highland Park - Montebello Via Mission-Tyler-Rush");
		addRoute("177", "177 JPL-Pasadena-Sm Vlla Sta Via California - Foothill");
		addRoute("180", "180 Hollywood-Glendale-Pasadena Via Los Feliz-Colorado");
		addRoute("181", "181 Hollywood-Glendale-Pasadena Via Los Feliz-Colorado");
		addRoute("183", "183 Sherman Oaks - Glendale Via Magnolia Bl");
		addRoute("190", "190 El Monte Sta - Cal Poly Pomona Via Ramona - Valley");
		addRoute("194", "194 El Monte Sta - Cal Poly Pomona Via Ramona - Valley");
		addRoute("200", "200 Echo Park- Exposition Park Via Alvarado-Hoover Sts");
		addRoute("201", "201 Glendale - Koreatown Via Silverlake Bl");
		addRoute("202", "202 Willowbrook - Compton- Wilmington Via Alameda St");
		addRoute("204", "204 Hollywood -Athens Via Vermont Av");
		addRoute("205", "205 Imperial/Wilmgtn Sta-San Pedro Via W");
		addRoute("206", "206 Hollywood - Athens Via Normandie Av");
		addRoute("207", "207 Hollywood - Athens Via Western Av");
		addRoute("209", "209 Wilshire Center - Athens Via Van Ness Av");
		addRoute("210", "210 Hllywd/Vine Sta - So Bay Galleria Via Crenshaw Bl");
		addRoute("211", "211 Inglewood - Redondo Beach Via Prairie - Inglewood");
		addRoute("212", "212 Hollywood/Vine Sta - Hawthorne Sta Via La Brea Av");
		addRoute("215", "215 Inglewood - Redondo Beach Via Prairie - Inglewood");
		addRoute("217", "217 Hlywd/Vine Sta-Washngtn/Fairfax Via Hllywd-Fairfax");
		addRoute("218", "218 Studio Cty-Cedars Sinai Med Via Laurel Cyn-Fairfax");
		addRoute("220", "220 Beverly Ctr - Culver City Via Robertson Bl");
		addRoute("222", "222 Sun Valley - Hollywood Via Hollywood Way-Cahuenga");
		addRoute("224", "224 Sylmar-Universal Cty Via San Fernando - Lankershim");
		addRoute("230", "230 Mission College - Studio City Via Laurel Cyn Bl");
		addRoute("232", "232 LAX City Bus Ctr-Long Beach-Via Sepu");
		addRoute("233", "233 Lake View Terr - Sherman Oaks Via Van Nuys Bl");
		addRoute("234", "234 Sylmar - Sherman Oaks Via Sepulveda Bl");
		addRoute("236", "236 Sylmar Sta-Encino- Sherman Oaks Via Balboa-Woodley");
		addRoute("237", "237 Sylmar Sta-Encino- Sherman Oaks Via Balboa-Woodley");
		addRoute("239", "239 Sylmar Sta - Encino Via White Oak - Zelzah Avs");
		addRoute("240", "240 Canoga Pk-Northridge-Universal City Via Ventura Bl");
		addRoute("242", "242 Porter Rnch-Woodland Hils Via Tampa - Winnetka Avs");
		addRoute("243", "243 Porter Rnch-Woodland Hils Via Tampa - Winnetka Avs");
		addRoute("244", "244 Chatsworth Sta-Woodlnd Hls Via De Soto-Topanga Cyn");
		addRoute("245", "245 Chatsworth Sta-Woodlnd Hls Via De Soto-Topanga Cyn");
		addRoute("246", "246 San Pedro - Artesia Transit Center Via Avalon Bl");
		addRoute("251", "251 Cypress Park- Long Bch Green Line Sta Via Soto St");
		addRoute("252", "252 Lincoln Heights - Boyle Heights Via Soto St");
		addRoute("254", "254 Boyle Hghts - 103rd St Sta Via Lorena St-Boyle Av");
		addRoute("256", "256 Eastern Ave. - Ave. 64 - N. Hill Ave.");
		addRoute("258", "258 Alhambra - Paramount Via Fremont - Eastern Avs");
		addRoute("260", "260 Altadena - Artesia Sta Via Fair Oaks - Atlantic");
		addRoute("264", "264 Pasadena - Altadena - City Of Hope - El Monte Sta");
		addRoute("265", "265 Pico Rivera - Lakewood Ctr Mall Via Paramount Bl");
		addRoute("266", "266 S Madre Villa Sta-Lakewood Ctr Mall Via Rosemead");
		addRoute("267", "267 Pasadena - Altadena - City Of Hope - El Monte Sta");
		addRoute("268", "268 Altadena - El Monte Sta Via Washington - Baldwin");
		addRoute("270", "270 Monrovia - Norwalk Sta Via Workman Mill - Peck Rds");
		addRoute("287", "287 Highland Park - Montebello Via Mission-Tyler-Rush");
		addRoute("290", "290 Sylmar - Sunland Via Foothill Bl");
		addRoute("292", "292 Sylmar Sta-Burbank Sta Via Glenoaks Bl");
		addRoute("302", "302 Downtown LA - Pacific Palisades Via Sunset Bl");
		addRoute("305", "305 Westwood - Leimert Park - South LA - Willowbrook");
		addRoute("311", "311 LAX City Bus Ctr - Norwalk Sta Via Florence Av");
		addRoute("312", "312 Hollywood/Vine Sta - Hawthorne Sta Via La Brea Av");
		addRoute("316", "316 Downtown LA - Century City Via West 3rd St");
		addRoute("344", "344 Artesia Transit Ctr- Palos Verdes Via Hawthorne Bl");
		addRoute("352", "352 Wilshr Ctr-Dtwn LA-Compton-Artesia Tran Via Avalon");
		addRoute("353", "353 Woodland Hills - N Hollywood Sta Via Roscoe Bl");
		addRoute("355", "355 Downtown LA - Imperial/Wilmngtn Sta Via Compton Av");
		addRoute("358", "358 Marina Del Rey - Pico Rivera Via Slauson Av");
		addRoute("363", "363 West Hills -Sun Valley-N Hollywood Via Sherman Way");
		addRoute("364", "364 Chatsworth Sta -Sun Valley Via Nordhoff-Osborne St");
		addRoute("378", "378 Downtown LA - Arcadia Via Las Tunas-Huntington Drs");
		addRoute("439", "439 Downtown LA - Culver City Transit Ctr Via I-10 Fwy");
		addRoute("442", "442 Downtown LA - Hawthorne Station- Via Manchester");
		addRoute("450", "450 Downtown LA - San Pedro Via Harbor Transitway");
		addRoute("460", "460 Downtown LA - Disneyland Via Harbor Tway-105 Fwy");
		addRoute("485", "485 Downtown LA - Altadena Via Fremont - Lake Avs");
		addRoute("487", "487 Downtown LA - S Madre Villa Sta - El Monte Sta");
		addRoute("489", "489 Downtown LA - S Madre Villa Sta - El Monte Sta");
		addRoute("534", "534 Malibu - Washington/Fairfax Via Pacific Coast Hwy");
		addRoute("550", "550 West Hollywood - San Pedro Via Harbor Transitway");
		addRoute("577", "577 El Monte Sta- Long Beach Va Med Ctr Via I-605 Fwy");
		addRoute("603", "603 Glendale-Grand Sta Via San Fernando-Rampart-Hoover");
		addRoute("605", "605 Lac+usc Medical Center - Olympic Bl Shuttle");
		addRoute("607", "607 Windsor Hills - Inglewood Shuttle");
		addRoute("611", "611 Huntington Park Shuttle");
		addRoute("612", "612 South Gate Shuttle");
		addRoute("620", "620 Boyle Heights Shuttle");
		addRoute("625", "625 Metro Green Line Shuttle");
		addRoute("645", "645 West Hills-Warner Ctr Via Valley Cir-Mulholland Dr");
		addRoute("656", "656 Panorama -Van Nuys-Hollywood Via Chandler-Cahuenga");
		addRoute("665", "665 Cal State LA - City Terrace Shuttle");
		addRoute("685", "685 Glendale College - Glassell Park Via Verdugo Rd");
		addRoute("686", "686 Altadena-Pasadena Via Fair Oaks-Los Robles-Allen");
		addRoute("687", "687 Altadena-Pasadena Via Fair Oaks-Los Robles-Allen");
		addRoute("704", "704 Downtown LA - Santa Monica Via Santa Monica Bl");
		addRoute("705", "705 W Hollywood - Vernon Via La Cienega Bl - Vernon Av");
		addRoute("710", "710 Wilshire Ctr - South Bay Galleria Via Crenshaw Bl");
		addRoute("720", "720 Santa Monica-Commerce Via Wilshire - Whittier Bls");
		addRoute("728", "728 Downtown LA - Century City Via West Olympic Bl");
		addRoute("730", "730 Downtown LA - Pico Rimpau Via Pico Bl");
		addRoute("733", "733 Downtown LA - Santa Monica Via Venice Bl");
		addRoute("734", "734 Sylmar Sta - Sherman Oaks Via Sepulveda Bl");
		addRoute("740", "740 Downtown LA - Redondo Beach Via Hawthorne - King");
		addRoute("741", "741 Northridge - Tarzana Via Reseda Bl");
		addRoute("745", "745 Downtown LA - Harbor Freeway Station Via Broadway");
		addRoute("750", "750 Warner Ctr - Universal City Via Ventura Bl");
		addRoute("751", "751 Cypress Park - Huntington Park Via Soto St");
		addRoute("754", "754 Hollywood - Athens Via Vermont Av");
		addRoute("757", "757 Hollywood - Crenshaw Station Via Western Av");
		addRoute("760", "760 Dtwn LA- Long Bch Green Line Sta Via Long Beach Bl");
		addRoute("761", "761 Pacoima-Westwood Via Van Nuys Bl - Sepulveda Bl");
		addRoute("762", "762 Pasadena - Artesia Station Via Atlantic Bl");
		addRoute("770", "770 Downtown LA - El Monte Sta Via Garvey - Chavez Avs");
		addRoute("780", "780 Pasadena-Washtn/Fairfax Via Colorado-Hlywd-Fairfax");
		addRoute("794", "794 Downtown LA - Sylmar Sta Via San Fernando Rd");
		addRoute("901", "901 Metro Orange Line");
		addRoute("910", "910 El Monte Sta - Downtown LA - Artesa Transit Center");
	}

	@Override
	protected int getInitialContentLength() {
		return 522020;
	}

	@Override
	protected void parseAlert(RouteConfig routeConfig)
			throws ClientProtocolException, IOException, SAXException {
		// alerts are not currently supported
		
	}

}
