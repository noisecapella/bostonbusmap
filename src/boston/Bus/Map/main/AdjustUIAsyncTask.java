package boston.Bus.Map.main;

import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.UpdateArguments;

public class AdjustUIAsyncTask extends UpdateAsyncTask
{
	public AdjustUIAsyncTask(UpdateArguments arguments,
			boolean doShowUnpredictable, int maxOverlays, boolean drawCircle, boolean allRoutesBlue,
			Selection selection, UpdateHandler handler, Integer toSelect) {
		super(arguments, doShowUnpredictable, maxOverlays, drawCircle, allRoutesBlue,
				selection, handler, toSelect);
	}

	@Override
	protected boolean doUpdate() {
		return true;
	}

	@Override
	protected boolean areUpdatesSilenced() {
		return true;
	}
}
