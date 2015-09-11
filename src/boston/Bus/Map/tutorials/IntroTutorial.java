package boston.Bus.Map.tutorials;

import java.util.List;


import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import boston.Bus.Map.R;
import boston.Bus.Map.main.Main;

import com.google.common.collect.Lists;

public class IntroTutorial {

	private static void addHighlight(View view) {
		Drawable viewDrawable = view.getBackground();
		if (!(viewDrawable instanceof LayerDrawable)) {
			Drawable redBackground = view.getResources().getDrawable(R.drawable.red_background);
			Drawable[] layers = new Drawable[]{redBackground, viewDrawable};
			LayerDrawable layerDrawable = new LayerDrawable(layers);
			
			view.setBackgroundDrawable(layerDrawable);
		}

	}
	
	private static void removeHighlight(View view) {
		Drawable viewDrawable = view.getBackground();
		if (viewDrawable instanceof LayerDrawable) {
			view.setBackgroundDrawable(((LayerDrawable) viewDrawable).getDrawable(1));
		}
	}
	
	public static List<TutorialStep> populate() {
		List<TutorialStep> steps = Lists.newArrayList();

		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep1;
			}
			
			@Override
			public void setup(Main parent) {
                View button = parent.findViewById(R.id.drawerButton);
                addHighlight(button);
			}
			
			@Override
			public void teardown(Main parent) {
				View button = parent.findViewById(R.id.drawerButton);
                removeHighlight(button);
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep2;
			}
			
			@Override
			public void setup(Main parent) {
			}
			
			@Override
			public void teardown(Main parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			@Override
			public int getTextResource() {
				return R.string.tutorialStep4;
			}
			
			@Override
			public void setup(Main parent) {
				View view = parent.findViewById(R.id.searchTextView);
				addHighlight(view);
			}
			
			@Override
			public void teardown(Main parent) {
				View view = parent.findViewById(R.id.searchTextView);
				removeHighlight(view);
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep5;
			}
			
			@Override
			public void setup(Main parent) {
				View view = parent.findViewById(R.id.predictionsOrLocations);
				addHighlight(view);
			}
			
			@Override
			public void teardown(Main parent) {
				View view = parent.findViewById(R.id.predictionsOrLocations);
				removeHighlight(view);
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep6;
			}
			
			@Override
			public void setup(Main parent) {

			}
			
			@Override
			public void teardown(Main parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep7;
			}
			
			@Override
			public void setup(Main parent) {
			}
			
			@Override
			public void teardown(Main parent) {
				
			}
			
		});
		steps.add(new TutorialStep() {
			
			@Override
			public int getTextResource() {
				return R.string.tutorialStep9;
			}
			
			@Override
			public void setup(Main parent) {
				
			}
			
			@Override
			public void teardown(Main parent) {
				
			}
			
		});
		
		return steps;
	}

}
