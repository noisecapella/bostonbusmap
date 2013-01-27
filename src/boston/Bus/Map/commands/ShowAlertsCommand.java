package boston.Bus.Map.commands;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import boston.Bus.Map.data.Alert;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.main.AlertInfo;
import boston.Bus.Map.main.Main;
import boston.Bus.Map.main.UpdateHandler;

public class ShowAlertsCommand implements Command {
	private final Alert[] alerts;
	
	public ShowAlertsCommand(Alert[] alerts) {
		this.alerts = alerts;
	}
	
	@Override
	public String getDescription() {
		int numAlerts = alerts.length;
		if (numAlerts == 1) {
			return "Show 1 alert";
		}
		else
		{
			return "Show " + numAlerts + " alerts";
		}
	}

	@Override
	public void execute(Main main, UpdateHandler handler, Locations locations,
			RouteTitles routeTitles) throws Exception {
		Intent intent = new Intent(main, AlertInfo.class);
		if (alerts != null)
		{
			intent.putExtra(AlertInfo.alertsKey, alerts);
			
			main.startActivity(intent);
		}
		else
		{
			Log.i("BostonBusMap", "alertsList is null");
		}
		
	}
}
