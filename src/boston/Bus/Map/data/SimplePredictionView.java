package boston.Bus.Map.data;

public class SimplePredictionView extends PredictionView {
	private final String snippet;
	private final String snippetTitle;
	private final Alert[] alerts;
	
	public SimplePredictionView(String snippet, String snippetTitle, Alert[] alerts) {
		this.snippet = snippet;
		this.snippetTitle = snippetTitle;
		this.alerts = alerts;
	}
	
	@Override
	public String getSnippet() {
		return snippet;
	}

	@Override
	public String getSnippetTitle() {
		return snippetTitle;
	}

	@Override
	public Alert[] getAlerts() {
		return alerts;
	}

	private static final SimplePredictionView EMPTY = new SimplePredictionView("", "", new Alert[0]); 
	
	public static SimplePredictionView empty() {
		return EMPTY;
	}

}
