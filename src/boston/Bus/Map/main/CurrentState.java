package boston.Bus.Map.main;


import com.schneeloch.bostonbusmap_library.data.RouteTitles;

import boston.Bus.Map.data.UpdateArguments;

import com.google.android.maps.MapView;
import com.schneeloch.bostonbusmap_library.data.Selection;

/**
 * Stores state when MainActivity pauses temporarily
 * @author schneg
 *
 */
public class CurrentState {
    protected final int selectedId;

    public CurrentState(int selectedId) {
        this.selectedId = selectedId;
    }

    public int getSelectedId() {
        return selectedId;
    }
}
