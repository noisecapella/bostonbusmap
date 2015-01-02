package boston.Bus.Map.parser;

import java.io.IOException;

import boston.Bus.Map.data.IAlerts;
import boston.Bus.Map.provider.DatabaseAgent;

import android.content.Context;

public interface IAlertsParser {
	/**
	 * Download alerts from the internet, then return them
	 * @return
	 */
	public IAlerts obtainAlerts(DatabaseAgent databaseAgent) throws IOException;
}
