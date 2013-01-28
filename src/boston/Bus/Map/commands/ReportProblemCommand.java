package boston.Bus.Map.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.android.gms.maps.model.Marker;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.View.OnClickListener;
import boston.Bus.Map.data.BusLocation;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;
import boston.Bus.Map.transit.TransitSystem;

public class ReportProblemCommand implements Command {
	private Location location;

	public ReportProblemCommand(Location location) {
		this.location = location;
	}
	
	@Override
	public String getDescription() {
		return "Report Problem";
	}
	
	@Override
	public void execute(Main main, UpdateHandler handler, Locations locations,
			RouteTitles routeTitles, Marker marker) throws Exception {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("plain/text");
		
		intent.putExtra(android.content.Intent.EXTRA_EMAIL, TransitSystem.emails);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, TransitSystem.emailSubject);

		
		String otherText = createEmailBody(main, locations);

		intent.putExtra(android.content.Intent.EXTRA_TEXT, otherText);
		main.startActivity(Intent.createChooser(intent, "Send email..."));
		
	}

	protected void createInfoForDeveloper(Context context, StringBuilder otherText, int mode, String routeTitle)
	{
		otherText.append("There was a problem with ");
		switch (mode)
		{
		case Selection.BUS_PREDICTIONS_ONE:
			otherText.append("bus predictions on one route. ");
			break;
		case Selection.BUS_PREDICTIONS_STAR:
			otherText.append("bus predictions for favorited routes. ");
			break;
		case Selection.BUS_PREDICTIONS_ALL:
			otherText.append("bus predictions for all routes. ");
			break;
		case Selection.VEHICLE_LOCATIONS_ALL:
			otherText.append("vehicle locations on all routes. ");
			break;
		case Selection.VEHICLE_LOCATIONS_ONE:
			otherText.append("vehicle locations for one route. ");
			break;
		default:
			otherText.append("something that I can't figure out. ");
		}
		
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			String versionText = packageInfo.versionName;
			otherText.append("App version: ").append(versionText).append(". ");
		}
		catch (NameNotFoundException e)
		{
			//don't worry about it
		}
		otherText.append("OS: ").append(android.os.Build.MODEL).append(". ");

		otherText.append("Currently selected route is '").append(routeTitle).append("'. ");
	}
	
	protected void createInfoForAgency(Context context,
			StringBuilder ret, int mode, String routeTitle, Locations locations)
	{
		if (location instanceof StopLocation)
		{
			StopLocation stopLocation = (StopLocation)location;
			String stopTag = stopLocation.getStopTag();
			ConcurrentMap<String, StopLocation> stopTags = locations.getAllStopsAtStop(stopTag);

			if (mode == Selection.BUS_PREDICTIONS_ONE)
			{
				if (stopTags.size() <= 1)
				{
					ret.append("The stop id is ").append(stopTag).append(" (").append(stopLocation.getTitle()).append(")");
					ret.append(" on route ").append(routeTitle).append(". ");
				}
				else
				{
					List<String> stopTagStrings = Lists.newArrayList();
					for (StopLocation stop : stopTags.values())
					{
						String text = stop.getStopTag() + " (" + stop.getTitle() + ")";
						stopTagStrings.add(text);
					}
					String stopTagsList = Joiner.on(",\n").join(stopTagStrings);
					
					ret.append("The stop ids are: ").append(stopTagsList).append(" on route ").append(routeTitle).append(". ");
				}
			}
			else
			{
				ArrayList<String> pairs = new ArrayList<String>();
				for (StopLocation stop : stopTags.values())
				{
					String routesJoin = Joiner.on(", ").join(stop.getRoutes());
					pairs.add(stop.getStopTag() + "(" + stop.getTitle() + ") on routes " + routesJoin);
				}
				
				//String list = Joiner.on(",\n").join(pairs);
				ret.append("The stop ids are: ");
				ret.append(Joiner.on(", ").join(pairs));
				ret.append(". ");
			}
		}
		else if (location instanceof BusLocation)
		{
			BusLocation busLocation = (BusLocation)location;
			String busRouteId = busLocation.getRouteId();
			ret.append("The bus number is ").append(busLocation.getBusNumber());
			ret.append(" on route ").append(locations.getRouteTitle(busRouteId)).append(". ");
		}

	}
	
	protected String createEmailBody(Context context, Locations locations)
	{
		Selection selection = locations.getSelection();
		if (selection == null) {
			selection = new Selection(-1, null);
		}

		String routeTitle = selection.getRoute();
		if (routeTitle != null) {
			routeTitle = locations.getRouteTitle(routeTitle);
		}
		else
		{
			routeTitle = "";
		}
		
		StringBuilder otherText = new StringBuilder();
		otherText.append("(What is the problem?\nAdd any other info you want at the beginning or end of this message, and click send.)\n\n");
		otherText.append("\n\n");
		createInfoForAgency(context, otherText, selection.getMode(), routeTitle, locations);
		otherText.append("\n\n");
		createInfoForDeveloper(context, otherText, selection.getMode(), routeTitle);

		

		return otherText.toString();
	}
}
