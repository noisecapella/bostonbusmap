package boston.Bus.Map.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import boston.Bus.Map.data.BusLocation;

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
import android.os.Debug;
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
	private final ArrayList<Integer> additionalHeadings;
	
	public BusDrawable(Drawable drawable, int heading, ArrayList<Integer> additionalHeadings, Drawable arrow)
	{
		this.bus = drawable;
		this.heading = heading;
		this.arrow = arrow;
		this.additionalHeadings = additionalHeadings;
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		//NOTE: 0, 0 is the bottom center point of the bus icon
		
		//first draw the bus
		bus.draw(canvas);
		
		//then draw arrow
		if (arrow != null && heading != BusLocation.NO_HEADING)
		{
			//put the arrow in the bus window
			
			int arrowLeft = -(arrow.getIntrinsicWidth() / 4);
			int arrowTop = -bus.getIntrinsicHeight() + 7;
			
			//NOTE: use integer division when possible. This code is frequently executed
			int arrowWidth = (arrow.getIntrinsicWidth() * 6) / 10;  
			int arrowHeight = (arrow.getIntrinsicHeight() * 6) / 10;
			int arrowRight = arrowLeft + arrowWidth;
			int arrowBottom = arrowTop + arrowHeight;

			arrow.setBounds(arrowLeft, arrowTop, arrowRight, arrowBottom);

			canvas.save();
			//set rotation pivot at the center of the arrow image
			canvas.rotate(heading, arrowLeft + arrowWidth/2, arrowTop + arrowHeight / 2);

			Rect rect = arrow.getBounds();
			arrow.draw(canvas);
			arrow.setBounds(rect);
			
			canvas.restore();

		
			if (additionalHeadings != null)
			{
				for (Integer heading : additionalHeadings)
				{
					canvas.save();
					//set rotation pivot at the center of the arrow image
					canvas.rotate(heading, arrowLeft + arrowWidth/2, arrowTop + arrowHeight / 2);

					rect = arrow.getBounds();
					arrow.draw(canvas);
					arrow.setBounds(rect);
					
					canvas.restore();
				}
			}
			
}
		
	}

	
	
	@Override
	public int getOpacity() {
		return bus.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		bus.setAlpha(alpha);
		
		if (arrow != null)
		{
			arrow.setAlpha(alpha);
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		bus.setColorFilter(cf);
		if (arrow != null)
		{
			arrow.setColorFilter(cf);
		}
	}
	
	@Override
	public int getIntrinsicHeight() {
		//ignoring arrow because it's inside the drawable
		return bus.getIntrinsicHeight();
	}
	@Override
	public int getIntrinsicWidth() {
		return bus.getIntrinsicWidth();
	}
	
	@Override
	public int getMinimumHeight() {
		return bus.getMinimumHeight();
	}
	
	@Override
	public int getMinimumWidth() {
		return bus.getMinimumWidth();
	}
	
	@Override
	public Drawable getCurrent() {
		return bus.getCurrent();
	}
	
	@Override
	public void clearColorFilter() {
		bus.clearColorFilter();
		if (arrow != null)
		{
			arrow.clearColorFilter();
		}
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

	public Drawable.ConstantState getConstantState() {
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
		if (arrow != null)
		{
			arrow.inflate(r, parser, attrs);
		}
	}
	
	@Override
	public void invalidateSelf() {
		bus.invalidateSelf();
		if (arrow != null)
		{
			arrow.invalidateSelf();
		}
	}
	
	@Override
	public boolean isStateful() {
		return bus.isStateful();
	}
	
	@Override
	public Drawable mutate() {
		//NOTE: even though mutate() returns a Drawable, it also changes the state of the current item (i think)
		//so we need to do it on each one
		
		if (arrow != null)
		{
			arrow.mutate();
		}
		return bus.mutate();
	}
	
	@Override
	public void scheduleSelf(Runnable what, long when) {
		bus.scheduleSelf(what, when);
		if (arrow != null)
		{
			arrow.scheduleSelf(what, when);
		}
	}
	
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		bus.setBounds(left, top, right, bottom);
		if (arrow != null)
		{
			arrow.setBounds(left, top, right, bottom);
		}
	}
	
	@Override
	public void setBounds(Rect bounds) {
		bus.setBounds(bounds);
		if (arrow != null)
		{
			arrow.setBounds(bounds);
		}
	}
	
	@Override
	public void setChangingConfigurations(int configs) {
		bus.setChangingConfigurations(configs);
		if (arrow != null)
		{
			arrow.setChangingConfigurations(configs);
		}
	}
	
	@Override
	public void setColorFilter(int color, Mode mode) {
		bus.setColorFilter(color, mode);
		if (arrow != null)
		{
			arrow.setColorFilter(color, mode);
		}
	}
	
	@Override
	public void setDither(boolean dither) {
		bus.setDither(dither);
		if (arrow != null)
		{
			arrow.setDither(dither);
		}
	}
	
	@Override
	public void setFilterBitmap(boolean filter) {
		bus.setFilterBitmap(filter);
		if (arrow != null)
		{
			arrow.setFilterBitmap(filter);
		}
	}
	
	@Override
	public boolean setState(int[] stateSet) {
		boolean busModified = bus.setState(stateSet);
		boolean arrowModified = (arrow != null ? arrow.setState(stateSet) : false);
		
		//we have to make sure all three are called. If we inline this, the condition may short circuit and not all setState will be called
		return arrowModified || busModified;
	}
	
	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		boolean busModified = bus.setVisible(visible, restart);
		boolean arrowModified = (arrow != null ? arrow.setVisible(visible, restart) : false);
		
		//we have to make sure all three are called. If we inline this, the condition may short circuit and not all setVisible will be called
		return arrowModified || busModified;
	}
	
	@Override
	public void unscheduleSelf(Runnable what) {
		bus.unscheduleSelf(what);
		if (arrow != null)
		{
			arrow.unscheduleSelf(what);
		}
	}
}
