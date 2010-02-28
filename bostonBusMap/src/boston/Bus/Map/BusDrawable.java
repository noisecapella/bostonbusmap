package boston.Bus.Map;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

/**
 * This draws a bus with an arrow in it. There might be a simpler way than subclassing Drawable; if so let me know
 * 
 * 
 * 
 * @author schneg
 *
 */
public class BusDrawable extends Drawable {
	private final Drawable bus;
	private final Drawable arrow;
	private final int heading;
	private final Drawable tooltip;
	private final TextView textView;
	
	public BusDrawable(Drawable drawable, int heading, Drawable arrow, Drawable tooltip, TextView textView)
	{
		this.bus = drawable;
		this.heading = heading;
		
		if (arrow != null)
		{
			this.arrow = arrow;
		}
		else
		{
			this.arrow = new BitmapDrawable();
		}
		
		
		if (tooltip != null)
		{
			this.tooltip = tooltip;
		}
		else
		{
			this.tooltip = new BitmapDrawable();
		}
		
		this.textView = textView;
	}
	
	@Override
	public void draw(Canvas canvas) {
		//put the arrow in the bus window
		
		int arrowLeft = -(arrow.getIntrinsicWidth() / 4);
		int arrowTop = -bus.getIntrinsicHeight() + 7;
		int arrowWidth = (int)(arrow.getIntrinsicWidth() * 0.60); 
		int arrowHeight = (int)(arrow.getIntrinsicHeight() * 0.60);
		int arrowRight = arrowLeft + arrowWidth;
		int arrowBottom = arrowTop + arrowHeight;
		
		//NOTE: 0, 0 is the bottom center point of the bus icon
		
		//first draw the bus
		bus.draw(canvas);
		
		//then draw arrow
		arrow.setBounds(arrowLeft, arrowTop, arrowRight, arrowBottom);
		
		canvas.save();
		//set rotation pivot at the center of the arrow image
		canvas.rotate(heading, arrowLeft + arrowWidth/2, arrowTop + arrowHeight / 2);
		
		Rect rect = arrow.getBounds();
		arrow.draw(canvas);
		arrow.setBounds(rect);
		canvas.restore();
		
		
		//last, draw tooltip if necessary. This drawable will be empty if we don't need to draw it
		if (textView != null)
		{
			int tooltipLeft = -bus.getIntrinsicWidth();
			int tooltipTop = -bus.getIntrinsicHeight() * 2;

			//tooltip.setBounds(0, 0, tooltipWidth, tooltipHeight);
			
			textView.setBackgroundDrawable(tooltip);
			textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			//textView.layout(tooltipLeft, tooltipTop, tooltipRight, tooltipBottom);
			//textView.

			//textView.setText(text);
			
			textView.measure(100, 100);
			int tooltipWidth = textView.getMeasuredWidth();
			int tooltipHeight = textView.getMeasuredHeight();
			int tooltipRight = tooltipLeft + tooltipWidth;
			int tooltipBottom = tooltipTop + tooltipHeight;
			
			
			textView.layout(tooltipLeft, tooltipTop, tooltipRight, tooltipBottom);
			textView.setTextColor(Color.BLACK);
			
			//HACK: the textView is drawing at 0,0, and i can't get it to draw slightly to the left
			canvas.save();
			canvas.translate(-textView.getMeasuredWidth() / 2, 0);
			textView.draw(canvas);
			canvas.restore();
			
		}

	}

	
	
	@Override
	public int getOpacity() {
		return bus.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		bus.setAlpha(alpha);
		arrow.setAlpha(alpha);
		tooltip.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		bus.setColorFilter(cf);
		arrow.setColorFilter(cf);
		tooltip.setColorFilter(cf);
	}
	
	@Override
	public int getIntrinsicHeight() {
		//ignoring arrow because it's inside the drawable
		
		return bus.getIntrinsicHeight() + tooltip.getIntrinsicHeight();
	}
	@Override
	public int getIntrinsicWidth() {
		return bus.getIntrinsicWidth() + tooltip.getIntrinsicWidth();
	}
	
	@Override
	public int getMinimumHeight() {
		return bus.getMinimumHeight() + tooltip.getMinimumHeight();
	}
	
	@Override
	public int getMinimumWidth() {
		return bus.getMinimumWidth() + tooltip.getMinimumWidth();
	}
	
	@Override
	public Drawable getCurrent() {
		return bus.getCurrent();
	}
	
	@Override
	public void clearColorFilter() {
		arrow.clearColorFilter();
		bus.clearColorFilter();
		tooltip.clearColorFilter();
	}
	
	@Override
	public boolean equals(Object o) {
		return bus.equals(o);
	}
	
	@Override
	public int getChangingConfigurations() {
		return bus.getChangingConfigurations();
	}
	
	@Override
	public ConstantState getConstantState() {
		return bus.getConstantState();
	}
	
	@Override
	public boolean getPadding(Rect padding) {
		return bus.getPadding(padding);
	}
	
	@Override
	public int[] getState() {
		return bus.getState();
	}
	
	@Override
	public Region getTransparentRegion() {
		return bus.getTransparentRegion();
	}
	
	@Override
	public int hashCode() {
		return bus.hashCode();
	}
	
	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		bus.inflate(r, parser, attrs);
		arrow.inflate(r, parser, attrs);
		tooltip.inflate(r, parser, attrs);
	}
	
	@Override
	public void invalidateSelf() {
		bus.invalidateSelf();
		arrow.invalidateSelf();
		tooltip.invalidateSelf();
	}
	
	@Override
	public boolean isStateful() {
		return bus.isStateful();
	}
	
	@Override
	public Drawable mutate() {
		//NOTE: even though mutate() returns a Drawable, it also changes the state of the current item (i think)
		//so we need to do it on each one
		
		arrow.mutate();
		tooltip.mutate();
		return bus.mutate();
	}
	
	@Override
	public void scheduleSelf(Runnable what, long when) {
		bus.scheduleSelf(what, when);
		arrow.scheduleSelf(what, when);
		tooltip.scheduleSelf(what, when);
	}
	
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		bus.setBounds(left, top, right, bottom);
		arrow.setBounds(left, top, right, bottom);
		tooltip.setBounds(left, top, right, bottom);
	}
	
	@Override
	public void setBounds(Rect bounds) {
		bus.setBounds(bounds);
		arrow.setBounds(bounds);
		tooltip.setBounds(bounds);
	}
	
	@Override
	public void setChangingConfigurations(int configs) {
		bus.setChangingConfigurations(configs);
		arrow.setChangingConfigurations(configs);
		tooltip.setChangingConfigurations(configs);
	}
	
	@Override
	public void setColorFilter(int color, Mode mode) {
		bus.setColorFilter(color, mode);
		arrow.setColorFilter(color, mode);
		tooltip.setColorFilter(color, mode);
	}
	
	@Override
	public void setDither(boolean dither) {
		arrow.setDither(dither);
		bus.setDither(dither);
		tooltip.setDither(dither);
	}
	
	@Override
	public void setFilterBitmap(boolean filter) {
		arrow.setFilterBitmap(filter);
		bus.setFilterBitmap(filter);
		tooltip.setFilterBitmap(filter);
	}
	
	@Override
	public boolean setState(int[] stateSet) {
		boolean arrowModified = arrow.setState(stateSet);
		boolean busModified = bus.setState(stateSet);
		boolean tooltipModified = tooltip.setState(stateSet);
		
		//we have to make sure all three are called. If we inline this, the condition may short circuit and not all setState will be called
		return arrowModified || busModified || tooltipModified;
	}
	
	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		boolean arrowModified = arrow.setVisible(visible, restart);
		boolean busModified = bus.setVisible(visible, restart);
		boolean tooltipModified = tooltip.setVisible(visible, restart);
		
		
		//we have to make sure all three are called. If we inline this, the condition may short circuit and not all setVisible will be called
		return arrowModified || busModified || tooltipModified;
	}
	
	@Override
	public void unscheduleSelf(Runnable what) {
		bus.unscheduleSelf(what);
		arrow.unscheduleSelf(what);
		tooltip.unscheduleSelf(what);
	}
}
