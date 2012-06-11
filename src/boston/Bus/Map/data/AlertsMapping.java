package boston.Bus.Map.data;



public class AlertsMapping {
	public static final String alertUrlPrefix = "http://talerts.com/rssfeed/alertsrss.aspx?";

	private final MyHashMap<String, Integer> routeDescriptionToAlertKey = new MyHashMap<String, Integer>();
	
	public AlertsMapping()
	{
		parseLine("Fairmount,1");
		parseLine("Fitchburg/South Acton,2");
		parseLine("Framingham/Worcester,4\n");
		parseLine("Franklin/Forge Park,5\n");
		parseLine("Haverhill,7\n");
		parseLine("Lowell,8\n");
		parseLine("Middleborough/Lakeville,9\n");
		parseLine("Needham,10\n");
		parseLine("Newburyport/Rockport,11\n");
		parseLine("Kingston/Plymouth,12\n");
		parseLine("Providence/Stoughton,14\n");
		parseLine("Red Line,15\n");
		parseLine("Orange Line,16\n");
		parseLine("Green Line,17\n");
		parseLine("Blue Line,18\n");
		parseLine("Washington Street,19\n");
		parseLine("SL1: Airport Terminal - South Station,20\n");
		parseLine("F1 - Hingham-Boston,21\n");
		parseLine("F2 - Quincy-Boston_Logan,22\n");
		parseLine("F2H - Quincy and Hull-Boston-Logan,23\n");
		parseLine("F4 - Boston-Charleston,25\n");
		parseLine("SL2: Boston Marine Industrial Park - South Station,28\n");
		parseLine("SL3: City Point - South Station via Boston Marine Industrial Park,29\n");
		parseLine("All Lines/Routes,99\n");
		parseLine("CT2  Sullivan Station - Ruggles Station  via Kendall/MIT and Longwood Medical Center,51\n");
		parseLine("CT3  Beth Israel Deaconess Medical Center - Andrew Station via B.U. Medical Center,52\n");
		parseLine("Silver Line Washington St.    Dudley Station - Downtown via Washington Street ,53\n");
		parseLine("Mattapan Trolley Shuttle Bus    Mattapan Sta. - Ashmont Sta. ,57\n");
		parseLine("1    Harvard/Holyoke Gate - Dudley Staton via Mass. Ave. ,58\n");
		parseLine("4    North Station - World Trade Center via Congress St. ,59\n");
		parseLine("5    City Point -McCormack Housing via Andrew Station ,60\n");
		parseLine("6    South Station - Haymarket Station via North End ,61\n");
		parseLine("7    City Point - Otis and Summer Streets via Summer Street,62\n");
		parseLine("8    Harbor Point/UMass - Kenmore Station   ,63\n");
		parseLine("9    City Point - Copley Square via Broadway Station ,64\n");
		parseLine("10   City Point - Copley Square  ,65\n");
		parseLine("11   City Point - Downtown via Bayview,66\n");
		parseLine("14   Roslindale Sq. - Heath Street Station  ,67\n");
		parseLine("15   Kane Sq. or Fields Corner Sta. - Ruggles Sta. ,68\n");
		parseLine("16   Forest Hills Station - Andrew Station or UMass ,69\n");
		parseLine("17   Fields Corner Station - Andrew Station via Kane Square ,70\n");
		parseLine("18   Ashmont Station - Andrew Station via Dorchester Ave.,71\n");
		parseLine("19   Fields Corner Sta. - Kenmore or Ruggles Sta. via Grove Hall and Dudley Station ,72\n");
		parseLine("21   Ashmont Sta. - Forest Hills Sta.,73\n");
		parseLine("22   Ashmont Sta. - Ruggles Sta. via Talbot Ave.,74\n");
		parseLine("23   Ashmont Sta. - Ruggles Sta. via Washington St. ,75\n");
		parseLine("24   Wakefield Ave.and Truman Parkway - Mattapan or Ashmont Sta.,76\n");
		parseLine("26   Ashmont Sta. - Norfolk and Morton Belt Line ,77\n");
		parseLine("27   Mattapan Sta. - Ashmont Sta.via River St. ,78\n");
		parseLine("28   Mattapan Sta. - Ruggles Sta.via Dudley Sta. ,79\n");
		parseLine("29   Mattapan Sta. - Jackson Sq.  Sta.via Seaver St.,80\n");
		parseLine("30   Mattapan Sta. - Forest Hills Sta.via Cummins Highway and Roslindale Square,81\n");
		parseLine("31   Mattapan Sta. - Forest Hills Sta.via Morton St. ,82\n");
		parseLine("32   Wolcott Sq. or Cleary Square - Forest Hills Sta.,83\n");
		parseLine("33   Dedham Line - Mattapan Sta.via River St. ,84\n");
		parseLine("34   34/34E    Walpole Center or Dedham Line - Forest Hills Sta.via Washington St.,85\n");
		parseLine("34E  34/34E    Walpole Center or Dedham Line - Forest Hills Sta.via Washington St.,86\n");
		parseLine("35   Dedham Mall/Stimson St. - Forest Hills Station,87\n");
		parseLine("36   Charles River Loop or V.A. Hospital - Forest Hills Sta. via Belgrade Ave.,88\n");
		parseLine("37   Baker and Vermont Sts. - Forest Hills Sta.,89\n");
		parseLine("38   Wren St. - Forest Hills Sta.via Centre and South Streets ,90\n");
		parseLine("39   Forest Hills Sta. - Back Bay Sta.via Jamaica Plain Center,91\n");
		parseLine("40   Georgetowne - Forest Hills Sta.,92\n");
		parseLine("41   Centre and Eliot Sts. - JFK/UMass Station ,93\n");
		parseLine("42   Forest Hills Sta. - Dudley or Ruggles Sta.via Washington St.,94\n");
		parseLine("43   Ruggles Station - Park and Tremont Sts.,95\n");
		parseLine("44   Jackson Sq. Sta. - Ruggles Sta.via Seaver St.,96\n");
		parseLine("45   Franklin Park Zoo - Ruggles Sta.,97\n");
		parseLine("47   Central Sq.  Cambridge - Broadway Station via B.U. Medical Center,988\n");
		parseLine("48   Jamaica Plain Loop Monument - Jackson Square Station ,49\n");
		parseLine("50   Cleary Square - Forest Hills Stationvia Roslindale Square,100\n");
		parseLine("51   Reservoir(Cleveland Circle) - Forest Hills Sta. ,101\n");
		parseLine("52   Dedham Mall or Charles River Loop - Watertown Yard,102\n");
		parseLine("55   Jersey and Queensberry - Copley Sq. or Park and Tremont Sts. via Ipswich St.,103\n");
		parseLine("57   Watertown Yard - Kenmore Station via Commonwealth Ave. ,104\n");
		parseLine("59   Needham Junction - Watertown Sq.,105\n");
		parseLine("60   Chestnut Hill - Kenmore Station via Cypress St.,106\n");
		parseLine("62   62/76    Bedford V.A. - Alewife Sta.,107\n");
		parseLine("76   62/76    Hanscom/Lincoln Labs - Alewife Sta.,108\n");
		parseLine("64   Oak Square - University Park  Cambridge or Kendall/MIT,109\n");
		parseLine("65   Brighton Center - Kenmore Sta.via Brookline Ave. ,110\n");
		parseLine("66   Harvard Square - Dudley Station via Harvard St. ,111\n");
		parseLine("67   Turkey Hill - Alewife Station ,112\n");
		parseLine("68   Harvard Sq. - Kendall/M.I.T. via Broadway ,113\n");
		parseLine("69   Harvard Sq. - Lechmere Sta.via Cambridge St.,114\n");
		parseLine("70   70/70A    Cedarwood or Watertown Sq. - University Park,115\n");
		parseLine("70A  70/70A    No. Waltham - University Park ,116\n");
		parseLine("71   Watertown Square - Harvard Station via Mt. Auburn St ,117\n");
		parseLine("72   Huron Ave. - Harvard Station via Concord Ave. ,118\n");
		parseLine("73   Waverley Sq. - Harvard Station via Trapelo Road ,119\n");
		parseLine("74   74/75    Belmont Center - Harvard Station via Concord Ave. ,120\n");
		parseLine("75   74/75    Belmont Center - Harvard Station via Concord Ave. ,121\n");
		parseLine("77   Arlington Heights - Harvard Station via Massachusetts Ave.,122\n");
		parseLine("78   Arlmont Village - Harvard Station via Park Circle ,123\n");
		parseLine("79   Arlington Heights - Alewife Station ,124\n");
		parseLine("80   Arlington Center - Lechmere Station via Medford Hillside,125\n");
		parseLine("83   Rindge Ave. - Central Sq.  Cambridge,126\n");
		parseLine("84   Arlmont Village - Alewife Station ,127\n");
		parseLine("85   Spring Hill - Kendall/M.I.T. Station ,128\n");
		parseLine("86   Sullivan Sq. Sta. - Reservoir (Cleveland Circle) via Harvard Sq.,129\n");
		parseLine("87   Arlington Center or Clarendon Hill - Lechmere Station          ,130\n");
		parseLine("88   Clarendon Hill - Lechmere Station via Highland Avenue ,131\n");
		parseLine("89   Clarendon Hill or Davis Square - Sullivan Square Station via Broadway ,132\n");
		parseLine("90   Davis Square - Wellington Station ,133\n");
		parseLine("91   Sullivan Sq. Sta. - Central Sq.  Cambridge via Washington St.,134\n");
		parseLine("92   Assembly Sq. Mall - Downtown via Main St.  ,135\n");
		parseLine("93   Sullivan Sq. Sta. - Downtown via Bunker Hill St. ,136\n");
		parseLine("94   Medford Square - Davis Sq. Station ,137\n");
		parseLine("95   West Medford - Sullivan Square Sta. ,138\n");
		parseLine("96   Medford Sq. - Harvard Station via George St. ,139\n");
		parseLine("97   Malden Center Sta. - Wellington Sta. ,140\n");
		parseLine("99   Boston Regional Medical Center- Wellington Station,141\n");
		parseLine("100  Elm St. - Wellington Station via Fellsway,142\n");
		parseLine("101  Malden Center Station - Sullivan Square Station ,143\n");
		parseLine("104  Malden Center Station - Sullivan Square Station ,144\n");
		parseLine("105  Malden Center Station - Sullivan Square Station via Newland St. Housing,145\n");
		parseLine("106  Lebanon St.  Malden  - Wellington Sta.,146\n");
		parseLine("108  Linden Square - Wellington Station ,147\n");
		parseLine("109  Linden Square - Sullivan Square Station ,148\n");
		parseLine("110  Wonderland or Broadwayand Park Ave. - Wellington Station,149\n");
		parseLine("111  Woodlawn or Broadway and Park Ave. - Haymarket Station via Mystic River/Tobin Bridge,150\n");
		parseLine("112  Wellington Sta. - Wood Island Sta. via Mystic Mall and Admiral's Hill,151\n");
		parseLine("114  114/116/117    Bellingham Square - Maverick Station ,152\n");
		parseLine("116  114/116/117    Wonderland Station - Maverick Station via Revere Street ,153\n");
		parseLine("117  114/116/117    Wonderland Station - Maverick Station via Beach St. ,154\n");
		parseLine("119  Northgate - Beachmont Station,155\n");
		parseLine("120  Orient Heights Station - Maverick Station via Bennington St.,156\n");
		parseLine("121  Wood Island Station - Maverick Station via Lexington St.,157\n");
		parseLine("131  Melrose Highlands - Malden Center Station,158\n");
		parseLine("132  Redstone Shopping Center - Malden Center Station ,159\n");
		parseLine("134  North Woburn - Wellington Station via Woburn Sq. and Winchester Ctr.,160\n");
		parseLine("136  Reading Depot - Malden Station,161\n");
		parseLine("137  Reading Depot - Malden Station ,162\n");
		parseLine("170  Oak Park - Dudley Square,163\n");
		parseLine("171  Dudley Station - Logan Airport via Andrew Station ,164\n");
		parseLine("201  Fields Corner or No. Quincy Station - Fields Corner via Neponset Ave. to Adams St.,165\n");
		parseLine("202  Fields Corner or No. Quincy Station - Fields Corner via Adams St. to Neponset Ave.,166\n");
		parseLine("210  Quincy Center Sta. - No. Quincy Sta. or Fields Corner Station,167\n");
		parseLine("211  Quincy Center Sta. - Squantum,168\n");
		parseLine("212  Quincy Center Sta. - North Quincy Sta. ,169\n");
		parseLine("214  Quincy Center Sta. - Germantown via Sea St.,170\n");
		parseLine("215  Quincy Center Sta. - Ashmont Sta. via West Quincy,171\n");
		parseLine("216  Quincy Center Sta. - Houghs Neck via Sea St.,172\n");
		parseLine("217  Quincy Center Sta. - Ashmont Station via Wollaston Beach ,173\n");
		parseLine("220  Quincy Center Sta. - Hingham ,174\n");
		parseLine("221  Quincy Center Sta. - Fort Point ,175\n");
		parseLine("222  Quincy Center Sta. - East Weymouth ,176\n");
		parseLine("225  Quincy Center Sta. - Weymouth Landing via Quincy Ave.,177\n");
		parseLine("230  Quincy Center Sta. - Montello Commuter Rail Station via Holbrook  ,178\n");
		parseLine("236  Quincy Center Sta. - South Shore Plaza,179\n");
		parseLine("238  Quincy Center Sta.  - Holbrook/Randolph Commuter Rail Station via Carwford Square,180\n");
		parseLine("240  Avon Square or Holbrook/Randolph Commuter Rail Sta. - Ashmont Sta. via Randolph Ave and Crawford Sq.,181\n");
		parseLine("245  Quincy Center Sta. - Mattapan Sta. via Pleasant St.,182\n");
		parseLine("275  275/276/277    EXPRESS BUS Boston Medical Center or Downtown Boston - Long Island via Expressway,183\n");
		parseLine("276  275/276/277    EXPRESS BUS Boston Medical Center or Downtown Boston - Long Island via Expressway,184\n");
		parseLine("277  275/276/277    EXPRESS BUS Boston Medical Center or Downtown Boston - Long Island ,185\n");
		parseLine("325  Elm St.  Medford - Haymarket Station via I-93,186\n");
		parseLine("326  West Medford - Haymarket Station via I-93 ,187\n");
		parseLine("350  North Burlington - Alewife Station,188\n");
		parseLine("351  Oak Park/Bedford Woods - Alewife Station,189\n");
		parseLine("352  Burlington - Boston Express Bus via Rte. 128 and I-93 ,190\n");
		parseLine("354  Woburn Express - Boston via I-93 ,191\n");
		parseLine("355  Mishawum Station - Boston via I-93 ,192\n");
		parseLine("411  Malden Center Station - Revere/Jack Satter House via Northgate Shopping Ctr.,193\n");
		parseLine("424  424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,194\n");
		parseLine("424W 424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland  ,195\n");
		parseLine("450  424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,196\n");
		parseLine("450W 424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,197\n");
		parseLine("456  424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,198\n");
		parseLine("426  426/426W/428    Central Sq.  Lynn - Haymarket Sta. via Cliftondale Square,199\n");
		parseLine("426W 426/426W/428    Central Sq.  Lynn - Haymarket Sta. via Cliftondale Square,200\n");
		parseLine("428  426/426W/428    Oaklandvale - Haymarket Sta. via Granada Highlands,201\n");
		parseLine("429  Northgate Shopping Center - Central Sq.  Lynn via Myrtle St. King's Lynne Saugus Plaza Linden Square and Square One Mall,202\n");
		parseLine("430  Saugus Center - Malden Center Station via Cliftondale Sq.,203\n");
		parseLine("431  431/434/435/436    Neptune Towers - Central Sq.  Lynn via Summer St.,204\n");
		parseLine("434  431/434/435/436    Peabody - Haymarket Express via Goodwins Circle ,205\n");
		parseLine("435  431/434/435/436    Liberty Tree Mall - Central Sq.  Lynn via peabody Sq.,206\n");
		parseLine("436  431/434/435/436    Liberty Tree Mall - Central Sq.  Lynn via Goodwins Circle,207\n");
		parseLine("439  Bass Point  Nahant -Central Sq.  Lynn,208\n");
		parseLine("441  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,209\n");
		parseLine("441W 441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,210\n");
		parseLine("442  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,211\n");
		parseLine("442W 441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,212\n");
		parseLine("449  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,213\n");
		parseLine("448  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,214\n");
		parseLine("451  451/465/468    North Beverly - Salem Depot via Cabot St. or Tozer Rd.,215\n");
		parseLine("465  451/465/468    Liberty Tree Mall - Salem Depot via Peabody and Danvers,216\n");
		parseLine("468  451/465/468    Danvers Sq. - Salem Depot via Peabody ,217\n");
		parseLine("455  455/455W/459    Salem Depot - Wonderland via Central Sq. Lynn,218\n");
		parseLine("455W 455/455W/459    Salem Depot - Haymarket via Central Sq. Lynn,219\n");
		parseLine("459  455/455W/459    Salem Depot - Downtown via Logan Airport ,220\n");
		parseLine("500  EXPRESS BUS Riverside - Downtown Boston via Mass Pike,221\n");
		parseLine("501  EXPRESS BUS Brighton Center - Downtown Boston via Mass Pike,222\n");
		parseLine("502  EXPRESS BUS Watertown - Copley Sq. via Mass Pike ,223\n");
		parseLine("503  EXPRESS BUS Brighton Center - Copley Sq. via Mass Pike,224\n");
		parseLine("504  EXPRESS BUS Watertown - Downtown Boston via Mass Pike  Waltham - Downtown Boston via Moody St.,225\n");
		parseLine("505  EXPRESS BUS Central Sq.,226\n");
		parseLine("553  553/554    Roberts - Downtown Boston via Newton Corner and Mass Pike ,227\n");
		parseLine("554  553/554    Waverley Sq. - Downtown Boston via Newton Corner and Mass Pike ,228\n");
		parseLine("556  556/558    Waltham Highlands - Downtown Boston via Newton Corner and Mass Pike,229\n");
		parseLine("558  556/558    Riverside - Downtown Boston via Newton Corner and Mass Pike,230\n");
		parseLine("CT1 Central Square, Cambridge - B.U. Medical Center/Boston Medical Centger via MIT,50\n");
		parseLine("Greenbush,232\n");
		parseLine("Red Line - Mattapan Line,233\n");
	}
	
	private void parseLine(String line) {
		line = line.trim();

		String[] fields = line.split(",");
		int alertKey = Integer.parseInt(fields[fields.length - 1]);
		String routeDescription = fields[0];

		routeDescriptionToAlertKey.put(routeDescription, alertKey);
	}

	public MyHashMap<String, Integer> getAlertNumbers(String[] routeNames, MyHashMap<String, String> routeKeysToTitles)
	{
		MyHashMap<String, Integer> ret = new MyHashMap<String, Integer>();
		
		for (String routeName : routeNames)
		{
			for (String routeDescription : routeDescriptionToAlertKey.keySet())
			{
				if (routeDescription.equals(routeName))
				{
					int value = routeDescriptionToAlertKey.get(routeDescription);
					ret.put(routeName, value);
					break;
				}
			}

			//try startwith
			if (ret.containsKey(routeName) == false)
			{
				for (String routeDescription : routeDescriptionToAlertKey.keySet())
				{
					if (routeDescription.startsWith(routeName + " ") ||
							routeDescription.startsWith(routeName + "/"))
					{
						int value = routeDescriptionToAlertKey.get(routeDescription);
						ret.put(routeName, value);
						break;
					}
				}
			}
		}
		
		//special cases
		addToList("CT1", 50, routeKeysToTitles, ret);
		addToList("CT2", 51, routeKeysToTitles, ret);
		addToList("CT3", 52, routeKeysToTitles, ret);

		addToList("Silver Line SL1", 20, routeKeysToTitles, ret);
		addToList("Silver Line SL2", 28, routeKeysToTitles, ret);
		addToList("Silver Line SL4", 53, routeKeysToTitles, ret);
		//which alert index number is SL5?
		//addToList("Silver Line SL5", 50, routeKeysToTitles, ret);

		ret.put("Red", 15);
		ret.put("Orange", 16);
		ret.put("Blue", 18);

		return ret;
	}

	private void addToList(String routeTitle, int alertIndex, MyHashMap<String, String> routeKeysToTitles,
			MyHashMap<String, Integer> alertsMapping) {
		for (String routeKey : routeKeysToTitles.values())
		{
			String potentialRouteTitle = routeKeysToTitles.get(routeKey);
			if (routeTitle.equals(potentialRouteTitle))
			{
				alertsMapping.put(routeKey, alertIndex);
				return;
			}
		}
	}
	
	
}
