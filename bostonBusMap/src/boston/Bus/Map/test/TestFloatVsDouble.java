package boston.Bus.Map.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.test.AndroidTestCase;
import android.util.Log;
import boston.Bus.Map.math.Geometry;
import boston.Bus.Map.util.Constants;

import junit.framework.TestCase;

public class TestFloatVsDouble extends AndroidTestCase {
	private final ArrayList<String> titles = new ArrayList<String>();
	private final ArrayList<Float> lats = new ArrayList<Float>();
	private final ArrayList<Float> lons = new ArrayList<Float>();
	
	private final ArrayList<Place> places = new ArrayList<Place>();
	
	public void testBasic()
	{
		addTitle("Dudley Station");
		addLat(42.3295399);
		addLon(-71.08398);
		addTitle("Malcolm X Blvd @ Shawmut Ave");
		addLat(42.3296799);
		addLon(-71.0861299);
		addTitle("Malcolm X Blvd @ O'Bryant HS");
		addLat(42.33068);
		addLon(-71.0882);
		addTitle("Malcolm X Blvd @ Madison Park HS");
		addLat(42.3311399);
		addLon(-71.0900599);
		addTitle("Malcolm X Blvd @ Tremont St");
		addLat(42.33135);
		addLon(-71.09331);
		addTitle("Tremont St opp Roxbury Crossing Sta");
		addLat(42.3313999);
		addLon(-71.09545);
		addTitle("Tremont St @ Parker St");
		addLat(42.3318);
		addLon(-71.09705);
		addTitle("Tremont St @ Tobin Community Center");
		addLat(42.3321199);
		addLon(-71.0985899);
		addTitle("Tremont St @ Mission Church");
		addLat(42.3325399);
		addLon(-71.1000799);
		addTitle("Tremont St @ Worthington St");
		addLat(42.3334399);
		addLon(-71.10252);
		addTitle("Tremont St @ Huntington Ave");
		addLat(42.33403);
		addLon(-71.10429);
		addTitle("Huntington Ave @ Fenwood Rd");
		addLat(42.3338499);
		addLon(-71.10556);
		addTitle("835 Huntington Ave opp Parker Hill Ave");
		addLat(42.3333199);
		addLon(-71.10957);
		addTitle("Huntington Ave @ Riverway");
		addLat(42.3320099);
		addLon(-71.1126);
		addTitle("Washington St @ Pearl St");
		addLat(42.3318899);
		addLon(-71.1166099);
		addTitle("Harvard St @ Kent St");
		addLat(42.33326);
		addLon(-71.11867);
		addTitle("Harvard St @ Linden St");
		addLat(42.3347599);
		addLon(-71.11922);
		addTitle("Harvard St @ Aspinwall St");
		addLat(42.3360999);
		addLon(-71.1206099);
		addTitle("Harvard St opp Auburn St");
		addLat(42.33725);
		addLon(-71.12138);
		addTitle("Harvard St opp Vernon St");
		addLat(42.33823);
		addLon(-71.1213099);
		addTitle("Harvard St @ Beacon St");
		addLat(42.3419099);
		addLon(-71.12114);
		addTitle("Harvard St @ Babcock St");
		addLat(42.3434699);
		addLon(-71.1233699);
		addTitle("Harvard St @ Beals St");
		addLat(42.3447);
		addLon(-71.12569);
		addTitle("Harvard St @ Coolidge St");
		addLat(42.34579);
		addLon(-71.12757);
		addTitle("Harvard St opp Verndale St");
		addLat(42.34844);
		addLon(-71.12942);
		addTitle("Harvard Ave @ Commonwealth Ave");
		addLat(42.3500899);
		addLon(-71.13075);
		addTitle("Harvard Ave @ Brighton Ave");
		addLat(42.35284);
		addLon(-71.13197);
		addTitle("Brighton Ave opp Quint St");
		addLat(42.35334);
		addLon(-71.13387);
		addTitle("Brighton Ave @ Craftsman St");
		addLat(42.3535699);
		addLon(-71.13593);
		addTitle("Cambridge St @ Craftsman St");
		addLat(42.3539299);
		addLon(-71.13637);
		addTitle("Cambridge St @ Harvard Ave");
		addLat(42.3552499);
		addLon(-71.13318);
		addTitle("Cambridge St @ Linden St");
		addLat(42.3559399);
		addLon(-71.13145);
		addTitle("N Harvard St @ Empire St");
		addLat(42.3585299);
		addLon(-71.12674);
		addTitle("N Harvard St @ Oxford St");
		addLat(42.3601899);
		addLon(-71.12857);
		addTitle("N Harvard St @ Kingsley St");
		addLat(42.3618899);
		addLon(-71.13033);
		addTitle("N Harvard St @ Western Ave");
		addLat(42.3635899);
		addLon(-71.12914);
		addTitle("Opp 175 N Harvard");
		addLat(42.3643699);
		addLon(-71.12807);
		addTitle("N Harvard St opp Harvard Stadium Gate 2");
		addLat(42.3670799);
		addLon(-71.12482);
		addTitle("JFK St @ Eliot St");
		addLat(42.3715199);
		addLon(-71.12097);
		addTitle("Massachusetts Ave @ Johnston Gate");
		addLat(42.3751299);
		addLon(-71.11851);
		addTitle("Harvard Sq @ Garden St - Dawes Island");
		addLat(42.3752999);
		addLon(-71.11924);
		addTitle("Cambridge St @ N Beacon St");
		addLat(42.3534099);
		addLon(-71.13802);
		addTitle("Cambridge St @ Gordon St");
		addLat(42.35239);
		addLon(-71.1408899);
		addTitle("Cambridge St opp Eleanor St");
		addLat(42.3516899);
		addLon(-71.1429);
		addTitle("Cambridge St @ Dustin St");
		addLat(42.35086);
		addLon(-71.1455399);
		addTitle("Cambridge St @ Sparhawk St");
		addLat(42.3501199);
		addLon(-71.14735);
		addTitle("Cambridge St @ Elko St");
		addLat(42.3494499);
		addLon(-71.1491499);
		addTitle("7 Winship St");
		addLat(42.34881);
		addLon(-71.1507599);
		addTitle("Winship St @ Union St");
		addLat(42.34619);
		addLon(-71.1524499);
		addTitle("Winship St @ Union St");
		addLat(42.3461999);
		addLon(-71.15231);
		addTitle("Brighton Ave @ Craftsman St");
		addLat(42.3535699);
		addLon(-71.13593);
		addTitle("Harvard Sq @ Garden St - Dawes Island");
		addLat(42.3752999);
		addLon(-71.11924);
		addTitle("Eliot St @ Bennett St");
		addLat(42.37238);
		addLon(-71.1218299);
		addTitle("JFK St @ Eliot St");
		addLat(42.37132);
		addLon(-71.1212699);
		addTitle("N Harvard St @ Gate 2 Harvard Stadium");
		addLat(42.3669499);
		addLon(-71.12511);
		addTitle("Opp 175 N Harvard St");
		addLat(42.3643899);
		addLon(-71.12822);
		addTitle("N Harvard St @ Western Ave");
		addLat(42.36366);
		addLon(-71.1292499);
		addTitle("N Harvard St @ Franklin St");
		addLat(42.3618899);
		addLon(-71.13049);
		addTitle("N Harvard St @ Coolidge St");
		addLat(42.3603899);
		addLon(-71.12901);
		addTitle("N Harvard St @  Hooker St");
		addLat(42.3587399);
		addLon(-71.12701);
		addTitle("Cambridge St @ N Harvard St");
		addLat(42.3579399);
		addLon(-71.1267);
		addTitle("Cambridge St opp Linden St");
		addLat(42.3565799);
		addLon(-71.13039);
		addTitle("Cambridge St @ Franklin St");
		addLat(42.3555999);
		addLon(-71.13267);
		addTitle("Brighton Ave @ Cambridge St");
		addLat(42.35335);
		addLon(-71.1366899);
		addTitle("Brighton Ave @ Allston St");
		addLat(42.3531699);
		addLon(-71.1345499);
		addTitle("Brighton Ave @ Harvard Ave");
		addLat(42.3528499);
		addLon(-71.13219);
		addTitle("Harvard Ave @ Commonwealth Ave");
		addLat(42.3505499);
		addLon(-71.1311799);
		addTitle("Harvard St @ Verndale St");
		addLat(42.3485699);
		addLon(-71.1296199);
		addTitle("Harvard St @ Coolidge St");
		addLat(42.3458899);
		addLon(-71.12781);
		addTitle("Harvard St @ Williams St");
		addLat(42.34446);
		addLon(-71.1254);
		addTitle("322 Harvard St");
		addLat(42.3432499);
		addLon(-71.1231499);
		addTitle("Harvard St @ Beacon St");
		addLat(42.34226);
		addLon(-71.1214699);
		addTitle("Harvard St @ Marion St");
		addLat(42.3398199);
		addLon(-71.12096);
		addTitle("Harvard St @ Harris St");
		addLat(42.3379099);
		addLon(-71.1215099);
		addTitle("Harvard St @ School St");
		addLat(42.33599);
		addLon(-71.12064);
		addTitle("Harvard St @ Pierce St");
		addLat(42.33445);
		addLon(-71.1191899);
		addTitle("Harvard St @ Washington St");
		addLat(42.3329699);
		addLon(-71.1187699);
		addTitle("Washington St @ Walnut");
		addLat(42.3315799);
		addLon(-71.11616);
		addTitle("Huntington Ave @ Jamaicaway");
		addLat(42.33175);
		addLon(-71.1125299);
		addTitle("Huntington Ave @ Parker Hill Ave");
		addLat(42.33309);
		addLon(-71.10968);
		addTitle("Huntington Ave @ Mission St");
		addLat(42.33318);
		addLon(-71.10718);
		addTitle("Huntington Ave opp Fenwood Rd");
		addLat(42.3342299);
		addLon(-71.10461);
		addTitle("Tremont St opp Wigglesworth St");
		addLat(42.33374);
		addLon(-71.10384);
		addTitle("Tremont St @ Whitney St");
		addLat(42.3331899);
		addLon(-71.1021599);
		addTitle("Tremont St @ Carmel St");
		addLat(42.3324599);
		addLon(-71.1001599);
		addTitle("Tremont St @ Burney St");
		addLat(42.3321299);
		addLon(-71.0990099);
		addTitle("Tremont St @ Parker St");
		addLat(42.3317199);
		addLon(-71.0973);
		addTitle("Tremont St @ Columbus Ave");
		addLat(42.3314);
		addLon(-71.09545);
		addTitle("Malcolm X Blvd @ King St");
		addLat(42.3312);
		addLon(-71.09269);
		addTitle("Malcolm X Blvd opp Madison Park HS");
		addLat(42.3309799);
		addLon(-71.0898599);
		addTitle("Malcolm X Blvd opp O'Bryant HS");
		addLat(42.3305999);
		addLon(-71.0884199);
		addTitle("Malcolm X Blvd @ Shawmut Ave");
		addLat(42.3294799);
		addLon(-71.0861999);
		addTitle("Dudley Station");
		addLat(42.3295399);
		addLon(-71.08398);
		addTitle("Cambridge St @ Warren St");
		addLat(42.3500699);
		addLon(-71.14717);
		addTitle("Cambridge St opp Dustin St");
		addLat(42.3506899);
		addLon(-71.14569);
		addTitle("Cambridge St @ Eleanor St");
		addLat(42.3515499);
		addLon(-71.1431);
		addTitle("Cambridge St @ Gordon St");
		addLat(42.3524499);
		addLon(-71.14033);
		addTitle("Cambridge St @ Barrows St");
		addLat(42.35309);
		addLon(-71.1384299);
		
		for (int i = 0; i < lons.size(); i++)
		{
			Place place = new Place(i, i, titles.get(i), lats.get(i), lons.get(i));
			places.add(place);
		}
		
		final int centerLatInt = 42335845;
		final int centerLonInt = -71118906;
		
		Comparator<Place> floatMathComparator = new Comparator<TestFloatVsDouble.Place>() {
			
			@Override
			public int compare(Place place1, Place place2) {
				float centerLat = centerLatInt * Constants.InvE6;
				centerLat *= Geometry.degreesToRadians;
				float centerLon = centerLonInt * Constants.InvE6;
				centerLon *= Geometry.degreesToRadians;
				
				float distance1 = Geometry.computeCompareDistanceFloat(centerLat, centerLon, (float)(place1.lat * Geometry.degreesToRadians), (float)(place1.lon * Geometry.degreesToRadians));
				float distance2 = Geometry.computeCompareDistanceFloat(centerLat, centerLon, (float)(place2.lat * Geometry.degreesToRadians), (float)(place2.lon * Geometry.degreesToRadians));
				
				return Float.compare(distance1, distance2);
			}
		};
		
		Comparator<Place> mathComparator = new Comparator<TestFloatVsDouble.Place>() {
			
			@Override
			public int compare(Place place1, Place place2) {
				float centerLat = centerLatInt / (float)Constants.E6;
				centerLat *= Math.PI / 180.0;
				float centerLon = centerLonInt / (float)Constants.E6;
				centerLon *= Math.PI / 180.0;
				float distance1 = Geometry.computeCompareDistance(centerLat, centerLon, (float)(place1.lat * Math.PI / 180.0), (float)(place1.lon * Math.PI / 180.0));
				float distance2 = Geometry.computeCompareDistance(centerLat, centerLon, (float)(place2.lat * Math.PI / 180.0), (float)(place2.lon * Math.PI / 180.0));
				
				return Float.compare(distance1, distance2);
			}
		};
		
		System.out.println("Before sorting: ");
		orderPlaces();
		printPlaces();
		print("");
		print("");
		print("");
		print("Sorting using Math: ");
		Collections.sort(places, mathComparator);
		orderPlaces();
		printPlaces();
		
		print("");
		print("");
		print("");
		print("Sorting using FloatMath: ");
		Collections.sort(places, floatMathComparator);
		printPlaces();
		
		print("");
		print("");
		print("");
		boolean failed = false;
		for (int i = 0; i < places.size(); i++)
		{
			if (i != places.get(i).sortedIndex)
			{
				print("FAIL FAIL FAIL FAIL at " + i);
				failed = true;
			}
		}
		
		print("Done, fail=" + failed);
	}

	
	
	private void orderPlaces() {
		for (int i = 0; i < places.size(); i++)
		{
			places.get(i).sortedIndex = i;
		}
	}



	private void print(String string) {
		Log.v("BostonBusMap", string);
	}



	private void printPlaces() {
		for (int i = 0; i < places.size(); i++)
		{
			Place place = places.get(i);
			print(place.index + ", " + place.sortedIndex + ", " + place.title + ", " + place.lat + ", " + place.lon);
		}
	}



	private void addLon(double d) {
		lons.add((float)d);
	}

	private void addLat(double d) {
		lats.add((float)d);
	}

	private void addTitle(String title) {
		titles.add(title);
	}
	
	private class Place
	{
		public final int index;
		public int sortedIndex;
		public final float lat;
		public final float lon;
		public final String title;
		
		public Place(int index, int sortedIndex, String title, float lat, float lon)
		{
			this.index = index;
			this.sortedIndex = sortedIndex;
			this.lat = lat;
			this.lon = lon;
			this.title = title;
		}
	}
}
