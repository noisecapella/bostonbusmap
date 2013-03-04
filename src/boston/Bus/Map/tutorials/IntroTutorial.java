package boston.Bus.Map.tutorials;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.widget.Button;
import android.widget.ImageButton;
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
				ImageButton button = (ImageButton) parent.findViewById(R.id.myLocationButton);
				Drawable buttonDrawable = button.getBackground();
				if (!(buttonDrawable instanceof LayerDrawable)) {
					Drawable redBackground = parent.getResources().getDrawable(R.drawable.red_background);
					Drawable[] layers = new Drawable[]{redBackground, buttonDrawable};
					LayerDrawable layerDrawable = new LayerDrawable(layers);
					
					button.setBackgroundDrawable(layerDrawable);
				}
			}
			
			@Override
			public void teardown(Activity parent) {
				ImageButton button = (ImageButton) parent.findViewById(R.id.myLocationButton);
				Drawable buttonDrawable = button.getBackground();
				if (buttonDrawable instanceof LayerDrawable) {
					button.setBackgroundDrawable(((LayerDrawable) buttonDrawable).getDrawable(1));
				}
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
