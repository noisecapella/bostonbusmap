package boston.Bus.Map.ui;

import com.schneeloch.bostonbusmap_library.data.BikeSharePrediction;
import com.schneeloch.bostonbusmap_library.data.IPrediction;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Predictions;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

import java.util.SortedSet;

import boston.Bus.Map.R;

public class BikeShareDrawablesLookup {
    private static int defaultDrawable(boolean isSelected) {
        return selectDrawable(isSelected, R.drawable.busstop_bike, R.drawable.busstop_bike_selected);
    }

    private static int selectDrawable(boolean isSelected, int resource, int selectedResource) {
        if (isSelected) {
            return selectedResource;
        }
        return resource;
    }


    public static int getDrawable(StopLocation location, boolean isSelected) {
        Predictions predictions = location.getPredictions();
        if (predictions == null) {
            return defaultDrawable(isSelected);
        }

        SortedSet<IPrediction> predictionsList = predictions.getPredictionsCopy();
        if (predictionsList.isEmpty()) {
            LogUtil.w("Expected a prediction for bike share stop");
            return defaultDrawable(isSelected);
        }
        IPrediction prediction = predictionsList.first();

        if (!(prediction instanceof BikeSharePrediction)) {
            LogUtil.w("Expected BikeSharePrediction");
            return defaultDrawable(isSelected);
        }

        BikeSharePrediction bikeSharePrediction = (BikeSharePrediction)prediction;
        if (!bikeSharePrediction.installed || bikeSharePrediction.locked) {
            return defaultDrawable(isSelected);
        }

        if (bikeSharePrediction.numBikes == 0) {
            return selectDrawable(isSelected, R.drawable.busstop_bike_0, R.drawable.busstop_bike_0_selected);
        }

        if (bikeSharePrediction.numBikes == 1) {
            return selectDrawable(isSelected, R.drawable.busstop_bike_1, R.drawable.busstop_bike_1_selected);
        }

        if (bikeSharePrediction.numBikes == 2) {
            return selectDrawable(isSelected, R.drawable.busstop_bike_2, R.drawable.busstop_bike_2_selected);
        }

        if (bikeSharePrediction.numEmptyDocks == 0) {
            return selectDrawable(isSelected, R.drawable.busstop_bike_6, R.drawable.busstop_bike_6_selected);
        }

        if (bikeSharePrediction.numEmptyDocks == 1) {
            return selectDrawable(isSelected, R.drawable.busstop_bike_5, R.drawable.busstop_bike_5_selected);
        }

        if (bikeSharePrediction.numEmptyDocks == 2) {
            return selectDrawable(isSelected, R.drawable.busstop_bike_4, R.drawable.busstop_bike_4_selected);
        }

        return selectDrawable(isSelected, R.drawable.busstop_bike_3, R.drawable.busstop_bike_3_selected);
    }
}
