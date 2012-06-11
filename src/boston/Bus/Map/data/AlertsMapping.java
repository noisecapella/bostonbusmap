package boston.Bus.Map.data;

import boston.Bus.Map.data.prepopulated.PrepopulatedAlerts;



public class AlertsMapping {
	public static final String alertUrlPrefix = "http://talerts.com/rssfeed/alertsrss.aspx?";

	public MyHashMap<String, Integer> getAlertNumbers() {
		return new PrepopulatedAlerts().getAlertNumbers();
	}

	
}
