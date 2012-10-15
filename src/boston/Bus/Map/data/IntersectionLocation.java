package boston.Bus.Map.data;

import java.util.Set;

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
	
	private IntersectionLocation(Builder builder) {
		this.name = builder.name;
		this.latitudeAsDegrees = builder.latitudeAsDegrees;
		this.longitudeAsDegrees = builder.longitudeAsDegrees;
		latitude = (float) (latitudeAsDegrees * Geometry.degreesToRadians);
		longitude = (float) (longitudeAsDegrees * Geometry.degreesToRadians);
		
		predictionView = new SimplePredictionView("", name, new Alert[0]);
		nearbyRoutes = builder.nearbyRoutes.build();
	}
	
	public static class Builder {
		private final String name;
		private final float latitudeAsDegrees;
		private final float longitudeAsDegrees;
		private final ImmutableSet.Builder<String> nearbyRoutes;
		
		public Builder(String name, float latitudeAsDegrees, float longitudeAsDegrees) {
			this.name = name;
			this.latitudeAsDegrees = latitudeAsDegrees;
			this.longitudeAsDegrees = longitudeAsDegrees;
			this.nearbyRoutes = ImmutableSet.builder();
		}
		
		public void addRoute(String route) {
			nearbyRoutes.add(route);
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
		return null;
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
}
