package boston.Bus.Map.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.LocationComparator;
import boston.Bus.Map.data.MyHashMap;
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
		public void makeSnippetAndTitle(RouteConfig selectedRoute,
				MyHashMap<String, String> routeKeysToTitles, Context context) {
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
		public ArrayList<Alert> getSnippetAlerts() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isVehicle() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isBeta() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int getLatAsInt() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getLonAsInt() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<String> getAllRoutes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFirstRoute() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
