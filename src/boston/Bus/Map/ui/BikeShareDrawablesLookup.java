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
    private static int defaultDrawable(boolean isSelected, boolean isUpdated) {
        return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike, R.drawable.busstop_bike_selected, R.drawable.busstop_bike_updated);
    }

    private static int selectDrawable(boolean isSelected, boolean isUpdated, int resource, int selectedResource, int updatedResource) {
        if (isSelected) {
            return selectedResource;
        } else if (isUpdated) {
            return updatedResource;
        }
        return resource;
    }


    public static int getDrawable(StopLocation location, boolean isSelected, boolean isUpdated) {
        Predictions predictions = location.getPredictions();
        if (predictions == null) {
            return defaultDrawable(isSelected, isUpdated);
        }

        SortedSet<IPrediction> predictionsList = predictions.getPredictionsCopy();
        if (predictionsList.isEmpty()) {
            LogUtil.w("Expected a prediction for bike share stop");
            return defaultDrawable(isSelected, isUpdated);
        }
        IPrediction prediction = predictionsList.first();

        if (!(prediction instanceof BikeSharePrediction)) {
            LogUtil.w("Expected BikeSharePrediction");
            return defaultDrawable(isSelected, isUpdated);
        }

        BikeSharePrediction bikeSharePrediction = (BikeSharePrediction)prediction;
        if (!bikeSharePrediction.installed || bikeSharePrediction.locked) {
            return defaultDrawable(isSelected, isUpdated);
        }

        if (bikeSharePrediction.numBikes == 0) {
            return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_0, R.drawable.busstop_bike_0_selected, R.drawable.busstop_bike_0_updated);
        }

        if (bikeSharePrediction.numBikes == 1) {
            return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_1, R.drawable.busstop_bike_1_selected, R.drawable.busstop_bike_1_updated);
        }

        if (bikeSharePrediction.numBikes == 2) {
            return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_2, R.drawable.busstop_bike_2_selected, R.drawable.busstop_bike_2_updated);
        }

        if (bikeSharePrediction.numEmptyDocks == 0) {
            return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_6, R.drawable.busstop_bike_6_selected, R.drawable.busstop_bike_6_updated);
        }

        if (bikeSharePrediction.numEmptyDocks == 1) {
            return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_5, R.drawable.busstop_bike_5_selected, R.drawable.busstop_bike_5_updated);
        }

        if (bikeSharePrediction.numEmptyDocks == 2) {
            return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_4, R.drawable.busstop_bike_4_selected, R.drawable.busstop_bike_4_updated);
        }

        return selectDrawable(isSelected, isUpdated, R.drawable.busstop_bike_3, R.drawable.busstop_bike_3_selected, R.drawable.busstop_bike_3_updated);
    }
}
