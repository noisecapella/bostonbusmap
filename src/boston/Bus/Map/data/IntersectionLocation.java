package boston.Bus.Map.data;

import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import boston.Bus.Map.math.Geometry;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class IntersectionLocation implements Location {
	/**
	 * Used for getId
	 */
	private static final int LOCATIONTYPE = 5;
	
	private final String name;
	private final float latitude;
	private final float longitude;
	private final float latitudeAsDegrees;
	private final float longitudeAsDegrees;
	
	private final PredictionView predictionView;
	
	private final ImmutableSet<String> nearbyRoutes;
	
	private final Drawable drawable;
	
	private IntersectionLocation(Builder builder, TransitSourceTitles routeTitles) {
		this.name = builder.name;
		this.latitudeAsDegrees = builder.latitudeAsDegrees;
		this.longitudeAsDegrees = builder.longitudeAsDegrees;
		latitude = (float) (latitudeAsDegrees * Geometry.degreesToRadians);
		longitude = (float) (longitudeAsDegrees * Geometry.degreesToRadians);
		
		nearbyRoutes = builder.nearbyRoutes.build();
		SortedSet<String> titles = Sets.newTreeSet();
		for (String tag : nearbyRoutes) {
			titles.add(routeTitles.getTitle(tag));
		}
		String routeTitleString = "<b>Nearby routes: " + Joiner.on(", ").join(titles);

		predictionView = new SimplePredictionView(routeTitleString, name, new Alert[0]);
		this.drawable = builder.drawable;
	}
	
	public static class Builder {
		private final String name;
		private final float latitudeAsDegrees;
		private final float longitudeAsDegrees;
		private final ImmutableSet.Builder<String> nearbyRoutes;
		private final Drawable drawable;
		
		public Builder(String name, float latitudeAsDegrees, float longitudeAsDegrees,
				Drawable drawable) {
			this.name = name;
			this.latitudeAsDegrees = latitudeAsDegrees;
			this.longitudeAsDegrees = longitudeAsDegrees;
			this.nearbyRoutes = ImmutableSet.builder();
			this.drawable = drawable;
		}
		
		public void addRoute(String route) {
			nearbyRoutes.add(route);
		}

		public IntersectionLocation build(TransitSourceTitles routeTitles) {
			return new IntersectionLocation(this, routeTitles);
		}

		public double getLatitudeAsDegrees() {
			return latitudeAsDegrees;
		}
		
		public double getLongitudeAsDegrees() {
			return longitudeAsDegrees;
		}
	}
	
	@Override
	public int getId() {
		return (name.hashCode() & 0xffffff) | LOCATIONTYPE << 24;
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
		return drawable;
	}

	@Override
	public float getLatitudeAsDegrees() {
		return latitudeAsDegrees;
	}

	@Override
	public float getLongitudeAsDegrees() {
		return longitudeAsDegrees;
	}

	@Override
	public float distanceFrom(double centerLatitude,
			double centerLongitude) {
		return Geometry.computeCompareDistance(latitude, longitude, centerLatitude, centerLongitude);
	}

	@Override
	public float distanceFromInMiles(double centerLatAsRadians,
			double centerLonAsRadians) {
		return Geometry.computeDistanceInMiles(latitude, longitude, centerLatAsRadians, centerLonAsRadians);
	}
	
	@Override
	public boolean isFavorite() {
		return false;
	}

	@Override
	public void makeSnippetAndTitle(RouteConfig selectedRoute,
			RouteTitles routeKeysToTitles, Context context) {
		// do nothing
	}

	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig,
			Location location, RouteTitles routeKeysToTitles, Context context) {
		// do nothing
	}

	@Override
	public boolean containsId(int selectedBusId) {
		return getId() == selectedBusId;
	}

	@Override
	public PredictionView getPredictionView() {
		return predictionView;
	}

	@Override
	public boolean hasMoreInfo() {
		return false;
	}

	@Override
	public boolean hasFavorite() {
		return false;
	}

	@Override
	public boolean hasReportProblem() {
		return false;
	}
	
	public ImmutableSet<String> getNearbyRoutes() {
		return nearbyRoutes;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public boolean isIntersection() {
		return true;
	}
}
