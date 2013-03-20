package boston.Bus.Map.tutorials;

import java.util.List;

import com.schneeloch.torontotransit.R;

import boston.Bus.Map.main.Main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Tutorial {
	private final List<TutorialStep> tutorialSteps;
	
	public Tutorial(List<TutorialStep> tutorialSteps) {
		this.tutorialSteps = tutorialSteps;
		
		if (tutorialSteps.size() < 2) {
			throw new RuntimeException("Tutorial must have at least 2 steps");
		}
	}

	private int getStep(SharedPreferences prefs) {
		return prefs.getInt(Main.tutorialStepKey, 0);
	}

	private void setStep(SharedPreferences prefs, int step) {
		prefs.edit().putInt(Main.tutorialStepKey, step).commit();
	}
	
	public void start(final Main parent) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent);
		final View tutorialView = parent.findViewById(R.id.mapViewTutorial);
		if (getStep(prefs) < 0) {
			tutorialView.setVisibility(View.GONE);
			return;
		}
		final TextView textView = (TextView) parent.findViewById(R.id.mapViewTutorialText);
		tutorialView.setVisibility(View.VISIBLE);
		final Button skipButton = (Button)parent.findViewById(R.id.mapViewTutorialSkipButton);
		final Button nextButton = (Button)parent.findViewById(R.id.mapViewTutorialNextButton);
		skipButton.setVisibility(View.VISIBLE);
		skipButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tutorialSteps.get(getStep(prefs)).teardown(parent);
				tutorialView.setVisibility(View.GONE);
				setStep(prefs, -1);
			}
		});
		nextButton.setVisibility(View.VISIBLE);
		nextButton.setText("Next");
		nextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int currentStep = getStep(prefs);
				if (currentStep >= tutorialSteps.size() - 1) {
					tutorialView.setVisibility(View.GONE);
					setStep(prefs, -1);
				}
				else
				{
					tutorialSteps.get(currentStep).teardown(parent);
					setStep(prefs, currentStep + 1);
					updateCurrent(parent, textView, prefs, nextButton, skipButton);
				}
				
			}
		});
		
		updateCurrent(parent, textView, prefs, nextButton, skipButton);
	}

	protected void updateCurrent(Main parent, TextView textView,
			SharedPreferences prefs, Button nextButton, Button skipButton) {
		int currentStep = getStep(prefs);
		int resource = tutorialSteps.get(currentStep).getTextResource();
		textView.setText(resource);
		Linkify.addLinks(textView, Linkify.ALL);
		tutorialSteps.get(currentStep).setup(parent);
		if (currentStep == tutorialSteps.size() - 1) {
			// next is last one
			nextButton.setText("Ok");
			skipButton.setVisibility(View.GONE);
		}
	}
}
