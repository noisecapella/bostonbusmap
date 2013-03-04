package boston.Bus.Map.tutorials;

import java.util.List;

import boston.Bus.Map.R;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Tutorial {
	private final List<TutorialStep> tutorialSteps;
	
	private int currentStep;

	public Tutorial(List<TutorialStep> tutorialSteps) {
		this.tutorialSteps = tutorialSteps;
		
		if (tutorialSteps.size() < 2) {
			throw new RuntimeException("Tutorial must have at least 2 steps");
		}
		
		this.currentStep = -1;
	}
	
	public void start(final Activity parent) {
		currentStep = 0;
		final View tutorialView = parent.findViewById(R.id.mapViewTutorial);
		final TextView textView = (TextView) parent.findViewById(R.id.mapViewTutorialText);
		tutorialView.setVisibility(View.VISIBLE);
		final Button skipButton = (Button)parent.findViewById(R.id.mapViewTutorialSkipButton);
		final Button nextButton = (Button)parent.findViewById(R.id.mapViewTutorialNextButton);
		skipButton.setVisibility(View.VISIBLE);
		skipButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tutorialSteps.get(currentStep).teardown(parent);
				tutorialView.setVisibility(View.GONE);
				currentStep = -1;
			}
		});
		nextButton.setVisibility(View.VISIBLE);
		nextButton.setText("Next");
		nextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tutorialSteps.get(currentStep).teardown(parent);
				if (currentStep == tutorialSteps.size() - 2) {
					// next is last one
					nextButton.setText("Ok");
					skipButton.setVisibility(View.GONE);
				}
				else if (currentStep >= tutorialSteps.size() - 1) {
					tutorialView.setVisibility(View.GONE);
					currentStep = -1;
				}
				
				int resource = tutorialSteps.get(currentStep).getTextResource();
				textView.setText(resource);
				tutorialSteps.get(currentStep).setup(parent);
				
			}
		});
		
		tutorialSteps.get(currentStep).setup(parent);
	}
}
