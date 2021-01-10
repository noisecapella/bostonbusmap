package boston.Bus.Map.tutorials;

import boston.Bus.Map.main.Main;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public interface TutorialStep {
	public void setup(Main parent);
	public void teardown(Main parent);
	public int getTextResource();
}
