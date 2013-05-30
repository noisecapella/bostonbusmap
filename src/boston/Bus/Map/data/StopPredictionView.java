package boston.Bus.Map.data;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public abstract class StopPredictionView extends PredictionView {
	public abstract String[] getTitles();

	public abstract String[] getRouteTitles();

	public abstract String getStops();

	public abstract Prediction[] getPredictions();

	private final static StopPredictionView EMPTY = new StopPredictionView() {
		@Override
		public String[] getTitles() {
			return nullStrings;
		}

		@Override
		public String[] getRouteTitles() {
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
		public ImmutableCollection<Alert> getAlerts() {
			return ImmutableList.of();
		}
	};

	public static StopPredictionView empty() {
		return EMPTY;
	}

}
