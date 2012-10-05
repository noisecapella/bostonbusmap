package boston.Bus.Map.data;

public abstract class StopPredictionView extends PredictionView {
	public abstract String[] getTitles();

	public abstract String[] getRoutes();

	public abstract String getStops();

	public abstract Prediction[] getPredictions();

	private final static StopPredictionView EMPTY = new StopPredictionView() {
		@Override
		public String[] getTitles() {
			return nullStrings;
		}

		@Override
		public String[] getRoutes() {
			return nullStrings;
		}

		@Override
		public String getStops() {
			return "";
		}

		@Override
		public String getSnippet() {
			return "";
		}

		@Override
		public String getSnippetTitle() {
			return "";
		}

		@Override
		public Prediction[] getPredictions() {
			return nullPredictions;
		}

		@Override
		public Alert[] getAlerts() {
			return nullAlerts;
		}
	};

	public static StopPredictionView empty() {
		return EMPTY;
	}

}
