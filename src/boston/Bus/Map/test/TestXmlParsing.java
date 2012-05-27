package boston.Bus.Map.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.TransitDrawables;
import boston.Bus.Map.database.DatabaseHelper;
import boston.Bus.Map.parser.VehicleLocationsFeedParser;

import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Xml.Encoding;

public class TestXmlParsing extends TestCase
{
	private static final String inputData = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> "+
	"<body copyright=\"All data copyright MBTA 2011.\"> "+
	"<vehicle id=\"0808\" routeTag=\"240\" dirTag=\"215_2150006v0_0\" tripTag=\"14608628\" lat=\"42.2841638\" lon=\"-71.0639857\" secsSinceReport=\"29\" predictable=\"true\" heading=\"324\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2127\" routeTag=\"22\" dirTag=\"22_220024v0_1\" tripTag=\"14441568\" lat=\"42.2917102\" lon=\"-71.0769895\" secsSinceReport=\"29\" predictable=\"true\" heading=\"291\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0867\" routeTag=\"435\" dirTag=\"435_4350012v0_0\" tripTag=\"14755129\" lat=\"42.5497086\" lon=\"-70.9317574\" secsSinceReport=\"59\" predictable=\"true\" heading=\"258\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0731\" routeTag=\"86\" dirTag=\"86_860024v0_1\" tripTag=\"14700930\" lat=\"42.3406024\" lon=\"-71.155212\" secsSinceReport=\"59\" predictable=\"true\" heading=\"205\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2009\" routeTag=\"34E\" dirTag=\"34E_340080v0_1\" tripTag=\"14620202\" lat=\"42.2570898\" lon=\"-71.1620257\" secsSinceReport=\"44\" predictable=\"true\" heading=\"43\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0681\" routeTag=\"77\" dirTag=\"77_770018v0_0\" tripTag=\"14700436\" lat=\"42.4049427\" lon=\"-71.1411075\" secsSinceReport=\"44\" predictable=\"true\" heading=\"317\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0847\" routeTag=\"435\" dirTag=\"435_4350012v0_0\" lat=\"42.4619933\" lon=\"-70.9459575\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2296\" routeTag=\"57\" dirTag=\"57_570003v0_0\" lat=\"42.3481437\" lon=\"-71.0992965\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0699\" routeTag=\"62\" dirTag=\"62_620019v0_0\" tripTag=\"14700400\" lat=\"42.4719931\" lon=\"-71.2462946\" secsSinceReport=\"194\" predictable=\"true\" heading=\"329\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0639\" routeTag=\"83\" dirTag=\"83_830004v0_0\" tripTag=\"14701928\" lat=\"42.3930739\" lon=\"-71.1293567\" secsSinceReport=\"29\" predictable=\"true\" heading=\"288\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2131\" routeTag=\"47\" dirTag=\"47_470012v0_1\" lat=\"42.3611734\" lon=\"-71.1064682\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2092\" routeTag=\"34\" dirTag=\"34_340093v0_0\" lat=\"42.2891102\" lon=\"-71.1253735\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0752\" routeTag=\"94\" dirTag=\"94_940003v0_0\" tripTag=\"14580683\" lat=\"42.4178142\" lon=\"-71.1311965\" secsSinceReport=\"29\" predictable=\"true\" heading=\"312\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0761\" routeTag=\"238\" dirTag=\"238_2380010v0_0\" tripTag=\"14608092\" lat=\"42.2353936\" lon=\"-71.028566\" secsSinceReport=\"44\" predictable=\"true\" heading=\"185\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0641\" routeTag=\"70\" dirTag=\"70_700004v0_1\" tripTag=\"14700311\" lat=\"42.3631225\" lon=\"-71.1318737\" secsSinceReport=\"44\" predictable=\"true\" heading=\"102\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0551\" routeTag=\"60\" dirTag=\"60_600017v0_0\" tripTag=\"14418330\" lat=\"42.3236618\" lon=\"-71.1634529\" secsSinceReport=\"44\" predictable=\"true\" heading=\"252\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0830\" routeTag=\"15\" dirTag=\"15_150024v0_1\" tripTag=\"14440512\" lat=\"42.2989816\" lon=\"-71.0637645\" secsSinceReport=\"59\" predictable=\"true\" heading=\"196\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2160\" routeTag=\"11\" dirTag=\"11_110015v0_0\" tripTag=\"14441378\" lat=\"42.3314666\" lon=\"-71.0354163\" secsSinceReport=\"104\" predictable=\"true\" heading=\"97\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0673\" routeTag=\"87\" dirTag=\"87_870016v0_0\" tripTag=\"14701111\" lat=\"42.3932567\" lon=\"-71.1205103\" secsSinceReport=\"59\" predictable=\"true\" heading=\"341\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2003\" routeTag=\"41\" dirTag=\"41_410011v0_0\" tripTag=\"14619261\" lat=\"42.3205781\" lon=\"-71.0571949\" secsSinceReport=\"14\" predictable=\"true\" heading=\"234\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2079\" routeTag=\"35\" dirTag=\"35_350015v0_1\" tripTag=\"14619587\" lat=\"42.2865399\" lon=\"-71.1540128\" secsSinceReport=\"29\" predictable=\"true\" heading=\"46\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0650\" routeTag=\"112\" dirTag=\"112_1120033v0_0\" tripTag=\"14581890\" lat=\"42.3914679\" lon=\"-71.0472439\" secsSinceReport=\"29\" predictable=\"true\" heading=\"343\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2103\" routeTag=\"42\" dirTag=\"42_420003v0_1\" tripTag=\"14619442\" lat=\"42.3166265\" lon=\"-71.0974063\" secsSinceReport=\"14\" predictable=\"true\" heading=\"36\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2149\" routeTag=\"65\" dirTag=\"65_650010v0_0\" tripTag=\"14440795\" lat=\"42.3489344\" lon=\"-71.0957384\" secsSinceReport=\"14\" predictable=\"true\" heading=\"79\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2278\" routeTag=\"11\" dirTag=\"11_110004v0_1\" tripTag=\"14441359\" lat=\"42.3422594\" lon=\"-71.0572345\" secsSinceReport=\"29\" predictable=\"true\" heading=\"179\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0661\" routeTag=\"77\" dirTag=\"77_770018v0_0\" tripTag=\"14700448\" lat=\"42.3890987\" lon=\"-71.1196732\" secsSinceReport=\"29\" predictable=\"true\" heading=\"342\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0275\" routeTag=\"274\" dirTag=\"274_2740005v0_0\" lat=\"42.2601317\" lon=\"-71.0099695\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0812\" routeTag=\"16\" dirTag=\"16_160013v0_0\" tripTag=\"14440586\" lat=\"42.3035464\" lon=\"-71.0847663\" secsSinceReport=\"14\" predictable=\"true\" heading=\"268\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2137\" routeTag=\"8\" dirTag=\"8_80005v0_1\" tripTag=\"14440387\" lat=\"42.3311481\" lon=\"-71.0683264\" secsSinceReport=\"29\" predictable=\"true\" heading=\"19\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2018\" routeTag=\"36\" dirTag=\"36_360006v0_1\" lat=\"42.2753357\" lon=\"-71.1676978\" secsSinceReport=\"119\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2108\" routeTag=\"39\" dirTag=\"39_390003v0_1\" lat=\"42.3020155\" lon=\"-71.1120792\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0625\" routeTag=\"77\" dirTag=\"77_770004v0_1\" tripTag=\"14700485\" lat=\"42.419312\" lon=\"-71.1679019\" secsSinceReport=\"29\" predictable=\"true\" heading=\"125\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1016\" routeTag=\"749\" dirTag=\"749_7490008v0_0\" tripTag=\"14658673\" lat=\"42.332377\" lon=\"-71.0813623\" secsSinceReport=\"44\" predictable=\"true\" heading=\"194\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0501\" routeTag=\"57\" dirTag=\"57_570002v0_1\" tripTag=\"14418093\" lat=\"42.3505593\" lon=\"-71.1678034\" secsSinceReport=\"29\" predictable=\"true\" heading=\"113\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2268\" routeTag=\"7\" dirTag=\"7_70008v0_1\" lat=\"42.3385822\" lon=\"-71.0319001\" secsSinceReport=\"44\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0865\" routeTag=\"116\" dirTag=\"116_1160018v0_0\" tripTag=\"14755562\" lat=\"42.379606\" lon=\"-71.039195\" secsSinceReport=\"59\" predictable=\"true\" heading=\"359\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0792\" routeTag=\"236\" dirTag=\"236_2360005v0_1\" tripTag=\"14608590\" lat=\"42.2120594\" lon=\"-70.9904448\" secsSinceReport=\"59\" predictable=\"true\" heading=\"72\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0719\" routeTag=\"86\" dirTag=\"86_860006v0_0\" tripTag=\"14700907\" lat=\"42.3642599\" lon=\"-71.1283059\" secsSinceReport=\"59\" predictable=\"true\" heading=\"45\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2289\" routeTag=\"28\" dirTag=\"28_280005v0_1\" tripTag=\"14658873\" lat=\"42.3081255\" lon=\"-71.0833236\" secsSinceReport=\"14\" predictable=\"true\" heading=\"27\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4117\" routeTag=\"73\" dirTag=\"73_730016v0_0\" tripTag=\"14574157\" lat=\"42.3777593\" lon=\"-71.1204558\" secsSinceReport=\"44\" predictable=\"true\" heading=\"129\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2184\" routeTag=\"44\" dirTag=\"44_440006v0_0\" tripTag=\"14440761\" lat=\"42.3261661\" lon=\"-71.0834309\" secsSinceReport=\"29\" predictable=\"true\" heading=\"186\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2071\" routeTag=\"36\" dirTag=\"36_360010v0_0\" lat=\"42.3005223\" lon=\"-71.1146226\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2093\" routeTag=\"26\" dirTag=\"26_260004v0_1\" lat=\"42.2837742\" lon=\"-71.063761\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2089\" routeTag=\"34\" dirTag=\"34_340081v0_1\" tripTag=\"14620463\" lat=\"42.2994405\" lon=\"-71.1150815\" secsSinceReport=\"29\" predictable=\"true\" heading=\"24\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0908\" routeTag=\"222\" dirTag=\"222_2220026v0_1\" tripTag=\"14608106\" lat=\"42.1969556\" lon=\"-70.9254073\" secsSinceReport=\"44\" predictable=\"true\" heading=\"34\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1221\" routeTag=\"28\" dirTag=\"28_280022v0_0\" lat=\"42.3312447\" lon=\"-71.0927157\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2125\" routeTag=\"57\" dirTag=\"57_570002v0_1\" tripTag=\"14440874\" lat=\"42.3488731\" lon=\"-71.1593912\" secsSinceReport=\"29\" predictable=\"true\" heading=\"84\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2145\" routeTag=\"22\" dirTag=\"22_220028v0_0\" tripTag=\"14441572\" lat=\"42.2903706\" lon=\"-71.0726488\" secsSinceReport=\"44\" predictable=\"true\" heading=\"112\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0576\" routeTag=\"57\" dirTag=\"57_570003v0_0\" tripTag=\"14418115\" lat=\"42.3505782\" lon=\"-71.1102944\" secsSinceReport=\"59\" predictable=\"true\" heading=\"279\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4105\" routeTag=\"71\" dirTag=\"71_710013v0_1\" tripTag=\"14574347\" lat=\"42.3740131\" lon=\"-71.119009\" secsSinceReport=\"14\" predictable=\"true\" heading=\"0\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2250\" routeTag=\"9\" dirTag=\"9_90019v0_0\" tripTag=\"14442258\" lat=\"42.3391907\" lon=\"-71.0515044\" secsSinceReport=\"59\" predictable=\"true\" heading=\"131\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2112\" routeTag=\"32\" dirTag=\"32_320008v0_0\" tripTag=\"14619775\" lat=\"42.3006822\" lon=\"-71.1133747\" secsSinceReport=\"44\" predictable=\"true\" heading=\"208\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0690\" routeTag=\"134\" dirTag=\"134_1340007v0_1\" tripTag=\"14580370\" lat=\"42.4055787\" lon=\"-71.0823994\" secsSinceReport=\"74\" predictable=\"true\" heading=\"188\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2274\" routeTag=\"1\" dirTag=\"1_10016v0_1\" tripTag=\"14440324\" lat=\"42.344969\" lon=\"-71.0866972\" secsSinceReport=\"44\" predictable=\"true\" heading=\"158\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0875\" routeTag=\"429\" dirTag=\"429_4290025v0_0\" tripTag=\"14755332\" lat=\"42.4267913\" lon=\"-71.0112724\" secsSinceReport=\"119\" predictable=\"true\" heading=\"46\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2179\" routeTag=\"43\" dirTag=\"43_430002v0_1\" tripTag=\"14440708\" lat=\"42.3523258\" lon=\"-71.067428\" secsSinceReport=\"59\" predictable=\"true\" heading=\"339\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0701\" routeTag=\"104\" dirTag=\"104_1040010v0_0\" tripTag=\"14581019\" lat=\"42.4078035\" lon=\"-71.0547273\" secsSinceReport=\"29\" predictable=\"true\" heading=\"34\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1035\" routeTag=\"39\" dirTag=\"39_390004v0_0\" tripTag=\"14658577\" lat=\"42.3391617\" lon=\"-71.0922092\" secsSinceReport=\"44\" predictable=\"true\" heading=\"241\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2280\" routeTag=\"16\" dirTag=\"16_160010v0_1\" tripTag=\"14440575\" lat=\"42.3007893\" lon=\"-71.1027847\" secsSinceReport=\"59\" predictable=\"true\" heading=\"107\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0486\" routeTag=\"57\" dirTag=\"57_570003v0_0\" tripTag=\"14418098\" lat=\"42.3562074\" lon=\"-71.182447\" secsSinceReport=\"44\" predictable=\"true\" heading=\"324\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0904\" routeTag=\"215\" dirTag=\"240_2400021v0_0\" tripTag=\"14605062\" lat=\"42.2843431\" lon=\"-71.0639101\" secsSinceReport=\"74\" predictable=\"true\" heading=\"324\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2069\" routeTag=\"29\" dirTag=\"29_290007v0_1\" tripTag=\"14618514\" lat=\"42.2928235\" lon=\"-71.0884699\" secsSinceReport=\"29\" predictable=\"true\" heading=\"14\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2036\" routeTag=\"41\" dirTag=\"41_410006v0_1\" tripTag=\"14619276\" lat=\"42.3227462\" lon=\"-71.1000333\" secsSinceReport=\"59\" predictable=\"true\" heading=\"59\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0887\" routeTag=\"426455\" dirTag=\"426455_4260039v0_0\" tripTag=\"14754932\" lat=\"42.4627221\" lon=\"-70.9636562\" secsSinceReport=\"44\" predictable=\"true\" heading=\"76\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2158\" routeTag=\"66\" dirTag=\"66_660017v0_1\" lat=\"42.3753489\" lon=\"-71.1193386\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1131\" routeTag=\"741\" dirTag=\"741_7410003v0_1\" tripTag=\"14658458\" lat=\"42.3693211\" lon=\"-71.0197935\" secsSinceReport=\"44\" predictable=\"true\" heading=\"305\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0612\" routeTag=\"111\" dirTag=\"111_1110026v0_1\" tripTag=\"14581715\" lat=\"42.410599\" lon=\"-71.012227\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2225\" routeTag=\"8\" dirTag=\"8_80007v0_0\" tripTag=\"14440413\" lat=\"42.3406454\" lon=\"-71.0707958\" secsSinceReport=\"59\" predictable=\"true\" heading=\"48\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2144\" routeTag=\"22\" dirTag=\"22_220024v0_1\" tripTag=\"14441566\" lat=\"42.3226649\" lon=\"-71.0993663\" secsSinceReport=\"44\" predictable=\"true\" heading=\"98\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0750\" routeTag=\"602\" dirTag=\"602_6020017v0_1\" lat=\"42.3708414\" lon=\"-71.0772405\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1217\" routeTag=\"28\" dirTag=\"28_280022v0_0\" tripTag=\"14659004\" lat=\"42.2873558\" lon=\"-71.0905142\" secsSinceReport=\"59\" predictable=\"true\" heading=\"194\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0675\" routeTag=\"79\" dirTag=\"79_790006v0_0\" tripTag=\"14701856\" lat=\"42.3978278\" lon=\"-71.1407718\" secsSinceReport=\"29\" predictable=\"true\" heading=\"275\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0816\" routeTag=\"23\" dirTag=\"23_230019v0_0\" tripTag=\"14442486\" lat=\"42.3311619\" lon=\"-71.0908542\" secsSinceReport=\"14\" predictable=\"true\" heading=\"91\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0635\" routeTag=\"602\" dirTag=\"602_6020017v0_1\" lat=\"42.3708856\" lon=\"-71.0775396\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0640\" routeTag=\"96\" dirTag=\"96_960009v0_1\" tripTag=\"14580720\" lat=\"42.3826581\" lon=\"-71.1197328\" secsSinceReport=\"59\" predictable=\"true\" heading=\"185\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2173\" routeTag=\"66\" dirTag=\"66_660017v0_1\" tripTag=\"14442001\" lat=\"42.3317033\" lon=\"-71.1154493\" secsSinceReport=\"59\" predictable=\"true\" heading=\"85\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0730\" routeTag=\"90\" dirTag=\"90_900003v0_1\" tripTag=\"14580232\" lat=\"42.394755\" lon=\"-71.1183141\" secsSinceReport=\"59\" predictable=\"true\" heading=\"120\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1122\" routeTag=\"741\" dirTag=\"741_7410003v0_1\" tripTag=\"14658378\" lat=\"42.3490328\" lon=\"-71.0438373\" secsSinceReport=\"29\" predictable=\"true\" heading=\"337\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2167\" routeTag=\"47\" dirTag=\"47_470020v0_0\" lat=\"42.3426949\" lon=\"-71.0612613\" secsSinceReport=\"74\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0722\" routeTag=\"93\" dirTag=\"93_930018v0_1\" tripTag=\"14580468\" lat=\"42.3657806\" lon=\"-71.0584094\" secsSinceReport=\"29\" predictable=\"true\" heading=\"178\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0474\" routeTag=\"60\" dirTag=\"60_600004v0_1\" tripTag=\"14418329\" lat=\"42.3257754\" lon=\"-71.1227322\" secsSinceReport=\"59\" predictable=\"true\" heading=\"122\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0618\" routeTag=\"111\" dirTag=\"111_1110019v0_0\" tripTag=\"14581670\" lat=\"42.396946\" lon=\"-71.0316044\" secsSinceReport=\"44\" predictable=\"true\" heading=\"26\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4106\" routeTag=\"73\" dirTag=\"73_730006v0_1\" tripTag=\"14574312\" lat=\"42.3871175\" lon=\"-71.1907835\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0603\" routeTag=\"602\" dirTag=\"602_6020018v0_0\" lat=\"42.3653369\" lon=\"-71.0601578\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2252\" routeTag=\"10\" dirTag=\"10_100020v0_1\" tripTag=\"14441246\" lat=\"42.3356039\" lon=\"-71.0397164\" secsSinceReport=\"44\" predictable=\"true\" heading=\"269\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0753\" routeTag=\"350\" dirTag=\"350_3500006v0_0\" tripTag=\"14701196\" lat=\"42.4204143\" lon=\"-71.1530783\" secsSinceReport=\"59\" predictable=\"true\" heading=\"260\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0737\" routeTag=\"64\" dirTag=\"64_640005v0_0\" tripTag=\"14700053\" lat=\"42.358033\" lon=\"-71.1618515\" secsSinceReport=\"44\" predictable=\"true\" heading=\"212\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0885\" routeTag=\"450\" dirTag=\"450_4500014v0_1\" tripTag=\"14754962\" lat=\"42.4686912\" lon=\"-70.9582807\" secsSinceReport=\"14\" predictable=\"true\" heading=\"222\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0609\" routeTag=\"350\" dirTag=\"350_3500004v0_1\" tripTag=\"14701175\" lat=\"42.4834305\" lon=\"-71.2182036\" secsSinceReport=\"74\" predictable=\"true\" heading=\"241\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0671\" routeTag=\"78\" dirTag=\"78_780014v0_0\" lat=\"42.3734384\" lon=\"-71.1198381\" secsSinceReport=\"74\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0844\" routeTag=\"116117\" dirTag=\"116117_1160011v0_0\" tripTag=\"14755849\" lat=\"42.3822254\" lon=\"-71.0392722\" secsSinceReport=\"14\" predictable=\"true\" heading=\"4\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0687\" routeTag=\"62\" dirTag=\"62_620007v0_1\" tripTag=\"14700420\" lat=\"42.4261611\" lon=\"-71.1967498\" secsSinceReport=\"74\" predictable=\"true\" heading=\"117\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0303\" routeTag=\"602\" dirTag=\"602_6020017v0_1\" lat=\"42.3709096\" lon=\"-71.0777252\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0685\" routeTag=\"23\" dirTag=\"23_230003v0_1\" lat=\"42.2839696\" lon=\"-71.0637026\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0706\" routeTag=\"89\" dirTag=\"89_890007v0_0\" tripTag=\"14580161\" lat=\"42.3985841\" lon=\"-71.1208323\" secsSinceReport=\"179\" predictable=\"true\" heading=\"238\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2275\" routeTag=\"10\" dirTag=\"10_100020v0_1\" tripTag=\"14441245\" lat=\"42.3408068\" lon=\"-71.0708463\" secsSinceReport=\"44\" predictable=\"true\" heading=\"298\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0628\" routeTag=\"111\" dirTag=\"111_1110019v0_0\" lat=\"42.3683052\" lon=\"-71.059269\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2123\" routeTag=\"40\" dirTag=\"40_400003v0_1\" tripTag=\"14620329\" lat=\"42.2903661\" lon=\"-71.1239056\" secsSinceReport=\"14\" predictable=\"true\" heading=\"39\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0704\" routeTag=\"106\" dirTag=\"106_1060043v0_0\" tripTag=\"14581077\" lat=\"42.4015932\" lon=\"-71.0686284\" secsSinceReport=\"29\" predictable=\"true\" heading=\"137\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2210\" routeTag=\"66\" dirTag=\"66_660022v0_0\" tripTag=\"14440955\" lat=\"42.3554111\" lon=\"-71.1329417\" secsSinceReport=\"59\" predictable=\"true\" heading=\"62\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2050\" routeTag=\"51\" dirTag=\"51_510005v0_0\" tripTag=\"14620343\" lat=\"42.300752\" lon=\"-71.1143269\" secsSinceReport=\"59\" predictable=\"true\" heading=\"203\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0870\" routeTag=\"119\" dirTag=\"119_1190004v0_0\" tripTag=\"14755616\" lat=\"42.4312534\" lon=\"-71.0237135\" secsSinceReport=\"59\" predictable=\"true\" heading=\"324\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2238\" routeTag=\"16\" dirTag=\"16_160013v0_0\" lat=\"42.3304548\" lon=\"-71.0617735\" secsSinceReport=\"29\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0679\" routeTag=\"77\" dirTag=\"77_770018v0_0\" tripTag=\"14700492\" lat=\"42.4149191\" lon=\"-71.1514958\" secsSinceReport=\"44\" predictable=\"true\" heading=\"297\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2022\" routeTag=\"32\" dirTag=\"32_320007v0_0\" tripTag=\"14619838\" lat=\"42.2947297\" lon=\"-71.1170869\" secsSinceReport=\"29\" predictable=\"true\" heading=\"197\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1123\" routeTag=\"741\" dirTag=\"741_7410004v0_0\" lat=\"42.3520649\" lon=\"-71.0558476\" secsSinceReport=\"194\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2114\" routeTag=\"29\" dirTag=\"29_290010v0_0\" tripTag=\"14618524\" lat=\"42.2860162\" lon=\"-71.090976\" secsSinceReport=\"14\" predictable=\"true\" heading=\"193\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0619\" routeTag=\"69\" dirTag=\"69_690004v0_0\" tripTag=\"14701087\" lat=\"42.3732244\" lon=\"-71.0969719\" secsSinceReport=\"104\" predictable=\"true\" heading=\"280\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2005\" routeTag=\"29\" dirTag=\"29_290010v0_0\" lat=\"42.336566\" lon=\"-71.0889701\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1132\" routeTag=\"741\" dirTag=\"741_7410004v0_0\" tripTag=\"14658442\" lat=\"42.3470997\" lon=\"-71.0374426\" secsSinceReport=\"59\" predictable=\"true\" heading=\"125\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1038\" routeTag=\"39\" dirTag=\"39_390004v0_0\" tripTag=\"14658550\" lat=\"42.3476246\" lon=\"-71.0744995\" secsSinceReport=\"29\" predictable=\"true\" heading=\"268\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0711\" routeTag=\"70\" dirTag=\"70_700004v0_1\" lat=\"42.3727036\" lon=\"-71.2638024\" secsSinceReport=\"134\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0720\" routeTag=\"111\" dirTag=\"111_1110026v0_1\" tripTag=\"14581626\" lat=\"42.402997\" lon=\"-71.0362958\" secsSinceReport=\"14\" predictable=\"true\" heading=\"177\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0643\" routeTag=\"602\" dirTag=\"602_6020018v0_0\" lat=\"42.3652803\" lon=\"-71.0605686\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0718\" routeTag=\"602\" dirTag=\"602_6020018v0_0\" lat=\"42.3655312\" lon=\"-71.0606133\" secsSinceReport=\"29\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0634\" routeTag=\"354\" dirTag=\"354_3540007v0_0\" tripTag=\"14581290\" lat=\"42.4781175\" lon=\"-71.1132538\" secsSinceReport=\"14\" predictable=\"true\" heading=\"333\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2135\" routeTag=\"15\" dirTag=\"15_150008v0_0\" tripTag=\"14440501\" lat=\"42.3292273\" lon=\"-71.0848053\" secsSinceReport=\"44\" predictable=\"true\" heading=\"200\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2186\" routeTag=\"22\" dirTag=\"22_220024v0_1\" tripTag=\"14441567\" lat=\"42.3223973\" lon=\"-71.0983636\" secsSinceReport=\"104\" predictable=\"true\" heading=\"90\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2181\" routeTag=\"1\" dirTag=\"1_10016v0_1\" tripTag=\"14441458\" lat=\"42.3580032\" lon=\"-71.0930879\" secsSinceReport=\"44\" predictable=\"true\" heading=\"158\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1027\" routeTag=\"39\" dirTag=\"39_390003v0_1\" tripTag=\"14658559\" lat=\"42.3417784\" lon=\"-71.0860329\" secsSinceReport=\"29\" predictable=\"true\" heading=\"40\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0778\" routeTag=\"230\" dirTag=\"230_2300005v0_0\" tripTag=\"14608018\" lat=\"42.174844\" lon=\"-71.0127701\" secsSinceReport=\"44\" predictable=\"true\" heading=\"147\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2243\" routeTag=\"1\" dirTag=\"1_10016v0_1\" tripTag=\"14441436\" lat=\"42.3730456\" lon=\"-71.1175887\" secsSinceReport=\"59\" predictable=\"true\" heading=\"289\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2208\" routeTag=\"66\" dirTag=\"66_660022v0_0\" tripTag=\"14441993\" lat=\"42.3292287\" lon=\"-71.0838731\" secsSinceReport=\"44\" predictable=\"true\" heading=\"333\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0809\" routeTag=\"216\" dirTag=\"216_2160005v0_0\" tripTag=\"14608367\" lat=\"42.2512938\" lon=\"-71.0023647\" secsSinceReport=\"44\" predictable=\"true\" heading=\"108\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2266\" routeTag=\"57\" dirTag=\"57_570003v0_0\" tripTag=\"14440866\" lat=\"42.3491252\" lon=\"-71.1568782\" secsSinceReport=\"179\" predictable=\"true\" heading=\"277\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0839\" routeTag=\"455\" dirTag=\"455_4550020v0_0\" tripTag=\"14755057\" lat=\"42.5208761\" lon=\"-70.8955298\" secsSinceReport=\"59\" predictable=\"true\" heading=\"353\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0581\" routeTag=\"708\" dirTag=\"708_7080014v0_1\" tripTag=\"14417760\" lat=\"42.3307722\" lon=\"-71.0636454\" secsSinceReport=\"14\" predictable=\"true\" heading=\"105\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2272\" routeTag=\"39\" dirTag=\"39_390004v0_0\" tripTag=\"14658552\" lat=\"42.3157596\" lon=\"-71.1141333\" secsSinceReport=\"44\" predictable=\"true\" heading=\"171\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2223\" routeTag=\"9\" dirTag=\"9_90006v0_1\" tripTag=\"14442221\" lat=\"42.3492512\" lon=\"-71.0803242\" secsSinceReport=\"44\" predictable=\"true\" heading=\"72\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4113\" routeTag=\"73\" dirTag=\"73_730006v0_1\" tripTag=\"14574184\" lat=\"42.3753036\" lon=\"-71.1421297\" secsSinceReport=\"14\" predictable=\"true\" heading=\"103\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2189\" routeTag=\"1\" dirTag=\"1_10017v0_0\" lat=\"42.3347336\" lon=\"-71.0746433\" secsSinceReport=\"14\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0644\" routeTag=\"110\" dirTag=\"110_1100008v0_0\" tripTag=\"14581439\" lat=\"42.4083266\" lon=\"-71.0461948\" secsSinceReport=\"44\" predictable=\"true\" heading=\"13\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0880\" routeTag=\"426455\" dirTag=\"426455_4260035v0_1\" tripTag=\"14755023\" lat=\"42.4980386\" lon=\"-70.8949808\" secsSinceReport=\"44\" predictable=\"true\" heading=\"156\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2058\" routeTag=\"21\" dirTag=\"21_210006v0_0\" tripTag=\"14619161\" lat=\"42.3000055\" lon=\"-71.1137418\" secsSinceReport=\"119\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0846\" routeTag=\"116\" dirTag=\"116_1160017v0_1\" lat=\"42.4554013\" lon=\"-70.9742888\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2141\" routeTag=\"555\" dirTag=\"555_5550002v0_0\" tripTag=\"14440985\" lat=\"42.3497155\" lon=\"-71.0752695\" secsSinceReport=\"59\" predictable=\"true\" heading=\"249\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0708\" routeTag=\"92\" dirTag=\"91_910007v0_1\" tripTag=\"14580572\" lat=\"42.3847105\" lon=\"-71.0764183\" secsSinceReport=\"59\" predictable=\"true\" heading=\"230\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0326\" routeTag=\"88\" dirTag=\"88_880018v0_1\" tripTag=\"14700981\" lat=\"42.3806012\" lon=\"-71.0899722\" secsSinceReport=\"59\" predictable=\"true\" heading=\"172\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2060\" routeTag=\"31\" dirTag=\"31_310003v0_1\" tripTag=\"14618613\" lat=\"42.2847551\" lon=\"-71.0923586\" secsSinceReport=\"59\" predictable=\"true\" heading=\"300\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2259\" routeTag=\"66\" dirTag=\"66_660017v0_1\" tripTag=\"14441996\" lat=\"42.3507799\" lon=\"-71.1312933\" secsSinceReport=\"44\" predictable=\"true\" heading=\"162\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2156\" routeTag=\"17\" dirTag=\"17_170004v0_0\" tripTag=\"14440596\" lat=\"42.3096862\" lon=\"-71.0638642\" secsSinceReport=\"59\" predictable=\"true\" heading=\"214\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2258\" routeTag=\"55\" dirTag=\"55_550007v0_1\" tripTag=\"14440811\" lat=\"42.3452794\" lon=\"-71.0953289\" secsSinceReport=\"44\" predictable=\"true\" heading=\"357\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0072\" routeTag=\"220\" dirTag=\"220_2200014v0_0\" lat=\"42.2510165\" lon=\"-71.0033868\" secsSinceReport=\"59\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0850\" routeTag=\"426455\" dirTag=\"426455_4260039v0_0\" lat=\"42.3667033\" lon=\"-71.0584738\" secsSinceReport=\"59\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4121\" routeTag=\"71\" dirTag=\"71_710007v0_0\" tripTag=\"14574317\" lat=\"42.3707549\" lon=\"-71.1648268\" secsSinceReport=\"44\" predictable=\"true\" heading=\"243\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0822\" routeTag=\"23\" dirTag=\"23_230019v0_0\" tripTag=\"14442485\" lat=\"42.3242605\" lon=\"-71.0829896\" secsSinceReport=\"119\" predictable=\"true\" heading=\"165\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0712\" routeTag=\"411\" dirTag=\"411_4110029v0_1\" tripTag=\"14581238\" lat=\"42.4297558\" lon=\"-71.0605222\" secsSinceReport=\"44\" predictable=\"true\" heading=\"231\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0636\" routeTag=\"602\" dirTag=\"602_6020017v0_1\" lat=\"42.3706249\" lon=\"-71.0771477\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2249\" routeTag=\"45\" dirTag=\"45_450003v0_1\" tripTag=\"14440743\" lat=\"42.3060949\" lon=\"-71.0844397\" secsSinceReport=\"44\" predictable=\"true\" heading=\"7\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0734\" routeTag=\"602\" dirTag=\"602_6020018v0_0\" tripTag=\"14702256\" lat=\"42.3658187\" lon=\"-71.0631975\" secsSinceReport=\"59\" predictable=\"true\" heading=\"146\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0672\" routeTag=\"96\" dirTag=\"96_960010v0_0\" tripTag=\"14580544\" lat=\"42.3966188\" lon=\"-71.1218097\" secsSinceReport=\"104\" predictable=\"true\" heading=\"295\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0605\" routeTag=\"70\" dirTag=\"70_700011v0_0\" tripTag=\"14700327\" lat=\"42.3630865\" lon=\"-71.1589161\" secsSinceReport=\"74\" predictable=\"true\" heading=\"278\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2294\" routeTag=\"39\" dirTag=\"39_390003v0_1\" tripTag=\"14658703\" lat=\"42.329078\" lon=\"-71.1107482\" secsSinceReport=\"29\" predictable=\"true\" heading=\"334\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0785\" routeTag=\"238\" dirTag=\"225_2250012v0_0\" tripTag=\"14608219\" lat=\"42.2525509\" lon=\"-71.0054947\" secsSinceReport=\"44\" predictable=\"true\" heading=\"206\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2043\" routeTag=\"51\" dirTag=\"51_510004v0_1\" tripTag=\"14620253\" lat=\"42.2916432\" lon=\"-71.1225093\" secsSinceReport=\"14\" predictable=\"true\" heading=\"38\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0646\" routeTag=\"99\" dirTag=\"99_990014v0_1\" tripTag=\"14580729\" lat=\"42.4399952\" lon=\"-71.0909554\" secsSinceReport=\"29\" predictable=\"true\" heading=\"138\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0724\" routeTag=\"101\" dirTag=\"101_1010052v0_0\" lat=\"42.3868969\" lon=\"-71.080735\" secsSinceReport=\"29\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0780\" routeTag=\"240\" dirTag=\"240_2400010v0_1\" tripTag=\"14608629\" lat=\"42.2378413\" lon=\"-71.005968\" secsSinceReport=\"14\" predictable=\"true\" heading=\"11\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2299\" routeTag=\"45\" dirTag=\"45_450006v0_0\" tripTag=\"14440726\" lat=\"42.3317789\" lon=\"-71.0941382\" secsSinceReport=\"14\" predictable=\"true\" heading=\"219\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0869\" routeTag=\"442\" dirTag=\"442_4420031v0_1\" tripTag=\"14755292\" lat=\"42.4655307\" lon=\"-70.9323068\" secsSinceReport=\"59\" predictable=\"true\" heading=\"254\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0725\" routeTag=\"77\" dirTag=\"77_770018v0_0\" tripTag=\"14700462\" lat=\"42.3729402\" lon=\"-71.1222503\" secsSinceReport=\"59\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1118\" routeTag=\"742\" dirTag=\"742_7420004v0_1\" tripTag=\"14659506\" lat=\"42.3446669\" lon=\"-71.0335744\" secsSinceReport=\"44\" predictable=\"true\" heading=\"90\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"6010\" routeTag=\"751\" dirTag=\"751_7510007v0_1\" tripTag=\"14658782\" lat=\"42.3300167\" lon=\"-71.0843845\" secsSinceReport=\"44\" predictable=\"true\" heading=\"213\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2219\" routeTag=\"60\" dirTag=\"60_600017v0_0\" lat=\"42.3416533\" lon=\"-71.1009518\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2029\" routeTag=\"32\" dirTag=\"32_320007v0_0\" tripTag=\"14620509\" lat=\"42.2980827\" lon=\"-71.1148517\" secsSinceReport=\"29\" predictable=\"true\" heading=\"208\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0217\" routeTag=\"276\" dirTag=\"276_2760012v0_0\" lat=\"42.3343142\" lon=\"-71.0700808\" secsSinceReport=\"29\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0791\" routeTag=\"240\" dirTag=\"240_2400015v0_0\" tripTag=\"14605063\" lat=\"42.1682716\" lon=\"-71.0450489\" secsSinceReport=\"29\" predictable=\"true\" heading=\"154\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2271\" routeTag=\"39\" dirTag=\"39_390004v0_0\" lat=\"42.3473926\" lon=\"-71.0735999\" secsSinceReport=\"29\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2267\" routeTag=\"1\" dirTag=\"1_10016v0_1\" tripTag=\"14440304\" lat=\"42.3337492\" lon=\"-71.0738755\" secsSinceReport=\"59\" predictable=\"true\" heading=\"139\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2155\" routeTag=\"9\" dirTag=\"9_90006v0_1\" lat=\"42.3385868\" lon=\"-71.0320696\" secsSinceReport=\"59\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0841\" routeTag=\"442\" dirTag=\"442_4420032v0_0\" tripTag=\"14755280\" lat=\"42.4119638\" lon=\"-70.9946292\" secsSinceReport=\"14\" predictable=\"true\" heading=\"63\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1004\" routeTag=\"749\" dirTag=\"749_7490007v0_1\" tripTag=\"14658610\" lat=\"42.3530694\" lon=\"-71.0625123\" secsSinceReport=\"44\" predictable=\"true\" heading=\"2\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0466\" routeTag=\"57\" dirTag=\"57_570002v0_1\" tripTag=\"14418096\" lat=\"42.3639585\" lon=\"-71.1854155\" secsSinceReport=\"29\" predictable=\"true\" heading=\"81\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0668\" routeTag=\"137\" dirTag=\"137_1370013v0_0\" tripTag=\"14580856\" lat=\"42.4786606\" lon=\"-71.0631154\" secsSinceReport=\"74\" predictable=\"true\" heading=\"328\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0173\" routeTag=\"350\" dirTag=\"350_3500004v0_1\" tripTag=\"14701210\" lat=\"42.3968348\" lon=\"-71.1414032\" secsSinceReport=\"14\" predictable=\"true\" heading=\"164\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1012\" routeTag=\"751\" dirTag=\"751_7510008v0_0\" tripTag=\"14658756\" lat=\"42.3520948\" lon=\"-71.0560153\" secsSinceReport=\"44\" predictable=\"true\" heading=\"117\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2025\" routeTag=\"2427\" dirTag=\"2427_240011v0_0\" tripTag=\"14620145\" lat=\"42.2510171\" lon=\"-71.1119234\" secsSinceReport=\"44\" predictable=\"true\" heading=\"324\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1115\" routeTag=\"742\" dirTag=\"742_7420007v0_0\" tripTag=\"14659505\" lat=\"42.3532769\" lon=\"-71.0545245\" secsSinceReport=\"59\" predictable=\"true\" heading=\"58\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1209\" routeTag=\"28\" dirTag=\"28_280022v0_0\" tripTag=\"14658944\" lat=\"42.295093\" lon=\"-71.0880345\" secsSinceReport=\"14\" predictable=\"true\" heading=\"191\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0623\" routeTag=\"77\" dirTag=\"77_770004v0_1\" tripTag=\"14700470\" lat=\"42.4009292\" lon=\"-71.1361887\" secsSinceReport=\"14\" predictable=\"true\" heading=\"127\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1018\" routeTag=\"749\" dirTag=\"749_7490007v0_1\" lat=\"42.3295821\" lon=\"-71.0840678\" secsSinceReport=\"44\" predictable=\"true\" heading=\"217\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2139\" routeTag=\"1\" dirTag=\"1_10017v0_0\" tripTag=\"14441437\" lat=\"42.3478687\" lon=\"-71.0880075\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2198\" routeTag=\"57\" dirTag=\"57_570003v0_0\" tripTag=\"14440868\" lat=\"42.3530465\" lon=\"-71.1317047\" secsSinceReport=\"44\" predictable=\"true\" heading=\"283\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2001\" routeTag=\"31\" dirTag=\"31_310004v0_0\" tripTag=\"14618585\" lat=\"42.2877465\" lon=\"-71.0940975\" secsSinceReport=\"14\" predictable=\"true\" heading=\"161\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0652\" routeTag=\"109\" dirTag=\"109_1090040v0_0\" lat=\"42.3850067\" lon=\"-71.0726488\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2148\" routeTag=\"66\" dirTag=\"66_660022v0_0\" tripTag=\"14440984\" lat=\"42.3384831\" lon=\"-71.1212815\" secsSinceReport=\"14\" predictable=\"true\" heading=\"4\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0698\" routeTag=\"95\" dirTag=\"95_950025v0_0\" lat=\"42.3868917\" lon=\"-71.0774892\" secsSinceReport=\"44\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2220\" routeTag=\"10\" dirTag=\"10_100009v0_0\" tripTag=\"14441280\" lat=\"42.3337635\" lon=\"-71.0731592\" secsSinceReport=\"59\" predictable=\"true\" heading=\"226\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2054\" routeTag=\"34E\" dirTag=\"34E_340089v0_0\" tripTag=\"14620530\" lat=\"42.2567203\" lon=\"-71.1627951\" secsSinceReport=\"29\" predictable=\"true\" heading=\"222\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2100\" routeTag=\"30\" dirTag=\"30_300003v0_1\" tripTag=\"14619215\" lat=\"42.2873151\" lon=\"-71.1271405\" secsSinceReport=\"14\" predictable=\"true\" heading=\"38\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4126\" routeTag=\"72\" dirTag=\"72_720010v0_1\" tripTag=\"14574134\" lat=\"42.3812761\" lon=\"-71.1369862\" secsSinceReport=\"59\" predictable=\"true\" heading=\"84\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1210\" routeTag=\"28\" dirTag=\"28_280022v0_0\" tripTag=\"14658913\" lat=\"42.3287949\" lon=\"-71.084784\" secsSinceReport=\"29\" predictable=\"true\" heading=\"103\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0680\" routeTag=\"109\" dirTag=\"109_1090032v0_1\" tripTag=\"14581365\" lat=\"42.4002187\" lon=\"-71.0619775\" secsSinceReport=\"59\" predictable=\"true\" heading=\"219\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0751\" routeTag=\"75\" dirTag=\"75_750006v0_0\" tripTag=\"14700648\" lat=\"42.3890718\" lon=\"-71.1609978\" secsSinceReport=\"44\" predictable=\"true\" heading=\"3\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0726\" routeTag=\"111\" dirTag=\"111_1110019v0_0\" tripTag=\"14581831\" lat=\"42.3985543\" lon=\"-71.0311535\" secsSinceReport=\"74\" predictable=\"true\" heading=\"323\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0898\" routeTag=\"117\" dirTag=\"117_1170016v0_1\" tripTag=\"14755474\" lat=\"42.4085953\" lon=\"-70.9932999\" secsSinceReport=\"14\" predictable=\"true\" heading=\"274\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4111\" routeTag=\"73\" dirTag=\"73_730008v0_0\" tripTag=\"14574345\" lat=\"42.3761803\" lon=\"-71.1581198\" secsSinceReport=\"44\" predictable=\"true\" heading=\"295\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0480\" routeTag=\"57\" dirTag=\"57_570002v0_1\" tripTag=\"14418108\" lat=\"42.3529685\" lon=\"-71.1323625\" secsSinceReport=\"44\" predictable=\"true\" heading=\"100\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0882\" routeTag=\"450\" dirTag=\"450_4500004v0_1\" lat=\"42.5220255\" lon=\"-70.8987956\" secsSinceReport=\"14\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"1019\" routeTag=\"749\" dirTag=\"749_7490008v0_0\" tripTag=\"14658675\" lat=\"42.3509407\" lon=\"-71.0639676\" secsSinceReport=\"59\" predictable=\"true\" heading=\"97\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0703\" routeTag=\"88\" dirTag=\"88_880020v0_0\" tripTag=\"14701031\" lat=\"42.3968984\" lon=\"-71.1228353\" secsSinceReport=\"14\" predictable=\"true\" heading=\"331\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0654\" routeTag=\"430\" dirTag=\"430_4300027v0_0\" tripTag=\"14582093\" lat=\"42.4364921\" lon=\"-71.0376955\" secsSinceReport=\"44\" predictable=\"true\" heading=\"17\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0811\" routeTag=\"23\" dirTag=\"23_230019v0_0\" tripTag=\"14442544\" lat=\"42.3370089\" lon=\"-71.0893861\" secsSinceReport=\"209\" predictable=\"true\" heading=\"67\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0729\" routeTag=\"80\" dirTag=\"80_800004v0_0\" tripTag=\"14700766\" lat=\"42.4212869\" lon=\"-71.1376629\" secsSinceReport=\"59\" predictable=\"true\" heading=\"259\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0638\" routeTag=\"111\" dirTag=\"111_1110026v0_1\" tripTag=\"14581541\" lat=\"42.3661273\" lon=\"-71.0584152\" secsSinceReport=\"14\" predictable=\"true\" heading=\"178\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0102\" routeTag=\"120\" dirTag=\"120_1200022v0_0\" lat=\"42.3751157\" lon=\"-71.0397032\" secsSinceReport=\"29\" predictable=\"true\" heading=\"216\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0515\" routeTag=\"411\" dirTag=\"411_4110030v0_0\" tripTag=\"14529590\" lat=\"42.4345973\" lon=\"-71.0385464\" secsSinceReport=\"59\" predictable=\"true\" heading=\"21\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0805\" routeTag=\"201\" dirTag=\"201_2010009v0_0\" tripTag=\"14608955\" lat=\"42.2990475\" lon=\"-71.063729\" secsSinceReport=\"44\" predictable=\"true\" heading=\"236\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"0859\" routeTag=\"116\" dirTag=\"116_1160017v0_1\" tripTag=\"14755561\" lat=\"42.4142373\" lon=\"-70.9899561\" secsSinceReport=\"29\" predictable=\"true\" heading=\"17\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"2292\" routeTag=\"28\" dirTag=\"28_280005v0_1\" tripTag=\"14659061\" lat=\"42.2896946\" lon=\"-71.0894725\" secsSinceReport=\"44\" predictable=\"true\" heading=\"14\" speedKmHr=\"0.0\"/> "+
	"<vehicle id=\"4112\" routeTag=\"71\" dirTag=\"71_710005v0_1\" tripTag=\"14574225\" lat=\"42.3716917\" lon=\"-71.1572586\" secsSinceReport=\"14\" predictable=\"true\" heading=\"47\" speedKmHr=\"0.0\"/> "+
	"<lastTime time=\"1307406787681\"/> "+
	"</body> ";
	
	public void testParseWithXml()
	{
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 50; i++)
		{
			parseXmlWithUtil();
		}
		long endTime = System.currentTimeMillis();
		Log.e("BostonBusMap", "DIFF: " + ((float)(endTime - startTime))/1000.0);
	}

	public void testParseRegular()
	{
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 50; i++)
		{
			parseXmlRegular();
		}
		long endTime = System.currentTimeMillis();
		Log.e("BostonBusMap", "DIFF: " + ((float)(endTime - startTime))/1000.0);
	}

	public void testParseBetter()
	{
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 50; i++)
		{
			parseXmlBetter();
		}
		long endTime = System.currentTimeMillis();
		Log.e("BostonBusMap", "DIFF: " + ((float)(endTime - startTime))/1000.0);
	}

	private void parseXmlRegular()
	{
		InputStream stream = new ByteArrayInputStream(inputData.getBytes());
		
		Directions directions = null;
		MyHashMap<String, String> routeKeysToTitles = new MyHashMap<String, String>();
		VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(getDrawables(), directions, routeKeysToTitles);
		try {
			parser.runParse(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void parseXmlWithUtil()
	{
		InputStream stream = new ByteArrayInputStream(inputData.getBytes());
		Directions directions = null;
		MyHashMap<String, String> routeKeysToTitles = new MyHashMap<String, String>();
		VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(getDrawables(), directions, routeKeysToTitles);
		try {
			android.util.Xml.parse(stream, Encoding.UTF_8, parser);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void parseXmlBetter()
	{/*
	This used to use Piccolo. Results were mostly the same
	
		InputStream stream = new ByteArrayInputStream(inputData.getBytes());
		Drawable bus = getDrawable();		
		Drawable arrow = bus;
		Directions directions = null;
		HashMap<String, String> routeKeysToTitles = new HashMap<String, String>();
		VehicleLocationsFeedParser parser = new VehicleLocationsFeedParser(bus, arrow, directions, routeKeysToTitles);
		try {
			parser.parseBetter(stream);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	private TransitDrawables getDrawables() {
		Drawable empty = new Drawable() {
			
			@Override
			public void setColorFilter(ColorFilter cf) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAlpha(int alpha) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public int getOpacity() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public void draw(Canvas canvas) {
				// TODO Auto-generated method stub
				
			}
		};
		return new TransitDrawables(empty, empty, empty, empty);
	}
}
