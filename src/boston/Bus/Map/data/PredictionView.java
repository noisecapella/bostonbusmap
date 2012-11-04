package boston.Bus.Map.data;

public abstract class PredictionView {
	protected final static String[] nullStrings = new String[0];
	protected final static Prediction[] nullPredictions = new Prediction[0];
	protected final static Alert[] nullAlerts = new Alert[0];
	
	
	public abstract String getSnippet();
	public abstract String getSnippetTitle();

	public abstract Alert[] getAlerts();

}
