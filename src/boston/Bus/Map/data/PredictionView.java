package boston.Bus.Map.data;

import com.google.common.collect.ImmutableCollection;

public abstract class PredictionView {
	protected final static String[] nullStrings = new String[0];
	protected final static Prediction[] nullPredictions = new Prediction[0];
	
	
	public abstract String getSnippet();
	public abstract String getSnippetTitle();

	public abstract ImmutableCollection<Alert> getAlerts();

}
