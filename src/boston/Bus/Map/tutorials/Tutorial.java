package boston.Bus.Map.tutorials;

import java.util.List;

import boston.Bus.Map.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
	
	public void start(final View parent) {
		currentStep = 0;
		final View tutorialView = parent.findViewById(R.id.mapViewTutorial);
		tutorialView.setVisibility(View.VISIBLE);
		Button skipButton = (Button)parent.findViewById(R.id.mapViewTutorialSkipButton);
		Button nextButton = (Button)parent.findViewById(R.id.mapViewTutorialNextButton);
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
		nextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tutorialSteps.get(currentStep).teardown(parent);
				if (currentStep + 1 >= tutorialSteps.size()) {
					tutorialView.setVisibility(View.GONE);
					currentStep = -1;
				}
				tutorialSteps.get(currentStep).setup(parent);
				
			}
		});
		
		tutorialSteps.get(currentStep).setup(parent);
	}
}
