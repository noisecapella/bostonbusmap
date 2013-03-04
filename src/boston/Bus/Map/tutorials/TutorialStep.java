package boston.Bus.Map.tutorials;

import android.view.View;
import android.widget.Button;

public abstract class TutorialStep {
	public abstract void setup(View parent);
	public abstract void teardown(View parent);
}
