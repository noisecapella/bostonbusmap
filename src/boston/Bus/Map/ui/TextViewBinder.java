package boston.Bus.Map.ui;

import android.R.color;
import android.graphics.Color;
import android.text.Spanned;
import android.view.View;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class TextViewBinder implements ViewBinder
{
	public TextViewBinder() {
	}
	
	@Override
	public boolean setViewValue(View view, Object data,
			String textRepresentation) {
		TextView textView = (TextView)view;
		Spanned spanned = (Spanned)data;
		
		textView.setText(spanned);
		textView.setTextColor(Color.WHITE);
		return true;
	}
	
}
