package boston.Bus.Map.data;

import java.util.HashMap;

public class AlertsMapping {
	private static final String alertsMappingData = "Fairmount,1\n"+
	"Fitchburg/South Acton,2\n"+
	"Framingham/Worcester,4\n"+
	"Franklin/Forge Park,5\n"+
	"Haverhill,7\n"+
	"Lowell,8\n"+
	"Middleborough/Lakeville,9\n"+
	"Needham,10\n"+
	"Newburyport/Rockport,11\n"+
	"Kingston/Plymouth,12\n"+
	"Providence/Stoughton,14\n"+
	"Red Line,15\n"+
	"Orange Line,16\n"+
	"Green Line,17\n"+
	"Blue Line,18\n"+
	"Washington Street,19\n"+
	"SL1: Airport Terminal - South Station,20\n"+
	"F1 - Hingham-Boston,21\n"+
	"F2 - Quincy-Boston_Logan,22\n"+
	"F2H - Quincy and Hull-Boston-Logan,23\n"+
	"F4 - Boston-Charleston,25\n"+
	"SL2: Boston Marine Industrial Park - South Station,28\n"+
	"SL3: City Point - South Station via Boston Marine Industrial Park,29\n"+
	"All Lines/Routes,99\n"+
	"CT2  Sullivan Station - Ruggles Station  via Kendall/MIT and Longwood Medical Center,51\n"+
	"CT3  Beth Israel Deaconess Medical Center - Andrew Station via B.U. Medical Center,52\n"+
	"Silver Line Washington St.    Dudley Station - Downtown via Washington Street ,53\n"+
	"Mattapan Trolley Shuttle Bus    Mattapan Sta. - Ashmont Sta. ,57\n"+
	"1    Harvard/Holyoke Gate - Dudley Staton via Mass. Ave. ,58\n"+
	"4    North Station - World Trade Center via Congress St. ,59\n"+
	"5    City Point -McCormack Housing via Andrew Station ,60\n"+
	"6    South Station - Haymarket Station via North End ,61\n"+
	"7    City Point - Otis and Summer Streets via Summer Street,62\n"+
	"8    Harbor Point/UMass - Kenmore Station   ,63\n"+
	"9    City Point - Copley Square via Broadway Station ,64\n"+
	"10   City Point - Copley Square  ,65\n"+
	"11   City Point - Downtown via Bayview,66\n"+
	"14   Roslindale Sq. - Heath Street Station  ,67\n"+
	"15   Kane Sq. or Fields Corner Sta. - Ruggles Sta. ,68\n"+
	"16   Forest Hills Station - Andrew Station or UMass ,69\n"+
	"17   Fields Corner Station - Andrew Station via Kane Square ,70\n"+
	"18   Ashmont Station - Andrew Station via Dorchester Ave.,71\n"+
	"19   Fields Corner Sta. - Kenmore or Ruggles Sta. via Grove Hall and Dudley Station ,72\n"+
	"21   Ashmont Sta. - Forest Hills Sta.,73\n"+
	"22   Ashmont Sta. - Ruggles Sta. via Talbot Ave.,74\n"+
	"23   Ashmont Sta. - Ruggles Sta. via Washington St. ,75\n"+
	"24   Wakefield Ave.and Truman Parkway - Mattapan or Ashmont Sta.,76\n"+
	"26   Ashmont Sta. - Norfolk and Morton Belt Line ,77\n"+
	"27   Mattapan Sta. - Ashmont Sta.via River St. ,78\n"+
	"28   Mattapan Sta. - Ruggles Sta.via Dudley Sta. ,79\n"+
	"29   Mattapan Sta. - Jackson Sq.  Sta.via Seaver St.,80\n"+
	"30   Mattapan Sta. - Forest Hills Sta.via Cummins Highway and Roslindale Square,81\n"+
	"31   Mattapan Sta. - Forest Hills Sta.via Morton St. ,82\n"+
	"32   Wolcott Sq. or Cleary Square - Forest Hills Sta.,83\n"+
	"33   Dedham Line - Mattapan Sta.via River St. ,84\n"+
	"34   34/34E    Walpole Center or Dedham Line - Forest Hills Sta.via Washington St.,85\n"+
	"34E  34/34E    Walpole Center or Dedham Line - Forest Hills Sta.via Washington St.,86\n"+
	"35   Dedham Mall/Stimson St. - Forest Hills Station,87\n"+
	"36   Charles River Loop or V.A. Hospital - Forest Hills Sta. via Belgrade Ave.,88\n"+
	"37   Baker and Vermont Sts. - Forest Hills Sta.,89\n"+
	"38   Wren St. - Forest Hills Sta.via Centre and South Streets ,90\n"+
	"39   Forest Hills Sta. - Back Bay Sta.via Jamaica Plain Center,91\n"+
	"40   Georgetowne - Forest Hills Sta.,92\n"+
	"41   Centre and Eliot Sts. - JFK/UMass Station ,93\n"+
	"42   Forest Hills Sta. - Dudley or Ruggles Sta.via Washington St.,94\n"+
	"43   Ruggles Station - Park and Tremont Sts.,95\n"+
	"44   Jackson Sq. Sta. - Ruggles Sta.via Seaver St.,96\n"+
	"45   Franklin Park Zoo - Ruggles Sta.,97\n"+
	"47   Central Sq.  Cambridge - Broadway Station via B.U. Medical Center,988\n"+
	"48   Jamaica Plain Loop Monument - Jackson Square Station ,49\n"+
	"50   Cleary Square - Forest Hills Stationvia Roslindale Square,100\n"+
	"51   Reservoir(Cleveland Circle) - Forest Hills Sta. ,101\n"+
	"52   Dedham Mall or Charles River Loop - Watertown Yard,102\n"+
	"55   Jersey and Queensberry - Copley Sq. or Park and Tremont Sts. via Ipswich St.,103\n"+
	"57   Watertown Yard - Kenmore Station via Commonwealth Ave. ,104\n"+
	"59   Needham Junction - Watertown Sq.,105\n"+
	"60   Chestnut Hill - Kenmore Station via Cypress St.,106\n"+
	"62   62/76    Bedford V.A. - Alewife Sta.,107\n"+
	"76   62/76    Hanscom/Lincoln Labs - Alewife Sta.,108\n"+
	"64   Oak Square - University Park  Cambridge or Kendall/MIT,109\n"+
	"65   Brighton Center - Kenmore Sta.via Brookline Ave. ,110\n"+
	"66   Harvard Square - Dudley Station via Harvard St. ,111\n"+
	"67   Turkey Hill - Alewife Station ,112\n"+
	"68   Harvard Sq. - Kendall/M.I.T. via Broadway ,113\n"+
	"69   Harvard Sq. - Lechmere Sta.via Cambridge St.,114\n"+
	"70   70/70A    Cedarwood or Watertown Sq. - University Park,115\n"+
	"70A  70/70A    No. Waltham - University Park ,116\n"+
	"71   Watertown Square - Harvard Station via Mt. Auburn St ,117\n"+
	"72   Huron Ave. - Harvard Station via Concord Ave. ,118\n"+
	"73   Waverley Sq. - Harvard Station via Trapelo Road ,119\n"+
	"74   74/75    Belmont Center - Harvard Station via Concord Ave. ,120\n"+
	"75   74/75    Belmont Center - Harvard Station via Concord Ave. ,121\n"+
	"77   Arlington Heights - Harvard Station via Massachusetts Ave.,122\n"+
	"78   Arlmont Village - Harvard Station via Park Circle ,123\n"+
	"79   Arlington Heights - Alewife Station ,124\n"+
	"80   Arlington Center - Lechmere Station via Medford Hillside,125\n"+
	"83   Rindge Ave. - Central Sq.  Cambridge,126\n"+
	"84   Arlmont Village - Alewife Station ,127\n"+
	"85   Spring Hill - Kendall/M.I.T. Station ,128\n"+
	"86   Sullivan Sq. Sta. - Reservoir (Cleveland Circle) via Harvard Sq.,129\n"+
	"87   Arlington Center or Clarendon Hill - Lechmere Station          ,130\n"+
	"88   Clarendon Hill - Lechmere Station via Highland Avenue ,131\n"+
	"89   Clarendon Hill or Davis Square - Sullivan Square Station via Broadway ,132\n"+
	"90   Davis Square - Wellington Station ,133\n"+
	"91   Sullivan Sq. Sta. - Central Sq.  Cambridge via Washington St.,134\n"+
	"92   Assembly Sq. Mall - Downtown via Main St.  ,135\n"+
	"93   Sullivan Sq. Sta. - Downtown via Bunker Hill St. ,136\n"+
	"94   Medford Square - Davis Sq. Station ,137\n"+
	"95   West Medford - Sullivan Square Sta. ,138\n"+
	"96   Medford Sq. - Harvard Station via George St. ,139\n"+
	"97   Malden Center Sta. - Wellington Sta. ,140\n"+
	"99   Boston Regional Medical Center- Wellington Station,141\n"+
	"100  Elm St. - Wellington Station via Fellsway,142\n"+
	"101  Malden Center Station - Sullivan Square Station ,143\n"+
	"104  Malden Center Station - Sullivan Square Station ,144\n"+
	"105  Malden Center Station - Sullivan Square Station via Newland St. Housing,145\n"+
	"106  Lebanon St.  Malden  - Wellington Sta.,146\n"+
	"108  Linden Square - Wellington Station ,147\n"+
	"109  Linden Square - Sullivan Square Station ,148\n"+
	"110  Wonderland or Broadwayand Park Ave. - Wellington Station,149\n"+
	"111  Woodlawn or Broadway and Park Ave. - Haymarket Station via Mystic River/Tobin Bridge,150\n"+
	"112  Wellington Sta. - Wood Island Sta. via Mystic Mall and Admiral's Hill,151\n"+
	"114  114/116/117    Bellingham Square - Maverick Station ,152\n"+
	"116  114/116/117    Wonderland Station - Maverick Station via Revere Street ,153\n"+
	"117  114/116/117    Wonderland Station - Maverick Station via Beach St. ,154\n"+
	"119  Northgate - Beachmont Station,155\n"+
	"120  Orient Heights Station - Maverick Station via Bennington St.,156\n"+
	"121  Wood Island Station - Maverick Station via Lexington St.,157\n"+
	"131  Melrose Highlands - Malden Center Station,158\n"+
	"132  Redstone Shopping Center - Malden Center Station ,159\n"+
	"134  North Woburn - Wellington Station via Woburn Sq. and Winchester Ctr.,160\n"+
	"136  Reading Depot - Malden Station,161\n"+
	"137  Reading Depot - Malden Station ,162\n"+
	"170  Oak Park - Dudley Square,163\n"+
	"171  Dudley Station - Logan Airport via Andrew Station ,164\n"+
	"201  Fields Corner or No. Quincy Station - Fields Corner via Neponset Ave. to Adams St.,165\n"+
	"202  Fields Corner or No. Quincy Station - Fields Corner via Adams St. to Neponset Ave.,166\n"+
	"210  Quincy Center Sta. - No. Quincy Sta. or Fields Corner Station,167\n"+
	"211  Quincy Center Sta. - Squantum,168\n"+
	"212  Quincy Center Sta. - North Quincy Sta. ,169\n"+
	"214  Quincy Center Sta. - Germantown via Sea St.,170\n"+
	"215  Quincy Center Sta. - Ashmont Sta. via West Quincy,171\n"+
	"216  Quincy Center Sta. - Houghs Neck via Sea St.,172\n"+
	"217  Quincy Center Sta. - Ashmont Station via Wollaston Beach ,173\n"+
	"220  Quincy Center Sta. - Hingham ,174\n"+
	"221  Quincy Center Sta. - Fort Point ,175\n"+
	"222  Quincy Center Sta. - East Weymouth ,176\n"+
	"225  Quincy Center Sta. - Weymouth Landing via Quincy Ave.,177\n"+
	"230  Quincy Center Sta. - Montello Commuter Rail Station via Holbrook  ,178\n"+
	"236  Quincy Center Sta. - South Shore Plaza,179\n"+
	"238  Quincy Center Sta.  - Holbrook/Randolph Commuter Rail Station via Carwford Square,180\n"+
	"240  Avon Square or Holbrook/Randolph Commuter Rail Sta. - Ashmont Sta. via Randolph Ave and Crawford Sq.,181\n"+
	"245  Quincy Center Sta. - Mattapan Sta. via Pleasant St.,182\n"+
	"275  275/276/277    EXPRESS BUS Boston Medical Center or Downtown Boston - Long Island via Expressway,183\n"+
	"276  275/276/277    EXPRESS BUS Boston Medical Center or Downtown Boston - Long Island via Expressway,184\n"+
	"277  275/276/277    EXPRESS BUS Boston Medical Center or Downtown Boston - Long Island ,185\n"+
	"325  Elm St.  Medford - Haymarket Station via I-93,186\n"+
	"326  West Medford - Haymarket Station via I-93 ,187\n"+
	"350  North Burlington - Alewife Station,188\n"+
	"351  Oak Park/Bedford Woods - Alewife Station,189\n"+
	"352  Burlington - Boston Express Bus via Rte. 128 and I-93 ,190\n"+
	"354  Woburn Express - Boston via I-93 ,191\n"+
	"355  Mishawum Station - Boston via I-93 ,192\n"+
	"411  Malden Center Station - Revere/Jack Satter House via Northgate Shopping Ctr.,193\n"+
	"424  424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,194\n"+
	"424W 424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland  ,195\n"+
	"450  424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,196\n"+
	"450W 424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,197\n"+
	"456  424/424W/450/450W/456    Salem Depot - Haymarket Sta. or Wonderland,198\n"+
	"426  426/426W/428    Central Sq.  Lynn - Haymarket Sta. via Cliftondale Square,199\n"+
	"426W 426/426W/428    Central Sq.  Lynn - Haymarket Sta. via Cliftondale Square,200\n"+
	"428  426/426W/428    Oaklandvale - Haymarket Sta. via Granada Highlands,201\n"+
	"429  Northgate Shopping Center - Central Sq.  Lynn via Myrtle St. King's Lynne Saugus Plaza Linden Square and Square One Mall,202\n"+
	"430  Saugus Center - Malden Center Station via Cliftondale Sq.,203\n"+
	"431  431/434/435/436    Neptune Towers - Central Sq.  Lynn via Summer St.,204\n"+
	"434  431/434/435/436    Peabody - Haymarket Express via Goodwins Circle ,205\n"+
	"435  431/434/435/436    Liberty Tree Mall - Central Sq.  Lynn via peabody Sq.,206\n"+
	"436  431/434/435/436    Liberty Tree Mall - Central Sq.  Lynn via Goodwins Circle,207\n"+
	"439  Bass Point  Nahant -Central Sq.  Lynn,208\n"+
	"441  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,209\n"+
	"441W 441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,210\n"+
	"442  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,211\n"+
	"442W 441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,212\n"+
	"449  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,213\n"+
	"448  441/441W/448 442/442W/449    Marblehead - Haymarket  Downtown Crossing or Wonderland,214\n"+
	"451  451/465/468    North Beverly - Salem Depot via Cabot St. or Tozer Rd.,215\n"+
	"465  451/465/468    Liberty Tree Mall - Salem Depot via Peabody and Danvers,216\n"+
	"468  451/465/468    Danvers Sq. - Salem Depot via Peabody ,217\n"+
	"455  455/455W/459    Salem Depot - Wonderland via Central Sq. Lynn,218\n"+
	"455W 455/455W/459    Salem Depot - Haymarket via Central Sq. Lynn,219\n"+
	"459  455/455W/459    Salem Depot - Downtown via Logan Airport ,220\n"+
	"500  EXPRESS BUS Riverside - Downtown Boston via Mass Pike,221\n"+
	"501  EXPRESS BUS Brighton Center - Downtown Boston via Mass Pike,222\n"+
	"502  EXPRESS BUS Watertown - Copley Sq. via Mass Pike ,223\n"+
	"503  EXPRESS BUS Brighton Center - Copley Sq. via Mass Pike,224\n"+
	"504  EXPRESS BUS Watertown - Downtown Boston via Mass Pike  Waltham - Downtown Boston via Moody St.,225\n"+
	"505  EXPRESS BUS Central Sq.,226\n"+
	"553  553/554    Roberts - Downtown Boston via Newton Corner and Mass Pike ,227\n"+
	"554  553/554    Waverley Sq. - Downtown Boston via Newton Corner and Mass Pike ,228\n"+
	"556  556/558    Waltham Highlands - Downtown Boston via Newton Corner and Mass Pike,229\n"+
	"558  556/558    Riverside - Downtown Boston via Newton Corner and Mass Pike,230\n"+
	"CT1 Central Square, Cambridge - B.U. Medical Center/Boston Medical Centger via MIT,50\n"+
	"Greenbush,232\n"+
	"Red Line - Mattapan Line,233\n";
	public static final String alertUrlPrefix = "http://talerts.com/rssfeed/alertsrss.aspx?";

	private final HashMap<String, Integer> routeDescriptionToAlertKey = new HashMap<String, Integer>();
	
	public AlertsMapping()
	{
		String[] lines = alertsMappingData.split("\n");
		for (String line : lines)
		{
			line = line.trim();
			
			String[] fields = line.split(",");
			int alertKey = Integer.parseInt(fields[fields.length - 1]);
			String routeDescription = fields[0];
			
			routeDescriptionToAlertKey.put(routeDescription, alertKey);
		}
	}

	public HashMap<String, Integer> getAlertNumbers(String[] routeNames, HashMap<String, String> routeKeysToTitles)
	{
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		
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
					if (routeDescription.startsWith(routeName + " "))
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

		return ret;
	}

	private void addToList(String routeTitle, int alertIndex, HashMap<String, String> routeKeysToTitles,
			HashMap<String, Integer> alertsMapping) {
		for (String routeKey : routeKeysToTitles.values())
		{
			String potentialRouteTitle = routeKeysToTitles.get(routeKey);
			if (potentialRouteTitle.equals(routeTitle))
			{
				alertsMapping.put(routeKey, alertIndex);
				return;
			}
		}
	}
	
	
}
