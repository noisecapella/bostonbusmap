package boston.Bus.Map.main;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.common.collect.ImmutableList;
import com.readystatesoftware.mapviewballoons.LimitLinearLayout;
import com.schneeloch.bostonbusmap_library.data.Alert;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.PredictionView;
import com.schneeloch.bostonbusmap_library.data.SimplePredictionView;
import com.schneeloch.bostonbusmap_library.data.StopLocation;

import boston.Bus.Map.R;
import boston.Bus.Map.ui.MapManager;

public class PopupAdapter implements InfoWindowAdapter {
    private static final int MAX_WIDTH = 360;
    private final Main main;
    private TextView title;
    private TextView snippet;
    private MapManager manager;
    private ImageView favorite;

    /**
     * The view we create for the popup which may get reused
     */
    private LimitLinearLayout popupView;

    public PopupAdapter(final Main main, MapManager manager) {
        this.main = main;
        this.manager = manager;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        if (popupView == null) {
            LimitLinearLayout parent = new LimitLinearLayout(getContext(), MAX_WIDTH);

            LayoutInflater inflater = (LayoutInflater) main
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layoutView = inflater.inflate(R.layout.balloon_overlay, parent);
            layoutView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            layoutView.setBackgroundResource(R.drawable.tooltip);
            title = (TextView) layoutView.findViewById(R.id.balloon_item_title);
            snippet = (TextView) layoutView.findViewById(R.id.balloon_item_snippet);
            favorite = (ImageView) layoutView.findViewById(R.id.balloon_item_favorite);

            popupView = parent;
        }

        String id = marker.getId();
        Location location = manager.getLocationFromMarkerId(id);
        populateView(location);
        return popupView;
    }
    protected Context getContext() {
        return main;
    }

    private void populateView(Location location) {
        //NOTE: originally this was going to be an actual link, but we can't click it on the popup except through its onclick listener

        PredictionView predictionView;
        if (location != null) {
            predictionView = location.getPredictionView();
        }
        else
        {
            predictionView = new SimplePredictionView("", "", ImmutableList.<Alert>of());
        }
        snippet.setText(Html.fromHtml(predictionView.getSnippet()));
        title.setText(Html.fromHtml(predictionView.getSnippetTitle()));

        if (location != null && location instanceof StopLocation) {
            favorite.setVisibility(View.VISIBLE);
            if (location.isFavorite()) {
                favorite.setImageResource(R.drawable.full_star);
            } else {
                favorite.setImageResource(R.drawable.empty_star);
            }
        }
        else {
            favorite.setVisibility(View.GONE);
        }
    }

}
