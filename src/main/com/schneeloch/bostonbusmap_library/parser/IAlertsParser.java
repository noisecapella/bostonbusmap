package com.schneeloch.bostonbusmap_library.parser;

import java.io.IOException;

import com.schneeloch.bostonbusmap_library.data.IAlerts;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;

public interface IAlertsParser {
	/**
	 * Download alerts from the internet, then return them
	 * @return
	 */
	public IAlerts obtainAlerts(IDatabaseAgent databaseAgent) throws IOException;
}
