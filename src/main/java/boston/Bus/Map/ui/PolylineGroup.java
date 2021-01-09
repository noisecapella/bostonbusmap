package boston.Bus.Map.ui;


import com.google.android.gms.maps.model.Polyline;
import com.schneeloch.bostonbusmap_library.data.Path;

/**
 * Convenience class to group Paths and Polylines
 * @author schneg
 *
 */
public class PolylineGroup {
    private final Polyline[] polylines;
    private final Path[] paths;

    public PolylineGroup(Polyline[] polylines, Path[] paths) {
        this.polylines = polylines;
        this.paths = paths;
    }

    public int size() {
        return paths.length;
    }

    public Polyline getPolyline(int i) {
        return polylines[i];
    }

    public Path getPath(int i) {
        return paths[i];
    }
}
