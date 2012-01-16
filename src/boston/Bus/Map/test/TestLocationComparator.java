package boston.Bus.Map.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.LocationComparator;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.math.Geometry;
import junit.framework.TestCase;

public class TestLocationComparator extends TestCase
{
	public void testBasic()
	{
		float x = 3;
		float y = 4;
		
		ArrayList<Location> list = new ArrayList<Location>();
		
		list.add(new FakeLocation(1, -8, -9));
		list.add(new FakeLocation(-2, 8, 9));
		list.add(new FakeLocation(5, -8, -9));
		list.add(new FakeLocation(-4, 8, 9));
		list.add(new FakeLocation(-5, -8, -9));
		list.add(new FakeLocation(2, 8, 9));
		list.add(new FakeLocation(3, 8, 9));
		list.add(new FakeLocation(-1, -8, -9));
		
		Collections.sort(list, new LocationComparator(x, y));
		
		assertEquals(-4, list.get(0).getId());
		assertEquals(-2, list.get(1).getId());
		assertEquals(2, list.get(2).getId());
		assertEquals(3, list.get(3).getId());
		assertEquals(-5, list.get(4).getId());
		assertEquals(-1, list.get(5).getId());
		assertEquals(1, list.get(6).getId());
		assertEquals(5, list.get(7).getId());
	}
	
	private class FakeLocation implements Location
	{
		private final int id;
		private final float latAsDeg;
		private final float lonAsDeg;
		
		
		public FakeLocation(int id, float latAsDeg, float lonAsDeg)
		{
			this.id = id;
			this.latAsDeg = latAsDeg;
			this.lonAsDeg = lonAsDeg;
		}
		
		@Override
		public int getId() {
			return id;
		}

		@Override
		public boolean hasHeading() {
			return false;
		}

		@Override
		public int getHeading() {
			return 0;
		}

		@Override
		public Drawable getDrawable(Context context, boolean shadow,
				boolean isSelected) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public float getLatitudeAsDegrees() {
			return latAsDeg;
		}

		@Override
		public float getLongitudeAsDegrees() {
			return lonAsDeg;
		}

		@Override
		public float distanceFrom(double centerLatitudeAsRadians,
				double centerLongitudeAsRadians) {
			return Geometry.computeCompareDistance(centerLatitudeAsRadians, centerLongitudeAsRadians, 
					latAsDeg * Geometry.degreesToRadians, lonAsDeg * Geometry.degreesToRadians);
		}

		@Override
		public boolean isFavorite() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void makeSnippetAndTitle(RouteConfig selectedRoute,
				HashMap<String, String> routeKeysToTitles, Context context) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addToSnippetAndTitle(RouteConfig routeConfig,
				Location location, HashMap<String, String> routeKeysToTitles,
				Context context) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getSnippetTitle() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSnippet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean containsId(int selectedBusId) {
			return false;
		}

		@Override
		public ArrayList<Alert> getSnippetAlerts() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
