package boston.Bus.Map.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.TreeMap;

import skylight1.opengl.files.QuickParseUtil;

import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.CommuterRailStopLocation;
import boston.Bus.Map.data.Directions;
import boston.Bus.Map.data.MyHashMap;
import boston.Bus.Map.data.MyTreeMap;
import boston.Bus.Map.data.Path;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RoutePool;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.SubwayStopLocation;
import boston.Bus.Map.main.UpdateAsyncTask;
import boston.Bus.Map.transit.CommuterRailTransitSource;

/**
 * This is a fake parser until something more automated is implemented
 * @author schneg
 *
 */
public class CommuterRailRouteConfigParser
{
	private final Directions directions;
	private final MyHashMap<String, RouteConfig> map = new MyHashMap<String, RouteConfig>();
	private final CommuterRailTransitSource source;
	
	public static final String temporaryInputData = "route_long_name,direction_id,stop_sequence,stop_id,stop_lat,stop_lon,Branch\n"+
	"Fairmount Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Fairmount Line,0,2,Uphams Corner,42.318670,-71.069072,Trunk\n"+
	"Fairmount Line,0,3,Morton Street,42.280994,-71.085475,Trunk\n"+
	"Fairmount Line,0,4,Fairmount,42.253638,-71.119270,Trunk\n"+
	"Fairmount Line,0,5,Readville,42.237750,-71.132376,Trunk\n"+
	"Fairmount Line,1,1,Readville,42.237750,-71.132376,Trunk\n"+
	"Fairmount Line,1,2,Fairmount,42.253638,-71.119270,Trunk\n"+
	"Fairmount Line,1,3,Morton Street,42.280994,-71.085475,Trunk\n"+
	"Fairmount Line,1,4,Uphams Corner,42.318670,-71.069072,Trunk\n"+
	"Fairmount Line,1,5,South Station,42.352614,-71.055364,Trunk\n"+
	"Fitchburg/South Acton Line,0,1,North Station,42.365551,-71.061251,Trunk\n"+
	"Fitchburg/South Acton Line,0,2,Porter Square,42.388353,-71.119159,Trunk\n"+
	"Fitchburg/South Acton Line,0,3,Belmont,42.398420,-71.174499,Trunk\n"+
	"Fitchburg/South Acton Line,0,4,Waverley,42.387489,-71.189864,Trunk\n"+
	"Fitchburg/South Acton Line,0,5,Waltham,42.374424,-71.236595,Trunk\n"+
	"Fitchburg/South Acton Line,0,6,Brandeis/ Roberts,42.361728,-71.260854,Trunk\n"+
	"Fitchburg/South Acton Line,0,7,Kendal Green,42.378970,-71.282411,Trunk\n"+
	"Fitchburg/South Acton Line,0,8,Hastings,42.385755,-71.289203,Trunk\n"+
	"Fitchburg/South Acton Line,0,9,Silver Hill,42.395625,-71.302357,Trunk\n"+
	"Fitchburg/South Acton Line,0,10,Lincoln,42.414229,-71.325344,Trunk\n"+
	"Fitchburg/South Acton Line,0,11,Concord,42.457147,-71.358051,Trunk\n"+
	"Fitchburg/South Acton Line,0,12,West Concord,42.456372,-71.392371,Trunk\n"+
	"Fitchburg/South Acton Line,0,13,South Acton,42.461575,-71.455322,Trunk\n"+
	"Fitchburg/South Acton Line,0,14,Littleton / Rte 495,42.519236,-71.502643,Trunk\n"+
	"Fitchburg/South Acton Line,0,15,Ayer,42.560047,-71.590117,Trunk\n"+
	"Fitchburg/South Acton Line,0,16,Shirley,42.544726,-71.648363,Trunk\n"+
	"Fitchburg/South Acton Line,0,17,North Leominster,42.540577,-71.739402,Trunk\n"+
	"Fitchburg/South Acton Line,0,18,Fitchburg,42.581739,-71.792750,Trunk\n"+
	"Fitchburg/South Acton Line,1,1,Fitchburg,42.581739,-71.792750,Trunk\n"+
	"Fitchburg/South Acton Line,1,2,North Leominster,42.540577,-71.739402,Trunk\n"+
	"Fitchburg/South Acton Line,1,3,Shirley,42.544726,-71.648363,Trunk\n"+
	"Fitchburg/South Acton Line,1,4,Ayer,42.560047,-71.590117,Trunk\n"+
	"Fitchburg/South Acton Line,1,5,Littleton / Rte 495,42.519236,-71.502643,Trunk\n"+
	"Fitchburg/South Acton Line,1,6,South Acton,42.461575,-71.455322,Trunk\n"+
	"Fitchburg/South Acton Line,1,7,West Concord,42.456372,-71.392371,Trunk\n"+
	"Fitchburg/South Acton Line,1,8,Concord,42.457147,-71.358051,Trunk\n"+
	"Fitchburg/South Acton Line,1,9,Lincoln,42.414229,-71.325344,Trunk\n"+
	"Fitchburg/South Acton Line,1,10,Silver Hill,42.395625,-71.302357,Trunk\n"+
	"Fitchburg/South Acton Line,1,11,Hastings,42.385755,-71.289203,Trunk\n"+
	"Fitchburg/South Acton Line,1,12,Kendal Green,42.378970,-71.282411,Trunk\n"+
	"Fitchburg/South Acton Line,1,13,Brandeis/ Roberts,42.361728,-71.260854,Trunk\n"+
	"Fitchburg/South Acton Line,1,14,Waltham,42.374424,-71.236595,Trunk\n"+
	"Fitchburg/South Acton Line,1,15,Waverley,42.387489,-71.189864,Trunk\n"+
	"Fitchburg/South Acton Line,1,16,Belmont,42.398420,-71.174499,Trunk\n"+
	"Fitchburg/South Acton Line,1,17,Porter Square,42.388353,-71.119159,Trunk\n"+
	"Fitchburg/South Acton Line,1,18,North Station,42.365551,-71.061251,Trunk\n"+
	"Framingham/Worcester Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Framingham/Worcester Line,0,2,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Framingham/Worcester Line,0,3,Yawkey,42.346796,-71.098937,Trunk\n"+
	"Framingham/Worcester Line,0,4,Newtonville,42.351603,-71.207338,Trunk\n"+
	"Framingham/Worcester Line,0,5,West Newton,42.348599,-71.229010,Trunk\n"+
	"Framingham/Worcester Line,0,6,Auburndale,42.346087,-71.246658,Trunk\n"+
	"Framingham/Worcester Line,0,7,Wellesley Farms,42.323608,-71.272288,Trunk\n"+
	"Framingham/Worcester Line,0,8,Wellesley Hills,42.310027,-71.276769,Trunk\n"+
	"Framingham/Worcester Line,0,9,Wellesley Square,42.296427,-71.294311,Trunk\n"+
	"Framingham/Worcester Line,0,10,Natick,42.285239,-71.347641,Trunk\n"+
	"Framingham/Worcester Line,0,11,West Natick,42.281855,-71.390548,Trunk\n"+
	"Framingham/Worcester Line,0,12,Framingham,42.276719,-71.416792,Trunk\n"+
	"Framingham/Worcester Line,0,13,Ashland,42.261694,-71.478813,Trunk\n"+
	"Framingham/Worcester Line,0,14,Southborough,42.267518,-71.523621,Trunk\n"+
	"Framingham/Worcester Line,0,15,Westborough,42.269184,-71.652005,Trunk\n"+
	"Framingham/Worcester Line,0,16,Grafton,42.246291,-71.684614,Trunk\n"+
	"Framingham/Worcester Line,0,17,Worcester / Union Station,42.261796,-71.793881,Trunk\n"+
	"Framingham/Worcester Line,1,1,Worcester / Union Station,42.261796,-71.793881,Trunk\n"+
	"Framingham/Worcester Line,1,2,Grafton,42.246291,-71.684614,Trunk\n"+
	"Framingham/Worcester Line,1,3,Westborough,42.269184,-71.652005,Trunk\n"+
	"Framingham/Worcester Line,1,4,Southborough,42.267518,-71.523621,Trunk\n"+
	"Framingham/Worcester Line,1,5,Ashland,42.261694,-71.478813,Trunk\n"+
	"Framingham/Worcester Line,1,6,Framingham,42.276719,-71.416792,Trunk\n"+
	"Framingham/Worcester Line,1,7,West Natick,42.281855,-71.390548,Trunk\n"+
	"Framingham/Worcester Line,1,8,Natick,42.285239,-71.347641,Trunk\n"+
	"Framingham/Worcester Line,1,9,Wellesley Square,42.296427,-71.294311,Trunk\n"+
	"Framingham/Worcester Line,1,10,Wellesley Hills,42.310027,-71.276769,Trunk\n"+
	"Framingham/Worcester Line,1,11,Wellesley Farms,42.323608,-71.272288,Trunk\n"+
	"Framingham/Worcester Line,1,12,Auburndale,42.346087,-71.246658,Trunk\n"+
	"Framingham/Worcester Line,1,13,West Newton,42.348599,-71.229010,Trunk\n"+
	"Framingham/Worcester Line,1,14,Newtonville,42.351603,-71.207338,Trunk\n"+
	"Framingham/Worcester Line,1,15,Yawkey,42.346796,-71.098937,Trunk\n"+
	"Framingham/Worcester Line,1,16,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Framingham/Worcester Line,1,17,South Station,42.352614,-71.055364,Trunk\n"+
	"Franklin Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Franklin Line,0,2,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Franklin Line,0,3,Ruggles,42.335545,-71.090524,Trunk\n"+
	"Franklin Line,0,4,Hyde Park,42.255121,-71.125022,Trunk\n"+
	"Franklin Line,0,5,Readville,42.237750,-71.132376,Trunk\n"+
	"Franklin Line,0,6,Endicott,42.232881,-71.160413,Trunk\n"+
	"Franklin Line,0,7,Dedham Corp Center,42.225896,-71.173806,Trunk\n"+
	"Franklin Line,0,8,Islington,42.220714,-71.183406,Trunk\n"+
	"Franklin Line,0,9,Norwood Depot,42.195668,-71.196784,Trunk\n"+
	"Franklin Line,0,10,Norwood Central,42.190776,-71.199748,Trunk\n"+
	"Franklin Line,0,11,Windsor Gardens,42.172192,-71.220704,Trunk\n"+
	"Franklin Line,0,12,Plimptonville,42.159123,-71.236125,Trunk\n"+
	"Franklin Line,0,13,Walpole,42.144192,-71.259016,Trunk\n"+
	"Franklin Line,0,14,Norfolk,42.120694,-71.325217,Trunk\n"+
	"Franklin Line,0,15,Franklin,42.083591,-71.396735,Trunk\n"+
	"Franklin Line,0,16,Forge Park / 495,42.090704,-71.430342,Trunk\n"+
	"Franklin Line,1,1,Forge Park / 495,42.090704,-71.430342,Trunk\n"+
	"Franklin Line,1,2,Franklin,42.083591,-71.396735,Trunk\n"+
	"Franklin Line,1,3,Norfolk,42.120694,-71.325217,Trunk\n"+
	"Franklin Line,1,4,Walpole,42.144192,-71.259016,Trunk\n"+
	"Franklin Line,1,5,Plimptonville,42.159123,-71.236125,Trunk\n"+
	"Franklin Line,1,6,Windsor Gardens,42.172192,-71.220704,Trunk\n"+
	"Franklin Line,1,7,Norwood Central,42.190776,-71.199748,Trunk\n"+
	"Franklin Line,1,8,Norwood Depot,42.195668,-71.196784,Trunk\n"+
	"Franklin Line,1,9,Islington,42.220714,-71.183406,Trunk\n"+
	"Franklin Line,1,10,Dedham Corp Center,42.225896,-71.173806,Trunk\n"+
	"Franklin Line,1,11,Endicott,42.232881,-71.160413,Trunk\n"+
	"Franklin Line,1,12,Readville,42.237750,-71.132376,Trunk\n"+
	"Franklin Line,1,13,Hyde Park,42.255121,-71.125022,Primary\n"+
	"Franklin Line,1,13,Fairmount,42.253638,-71.119270,Secondary\n"+
	"Franklin Line,1,14,Ruggles,42.335545,-71.090524,Primary\n"+
	"Franklin Line,1,14,Morton Street,42.280994,-71.085475,Secondary\n"+
	"Franklin Line,1,15,Uphams Corner,42.318670,-71.069072,Secondary\n"+
	"Franklin Line,1,15,Back Bay,42.347158,-71.075769,Primary\n"+
	"Franklin Line,1,16,South Station,42.352614,-71.055364,Trunk\n"+
	"Greenbush Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Greenbush Line,0,2,JFK/UMASS,42.321123,-71.052555,Trunk\n"+
	"Greenbush Line,0,3,Quincy Center,42.250862,-71.004843,Trunk\n"+
	"Greenbush Line,0,4,Weymouth Landing/ East Braintree,42.220800,-70.968200,Trunk\n"+
	"Greenbush Line,0,5,East Weymouth,42.219100,-70.921400,Trunk\n"+
	"Greenbush Line,0,6,West Hingham,42.236700,-70.903100,Trunk\n"+
	"Greenbush Line,0,7,Nantasket Junction,42.245200,-70.869800,Trunk\n"+
	"Greenbush Line,0,8,Cohasset,42.242400,-70.837000,Trunk\n"+
	"Greenbush Line,0,9,North Scituate,42.219700,-70.787700,Trunk\n"+
	"Greenbush Line,0,10,Greenbush,42.178100,-70.746200,Trunk\n"+
	"Greenbush Line,1,1,Greenbush,42.178100,-70.746200,Trunk\n"+
	"Greenbush Line,1,2,North Scituate,42.219700,-70.787700,Trunk\n"+
	"Greenbush Line,1,3,Cohasset,42.242400,-70.837000,Trunk\n"+
	"Greenbush Line,1,4,Nantasket Junction,42.245200,-70.869800,Trunk\n"+
	"Greenbush Line,1,5,West Hingham,42.236700,-70.903100,Trunk\n"+
	"Greenbush Line,1,6,East Weymouth,42.219100,-70.921400,Trunk\n"+
	"Greenbush Line,1,7,Weymouth Landing/ East Braintree,42.220800,-70.968200,Trunk\n"+
	"Greenbush Line,1,8,Quincy Center,42.250862,-71.004843,Trunk\n"+
	"Greenbush Line,1,9,JFK/UMASS,42.321123,-71.052555,Trunk\n"+
	"Greenbush Line,1,10,South Station,42.352614,-71.055364,Trunk\n"+
	"Haverhill Line,0,1,North Station,42.365551,-71.061251,Trunk\n"+
	"Haverhill Line,0,2,West Medford,42.421184,-71.132468,Secondary\n"+
	"Haverhill Line,0,2,Malden Center,42.426407,-71.074227,Primary\n"+
	"Haverhill Line,0,3,Wyoming Hill,42.452097,-71.069518,Primary\n"+
	"Haverhill Line,0,3,Wedgemere,42.445284,-71.140909,Secondary\n"+
	"Haverhill Line,0,4,Melrose Cedar Park,42.459128,-71.069448,Primary\n"+
	"Haverhill Line,0,4,Winchester Center,42.452650,-71.137041,Secondary\n"+
	"Haverhill Line,0,5,Anderson/ Woburn,42.518082,-71.138650,Secondary\n"+
	"Haverhill Line,0,5,Melrose Highlands,42.468793,-71.068270,Primary\n"+
	"Haverhill Line,0,6,Greenwood,42.483473,-71.067233,Primary\n"+
	"Haverhill Line,0,6,Wilmington,42.546368,-71.173569,Secondary\n"+
	"Haverhill Line,0,7,Wakefield,42.501811,-71.075000,Primary\n"+
	"Haverhill Line,0,8,Reading,42.521480,-71.107440,Primary\n"+
	"Haverhill Line,0,11,North Wilmington,42.568462,-71.159724,Primary\n"+
	"Haverhill Line,0,12,Ballardvale,42.626449,-71.159653,Trunk\n"+
	"Haverhill Line,0,13,Andover,42.657798,-71.144513,Trunk\n"+
	"Haverhill Line,0,14,Lawrence,42.700094,-71.159797,Trunk\n"+
	"Haverhill Line,0,15,Bradford,42.768899,-71.085998,Trunk\n"+
	"Haverhill Line,0,16,Haverhill,42.772684,-71.085962,Trunk\n"+
	"Haverhill Line,1,1,Haverhill,42.772684,-71.085962,Trunk\n"+
	"Haverhill Line,1,2,Bradford,42.768899,-71.085998,Trunk\n"+
	"Haverhill Line,1,3,Lawrence,42.700094,-71.159797,Trunk\n"+
	"Haverhill Line,1,4,Andover,42.657798,-71.144513,Trunk\n"+
	"Haverhill Line,1,5,Ballardvale,42.626449,-71.159653,Trunk\n"+
	"Haverhill Line,1,6,North Wilmington,42.568462,-71.159724,Trunk\n"+
	"Haverhill Line,1,7,Wilmington,42.546368,-71.173569,Secondary\n"+
	"Haverhill Line,1,8,Anderson/ Woburn,42.518082,-71.138650,Secondary\n"+
	"Haverhill Line,1,9,Winchester Center,42.452650,-71.137041,Secondary\n"+
	"Haverhill Line,1,9,Reading,42.521480,-71.107440,Primary\n"+
	"Haverhill Line,1,10,Wedgemere,42.445284,-71.140909,Secondary\n"+
	"Haverhill Line,1,10,Wakefield,42.501811,-71.075000,Primary\n"+
	"Haverhill Line,1,11,Greenwood,42.483473,-71.067233,Primary\n"+
	"Haverhill Line,1,11,West Medford,42.421184,-71.132468,Secondary\n"+
	"Haverhill Line,1,12,Melrose Highlands,42.468793,-71.068270,Primary\n"+
	"Haverhill Line,1,13,Melrose Cedar Park,42.459128,-71.069448,Primary\n"+
	"Haverhill Line,1,14,Wyoming Hill,42.452097,-71.069518,Primary\n"+
	"Haverhill Line,1,15,Malden Center,42.426407,-71.074227,Primary\n"+
	"Haverhill Line,1,16,North Station,42.365551,-71.061251,Trunk\n"+
	"Kingston/Plymouth Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Kingston/Plymouth Line,0,2,JFK/UMASS,42.321123,-71.052555,Trunk\n"+
	"Kingston/Plymouth Line,0,3,Quincy Center,42.250862,-71.004843,Trunk\n"+
	"Kingston/Plymouth Line,0,4,Braintree,42.208550,-71.000850,Trunk\n"+
	"Kingston/Plymouth Line,0,11,South Weymouth,42.153747,-70.952490,Trunk\n"+
	"Kingston/Plymouth Line,0,12,Abington,42.108034,-70.935296,Trunk\n"+
	"Kingston/Plymouth Line,0,13,Whitman,42.083563,-70.923204,Trunk\n"+
	"Kingston/Plymouth Line,0,14,Hanson,42.043262,-70.881553,Trunk\n"+
	"Kingston/Plymouth Line,0,15,Halifax,42.012867,-70.820832,Trunk\n"+
	"Kingston/Plymouth Line,0,17,Plymouth,41.981184,-70.692514,Secondary\n"+
	"Kingston/Plymouth Line,0,17,Kingston,41.978548,-70.720315,Primary\n"+
	"Kingston/Plymouth Line,1,2,Plymouth,41.981184,-70.692514,Secondary\n"+
	"Kingston/Plymouth Line,1,2,Kingston,41.978548,-70.720315,Primary\n"+
	"Kingston/Plymouth Line,1,3,Halifax,42.012867,-70.820832,Trunk\n"+
	"Kingston/Plymouth Line,1,4,Hanson,42.043262,-70.881553,Trunk\n"+
	"Kingston/Plymouth Line,1,5,Whitman,42.083563,-70.923204,Trunk\n"+
	"Kingston/Plymouth Line,1,6,Abington,42.108034,-70.935296,Trunk\n"+
	"Kingston/Plymouth Line,1,7,South Weymouth,42.153747,-70.952490,Trunk\n"+
	"Kingston/Plymouth Line,1,14,Braintree,42.208550,-71.000850,Trunk\n"+
	"Kingston/Plymouth Line,1,15,Quincy Center,42.250862,-71.004843,Trunk\n"+
	"Kingston/Plymouth Line,1,16,JFK/UMASS,42.321123,-71.052555,Trunk\n"+
	"Kingston/Plymouth Line,1,17,South Station,42.352614,-71.055364,Trunk\n"+
	"Lowell Line,0,1,North Station,42.365551,-71.061251,Trunk\n"+
	"Lowell Line,0,2,West Medford,42.421184,-71.132468,Trunk\n"+
	"Lowell Line,0,3,Wedgemere,42.445284,-71.140909,Trunk\n"+
	"Lowell Line,0,4,Winchester Center,42.452650,-71.137041,Trunk\n"+
	"Lowell Line,0,5,Mishawum,42.503595,-71.137511,Trunk\n"+
	"Lowell Line,0,6,Anderson/ Woburn,42.518082,-71.138650,Trunk\n"+
	"Lowell Line,0,7,Wilmington,42.546368,-71.173569,Trunk\n"+
	"Lowell Line,0,9,North Billerica,42.592881,-71.280869,Trunk\n"+
	"Lowell Line,0,10,Lowell,42.638402,-71.314916,Trunk\n"+
	"Lowell Line,1,1,Lowell,42.638402,-71.314916,Trunk\n"+
	"Lowell Line,1,2,North Billerica,42.592881,-71.280869,Trunk\n"+
	"Lowell Line,1,4,Wilmington,42.546368,-71.173569,Trunk\n"+
	"Lowell Line,1,5,Anderson/ Woburn,42.518082,-71.138650,Trunk\n"+
	"Lowell Line,1,6,Mishawum,42.503595,-71.137511,Trunk\n"+
	"Lowell Line,1,7,Winchester Center,42.452650,-71.137041,Trunk\n"+
	"Lowell Line,1,8,Wedgemere,42.445284,-71.140909,Trunk\n"+
	"Lowell Line,1,9,West Medford,42.421184,-71.132468,Trunk\n"+
	"Lowell Line,1,10,North Station,42.365551,-71.061251,Trunk\n"+
	"Middleborough/Lakeville Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Middleborough/Lakeville Line,0,2,JFK/UMASS,42.321123,-71.052555,Trunk\n"+
	"Middleborough/Lakeville Line,0,3,Quincy Center,42.250862,-71.004843,Trunk\n"+
	"Middleborough/Lakeville Line,0,4,Braintree,42.208550,-71.000850,Trunk\n"+
	"Middleborough/Lakeville Line,0,5,Holbrook/ Randolph,42.155314,-71.027518,Trunk\n"+
	"Middleborough/Lakeville Line,0,6,Montello,42.106047,-71.021078,Trunk\n"+
	"Middleborough/Lakeville Line,0,7,Brockton,42.085720,-71.016860,Trunk\n"+
	"Middleborough/Lakeville Line,0,8,Campello,42.060038,-71.012460,Trunk\n"+
	"Middleborough/Lakeville Line,0,9,Bridgewater,41.986355,-70.966625,Trunk\n"+
	"Middleborough/Lakeville Line,0,10,Middleboro/ Lakeville,41.878210,-70.918444,Trunk\n"+
	"Middleborough/Lakeville Line,1,8,Middleboro/ Lakeville,41.878210,-70.918444,Trunk\n"+
	"Middleborough/Lakeville Line,1,9,Bridgewater,41.986355,-70.966625,Trunk\n"+
	"Middleborough/Lakeville Line,1,10,Campello,42.060038,-71.012460,Trunk\n"+
	"Middleborough/Lakeville Line,1,11,Brockton,42.085720,-71.016860,Trunk\n"+
	"Middleborough/Lakeville Line,1,12,Montello,42.106047,-71.021078,Trunk\n"+
	"Middleborough/Lakeville Line,1,13,Holbrook/ Randolph,42.155314,-71.027518,Trunk\n"+
	"Middleborough/Lakeville Line,1,14,Braintree,42.208550,-71.000850,Trunk\n"+
	"Middleborough/Lakeville Line,1,15,Quincy Center,42.250862,-71.004843,Trunk\n"+
	"Middleborough/Lakeville Line,1,16,JFK/UMASS,42.321123,-71.052555,Trunk\n"+
	"Middleborough/Lakeville Line,1,17,South Station,42.352614,-71.055364,Trunk\n"+
	"Needham Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Needham Line,0,2,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Needham Line,0,3,Ruggles,42.335545,-71.090524,Trunk\n"+
	"Needham Line,0,4,Forest Hills,42.300023,-71.113377,Trunk\n"+
	"Needham Line,0,5,Roslindale Village,42.287206,-71.129610,Trunk\n"+
	"Needham Line,0,6,Bellevue,42.287138,-71.146060,Trunk\n"+
	"Needham Line,0,7,Highland,42.284869,-71.154700,Trunk\n"+
	"Needham Line,0,8,West Roxbury,42.281600,-71.159932,Trunk\n"+
	"Needham Line,0,9,Hersey,42.275842,-71.214853,Trunk\n"+
	"Needham Line,0,10,Needham Junction,42.273327,-71.238007,Trunk\n"+
	"Needham Line,0,11,Needham Center,42.280274,-71.238089,Trunk\n"+
	"Needham Line,0,12,Needham Heights,42.293139,-71.235087,Trunk\n"+
	"Needham Line,1,1,Needham Heights,42.293139,-71.235087,Trunk\n"+
	"Needham Line,1,2,Needham Center,42.280274,-71.238089,Trunk\n"+
	"Needham Line,1,3,Needham Junction,42.273327,-71.238007,Trunk\n"+
	"Needham Line,1,4,Hersey,42.275842,-71.214853,Trunk\n"+
	"Needham Line,1,5,West Roxbury,42.281600,-71.159932,Trunk\n"+
	"Needham Line,1,6,Highland,42.284869,-71.154700,Trunk\n"+
	"Needham Line,1,7,Bellevue,42.287138,-71.146060,Trunk\n"+
	"Needham Line,1,8,Roslindale Village,42.287206,-71.129610,Trunk\n"+
	"Needham Line,1,9,Forest Hills,42.300023,-71.113377,Trunk\n"+
	"Needham Line,1,10,Ruggles,42.335545,-71.090524,Trunk\n"+
	"Needham Line,1,11,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Needham Line,1,12,South Station,42.352614,-71.055364,Trunk\n"+
	"Newburyport/Rockport Line,0,1,North Station,42.365551,-71.061251,Trunk\n"+
	"Newburyport/Rockport Line,0,2,Chelsea,42.395661,-71.034826,Trunk\n"+
	"Newburyport/Rockport Line,0,3,River Works,42.453804,-70.975698,Trunk\n"+
	"Newburyport/Rockport Line,0,4,Lynn,42.462293,-70.947794,Trunk\n"+
	"Newburyport/Rockport Line,0,5,Swampscott,42.473739,-70.922036,Trunk\n"+
	"Newburyport/Rockport Line,0,6,Salem,42.523927,-70.898903,Trunk\n"+
	"Newburyport/Rockport Line,0,7,Beverly,42.546907,-70.885168,Trunk\n"+
	"Newburyport/Rockport Line,0,8,North Beverly,42.582471,-70.884501,Trunk\n"+
	"Newburyport/Rockport Line,0,9,Hamilton/ Wenham,42.610756,-70.874005,Trunk\n"+
	"Newburyport/Rockport Line,0,10,Ipswich,42.678355,-70.840024,Trunk\n"+
	"Newburyport/Rockport Line,0,11,Rowley,42.725351,-70.859436,Trunk\n"+
	"Newburyport/Rockport Line,0,12,Newburyport,42.800292,-70.880262,Trunk\n"+
	"Newburyport/Rockport Line,0,13,Montserrat,42.561483,-70.870035,Trunk\n"+
	"Newburyport/Rockport Line,0,14,Prides Crossing,42.559513,-70.824813,Trunk\n"+
	"Newburyport/Rockport Line,0,15,Beverly Farms,42.561403,-70.812745,Trunk\n"+
	"Newburyport/Rockport Line,0,16,Manchester,42.573570,-70.770473,Trunk\n"+
	"Newburyport/Rockport Line,0,17,West Gloucester,42.610928,-70.706456,Trunk\n"+
	"Newburyport/Rockport Line,0,18,Gloucester,42.616069,-70.668767,Trunk\n"+
	"Newburyport/Rockport Line,0,19,Rockport,42.656173,-70.625616,Trunk\n"+
	"Newburyport/Rockport Line,1,1,Rockport,42.656173,-70.625616,Trunk\n"+
	"Newburyport/Rockport Line,1,2,Gloucester,42.616069,-70.668767,Trunk\n"+
	"Newburyport/Rockport Line,1,3,West Gloucester,42.610928,-70.706456,Trunk\n"+
	"Newburyport/Rockport Line,1,4,Manchester,42.573570,-70.770473,Trunk\n"+
	"Newburyport/Rockport Line,1,5,Beverly Farms,42.561403,-70.812745,Trunk\n"+
	"Newburyport/Rockport Line,1,6,Prides Crossing,42.559513,-70.824813,Trunk\n"+
	"Newburyport/Rockport Line,1,7,Montserrat,42.561483,-70.870035,Trunk\n"+
	"Newburyport/Rockport Line,1,8,Newburyport,42.800292,-70.880262,Trunk\n"+
	"Newburyport/Rockport Line,1,9,Rowley,42.725351,-70.859436,Trunk\n"+
	"Newburyport/Rockport Line,1,10,Ipswich,42.678355,-70.840024,Trunk\n"+
	"Newburyport/Rockport Line,1,11,Hamilton/ Wenham,42.610756,-70.874005,Trunk\n"+
	"Newburyport/Rockport Line,1,12,North Beverly,42.582471,-70.884501,Trunk\n"+
	"Newburyport/Rockport Line,1,13,Beverly,42.546907,-70.885168,Trunk\n"+
	"Newburyport/Rockport Line,1,14,Salem,42.523927,-70.898903,Trunk\n"+
	"Newburyport/Rockport Line,1,15,Swampscott,42.473739,-70.922036,Trunk\n"+
	"Newburyport/Rockport Line,1,16,Lynn,42.462293,-70.947794,Trunk\n"+
	"Newburyport/Rockport Line,1,17,River Works,42.453804,-70.975698,Trunk\n"+
	"Newburyport/Rockport Line,1,18,Chelsea,42.395661,-71.034826,Trunk\n"+
	"Newburyport/Rockport Line,1,19,North Station,42.365551,-71.061251,Trunk\n"+
	"Providence/Stoughton Line,0,1,South Station,42.352614,-71.055364,Trunk\n"+
	"Providence/Stoughton Line,0,2,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Providence/Stoughton Line,0,3,Ruggles,42.335545,-71.090524,Trunk\n"+
	"Providence/Stoughton Line,0,4,Hyde Park,42.255121,-71.125022,Trunk\n"+
	"Providence/Stoughton Line,0,5,Route 128,42.209884,-71.147100,Trunk\n"+
	"Providence/Stoughton Line,0,6,Canton Junction,42.163423,-71.153374,Trunk\n"+
	"Providence/Stoughton Line,0,7,Canton Center,42.156769,-71.145530,Secondary\n"+
	"Providence/Stoughton Line,0,8,Stoughton,42.123818,-71.103090,Secondary\n"+
	"Providence/Stoughton Line,0,9,Sharon,42.124804,-71.183213,Primary\n"+
	"Providence/Stoughton Line,0,10,Mansfield,42.032734,-71.219318,Primary\n"+
	"Providence/Stoughton Line,0,11,Attleboro,41.942097,-71.284897,Primary\n"+
	"Providence/Stoughton Line,0,12,South Attleboro,41.897943,-71.354621,Primary\n"+
	"Providence/Stoughton Line,0,13,Providence,41.829641,-71.413332,Primary\n"+
	"Providence/Stoughton Line,0,14,TF Green Airport,41.726599,-71.442453,Primary\n"+
	"Providence/Stoughton Line,1,0,TF Green Airport,41.726599,-71.442453,Primary\n"+
	"Providence/Stoughton Line,1,1,Providence,41.829641,-71.413332,Primary\n"+
	"Providence/Stoughton Line,1,2,South Attleboro,41.897943,-71.354621,Primary\n"+
	"Providence/Stoughton Line,1,3,Attleboro,41.942097,-71.284897,Primary\n"+
	"Providence/Stoughton Line,1,4,Mansfield,42.032734,-71.219318,Primary\n"+
	"Providence/Stoughton Line,1,5,Sharon,42.124804,-71.183213,Primary\n"+
	"Providence/Stoughton Line,1,6,Stoughton,42.123818,-71.103090,Secondary\n"+
	"Providence/Stoughton Line,1,7,Canton Center,42.156769,-71.145530,Secondary\n"+
	"Providence/Stoughton Line,1,8,Canton Junction,42.163423,-71.153374,Trunk\n"+
	"Providence/Stoughton Line,1,9,Route 128,42.209884,-71.147100,Trunk\n"+
	"Providence/Stoughton Line,1,10,Hyde Park,42.255121,-71.125022,Trunk\n"+
	"Providence/Stoughton Line,1,11,Ruggles,42.335545,-71.090524,Trunk\n"+
	"Providence/Stoughton Line,1,12,Back Bay,42.347158,-71.075769,Trunk\n"+
	"Providence/Stoughton Line,1,13,South Station,42.352614,-71.055364,Trunk\n";
	
	
	private final MyHashMap<String, Integer> indexes = new MyHashMap<String, Integer>();
	
	public CommuterRailRouteConfigParser(Directions directions, RouteConfig oldRouteConfig,
			CommuterRailTransitSource source) 
	{
		this.directions = directions;
		this.source = source;
	}

	public void writeToDatabase(RoutePool routeMapping, boolean wipe, UpdateAsyncTask task, boolean silent) throws IOException
	{
		routeMapping.writeToDatabase(map, wipe, task, silent);
		directions.writeToDatabase(wipe);
	}

	private void populateStops(Reader inputStreamReader) throws IOException
	{
		BufferedReader reader = new BufferedReader(inputStreamReader, 2048);
		String[] definitions = reader.readLine().split(",");
		
		for (int i = 0; i < definitions.length; i++)
		{
			indexes.put(definitions[i], i);
		}
		
		
		/**route to a mapping of
	     direction + branch to a mapping of
	       platform order numbers to stops
		 * 
		 */
		MyHashMap<String, MyHashMap<String, MyTreeMap<Short, StopLocation>>> orderedStations =
			new MyHashMap<String, MyHashMap<String, MyTreeMap<Short, StopLocation>>>();

		MyHashMap<String, String> routeKeysToTitles = source.getRouteKeysToTitles();
		MyHashMap<String, String> routeTitlesToKeys = new MyHashMap<String, String>();
		for (String key : routeKeysToTitles.keySet())
		{
			String title = routeKeysToTitles.get(key);
			routeTitlesToKeys.put(title, key);
		}
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			String[] array = line.split(",");
			String routeTitle = array[indexes.get("route_long_name")];
			if (routeTitle.endsWith(" Line"))
			{
				routeTitle = routeTitle.substring(0, routeTitle.length() - 5);
			}
			String routeKey = routeTitlesToKeys.get(routeTitle);
			RouteConfig route = map.get(routeKey);
			
			float lat = QuickParseUtil.parseFloat(array[indexes.get("stop_lat")]);
			float lon = QuickParseUtil.parseFloat(array[indexes.get("stop_lon")]);
			String stopTitle = array[indexes.get("stop_id")];
			String direction = array[indexes.get("direction_id")];
			String stopTag = CommuterRailTransitSource.stopTagPrefix + stopTitle;
			short platformOrder = Short.parseShort(array[indexes.get("stop_sequence")]);
			String branch = array[indexes.get("Branch")];
			
			StopLocation stopLocation = route.getStop(stopTag);
			if (stopLocation == null)
			{
				stopLocation = new CommuterRailStopLocation(lat, lon, source.getDrawables(), stopTag, stopTitle, platformOrder, branch);
				route.addStop(stopTag, stopLocation);
			}
			
			stopLocation.addRoute(routeKey);
			
			route.addStop(stopTag, stopLocation);
			
			MyHashMap<String, MyTreeMap<Short, StopLocation>> innerMapping = orderedStations.get(routeKey);
			if (innerMapping == null)
			{
				innerMapping = new MyHashMap<String, MyTreeMap<Short, StopLocation>>();
				orderedStations.put(routeKey, innerMapping);
			}
			
			//mapping of (direction plus branch plus platform order) to a stop
			//for example, key is NBAshmont3 for fields corner
			
			String combinedDirectionBranch = createDirectionHash(direction, branch);
			MyTreeMap<Short, StopLocation> innerInnerMapping = innerMapping.get(combinedDirectionBranch);
			if (innerInnerMapping == null)
			{
				innerInnerMapping = new MyTreeMap<Short, StopLocation>();
				innerMapping.put(combinedDirectionBranch, innerInnerMapping);
			}
			
			innerInnerMapping.put(platformOrder, stopLocation);
		}
		
		//TODO: workarounds
		
		//path
		for (String route : orderedStations.keySet())
		{
			
			MyHashMap<String, MyTreeMap<Short, StopLocation>> innerMapping = orderedStations.get(route);

			
			for (String directionHash : innerMapping.keySet())
			{
				MyTreeMap<Short, StopLocation> stations = innerMapping.get(directionHash);

				ArrayList<Float> floats = new ArrayList<Float>();
				for (Short platformOrder : stations.keySet())
				{
					StopLocation station = stations.get(platformOrder);

					floats.add((float)station.getLatitudeAsDegrees());
					floats.add((float)station.getLongitudeAsDegrees());
				}
				
				Path path = new Path(floats);
				
				map.get(route).addPaths(path);
			}
			
			//match other branches to main branch if possible
			HashSet<String> alreadyHandledDirections = new HashSet<String>();
			
			final String trunkBranch = "Trunk";
			for (String directionHash : innerMapping.keySet())
			{
				String[] array = directionHash.split("\\|");
				String direction = array[0];
				String branch = array[1];
				
				if (alreadyHandledDirections.contains(direction))
				{
					continue;
				}
				
				if (trunkBranch.equals(branch))
				{
					continue;
				}
				
				MyTreeMap<Short, StopLocation> branchInnerMapping = innerMapping.get(directionHash);
				String trunkDirectionHash = createDirectionHash(direction, trunkBranch);
				MyTreeMap<Short, StopLocation> trunkInnerMapping = innerMapping.get(trunkDirectionHash);
				
				int minBranchOrder = -1;
				for (Short order : branchInnerMapping.keySet())
				{
					if (minBranchOrder == -1)
					{
						minBranchOrder = order;
					}
					else
					{
						minBranchOrder = Math.min(order, minBranchOrder);
					}
				}
				
				int maxTrunkOrder = 0;
				for (Short order : trunkInnerMapping.keySet())
				{
					if (order < minBranchOrder)
					{
						maxTrunkOrder = Math.max(order, maxTrunkOrder);
					}
				}
				
				ArrayList<Float> points = new ArrayList<Float>();
				
				StopLocation branchStop = branchInnerMapping.get((short)minBranchOrder);
				StopLocation trunkStop = trunkInnerMapping.get((short)maxTrunkOrder);
				
				if (trunkStop != null && branchStop != null)
				{
					points.add(trunkStop.getLatitudeAsDegrees());
					points.add(trunkStop.getLongitudeAsDegrees());
					points.add(branchStop.getLatitudeAsDegrees());
					points.add(branchStop.getLongitudeAsDegrees());

					Path path = new Path(points);
					map.get(route).addPaths(path);
				}
			}
		}

	}
	
	private static String createDirectionHash(String direction, String branch)
	{
		return direction + "|" + branch;
	}
	
	public void runParse(Reader stream) throws IOException
	{
		MyHashMap<String, String> routeKeysToTitles = source.getRouteKeysToTitles();
		for (String route : source.getRoutes())
		{
			String routeTitle = routeKeysToTitles.get(route);
			map.put(route, new RouteConfig(route, routeTitle, 0, 0, source));
		}

		populateStops(stream);
		
	}
	
}
