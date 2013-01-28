package boston.Bus.Map.data;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.readystatesoftware.mapviewballoons.LimitLinearLayout;

import boston.Bus.Map.R;
import boston.Bus.Map.commands.Command;
import boston.Bus.Map.commands.DeletePlaceCommand;
import boston.Bus.Map.commands.EditPlaceCommand;
import boston.Bus.Map.commands.NearbyRoutesCommand;
import boston.Bus.Map.math.Geometry;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
	private final ImmutableSet<String> nearbyRouteTitles;
	
	private final Drawable drawable;
	
	private IntersectionLocation(Builder builder, TransitSourceTitles routeTitles) {
		this.name = builder.name;
		this.latitudeAsDegrees = builder.latitudeAsDegrees;
		this.longitudeAsDegrees = builder.longitudeAsDegrees;
		latitude = (float) (latitudeAsDegrees * Geometry.degreesToRadians);
		longitude = (float) (longitudeAsDegrees * Geometry.degreesToRadians);
		
		nearbyRoutes = builder.nearbyRoutes.build();
		SortedSet<String> titles = Sets.newTreeSet(new RouteTitleComparator());
		for (String tag : nearbyRoutes) {
			titles.add(routeTitles.getTitle(tag));
		}
		nearbyRouteTitles = ImmutableSet.copyOf(titles);

		predictionView = new SimplePredictionView("", name, new Alert[0]);
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
	public BitmapDescriptor getDrawable(Context context,
			boolean isSelected) {
		ViewGroup root = new LimitLinearLayout(context, 120);
		View view = LayoutInflater.from(context).inflate(R.layout.intersection_icon, root);
		TextView textView = (TextView)view.findViewById(R.id.intersection_text);
		textView.setText(getName());
		Bitmap ret = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(ret);
		canvas.drawColor(Color.WHITE);
		view.draw(canvas);
		return BitmapDescriptorFactory.fromBitmap(ret);
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
			RouteTitles routeKeysToTitles, Locations locations, Context context) {
		// do nothing
	}

	@Override
	public void addToSnippetAndTitle(RouteConfig routeConfig,
			Location location, RouteTitles routeKeysToTitles, Locations locations, Context context) {
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
	
	public ImmutableSet<String> getNearbyRouteTitles() {
		return nearbyRouteTitles;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public boolean isIntersection() {
		return true;
	}

	@Override
	public boolean needsUpdating() {
		return false;
	}

	@Override
	public List<Command> getCommands() {
		return Arrays.asList(new DeletePlaceCommand(this),
				new EditPlaceCommand(this), 
				new NearbyRoutesCommand(this));
	}
}
