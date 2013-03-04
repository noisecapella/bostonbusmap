package boston.Bus.Map.tutorials;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public interface TutorialStep {
	public void setup(Activity parent);
	public void teardown(Activity parent);
	public int getTextResource();
}
