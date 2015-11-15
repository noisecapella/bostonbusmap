package boston.Bus.Map.main;

import com.schneeloch.bostonbusmap_library.data.Selection;
import boston.Bus.Map.data.UpdateArguments;

public class AdjustUIAsyncTask extends UpdateAsyncTask
{
	public AdjustUIAsyncTask(UpdateArguments arguments,
			boolean doShowUnpredictable, int maxOverlays,
			Selection selection, UpdateHandler handler, Runnable afterUpdate) {
		super(arguments, doShowUnpredictable, maxOverlays,
				selection, handler, afterUpdate);
	}

	@Override
	protected boolean doUpdate() {
		return true;
	}

	@Override
	protected boolean areUpdatesSilenced() {
		return true;
	}

    @Override
    protected boolean forceNewMarker() {
        return false;
    }
}
