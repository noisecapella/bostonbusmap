package boston.Bus.Map.tutorials;

import java.util.List;

import android.app.Activity;
import android.widget.TextView;
import boston.Bus.Map.R;

import com.google.common.collect.Lists;

public class IntroTutorial {

	public static List<TutorialStep> populate() {
		List<TutorialStep> steps = Lists.newArrayList();

		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep1;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep2;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep3;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep4;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep5;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep6;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep7;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep8;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep9;
			}
			
			@Override
			public void setup(Activity parent) {
				
			}
			
			@Override
			public void teardown(Activity parent) {
				
			}
			
		});
		
		return steps;
	}

}
