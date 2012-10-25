package boston.Bus.Map.ui;

import android.text.Spanned;
import android.view.View;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class TextViewBinder implements ViewBinder
{
	private final int color;
	
	public TextViewBinder(int color) {
		this.color = color;
	}
	
	@Override
	public boolean setViewValue(View view, Object data,
			String textRepresentation) {
		TextView textView = (TextView)view;
		Spanned spanned = (Spanned)data;
		
		textView.setText(spanned);
		textView.setTextColor(color);
		return true;
	}
	
}
