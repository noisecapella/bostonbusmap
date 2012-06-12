package boston.Bus.Map.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import boston.Bus.Map.data.VehicleLocation;

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
	private final Drawable vehicle;
	private final Drawable arrow;
	private final int heading;
	/**
	 * Pixels from top of bus/rail icon to draw the arrow
	 */
	private final int arrowTopDiff;
	
	public BusDrawable(Drawable drawable, int heading, Drawable arrow, int arrowTop)
	{
		this.vehicle = drawable;
		this.heading = heading;
		this.arrow = arrow;
		this.arrowTopDiff = arrowTop;
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		//NOTE: 0, 0 is the bottom center point of the bus icon
		
		//first draw the bus
		vehicle.draw(canvas);
		
		//then draw arrow
		if (arrow != null && heading != VehicleLocation.NO_HEADING)
		{
			//put the arrow in the bus window
			
			int arrowLeft = -(arrow.getIntrinsicWidth() / 4);
			int arrowTop = -vehicle.getIntrinsicHeight() + arrowTopDiff;
			
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

		}

	}

	
	
	@Override
	public int getOpacity() {
		return vehicle.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		vehicle.setAlpha(alpha);
		
		if (arrow != null)
		{
			arrow.setAlpha(alpha);
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		vehicle.setColorFilter(cf);
		if (arrow != null)
		{
			arrow.setColorFilter(cf);
		}
	}
	
	@Override
	public int getIntrinsicHeight() {
		//ignoring arrow because it's inside the drawable
		return vehicle.getIntrinsicHeight();
	}
	@Override
	public int getIntrinsicWidth() {
		return vehicle.getIntrinsicWidth();
	}
	
	@Override
	public int getMinimumHeight() {
		return vehicle.getMinimumHeight();
	}
	
	@Override
	public int getMinimumWidth() {
		return vehicle.getMinimumWidth();
	}
	
	@Override
	public Drawable getCurrent() {
		return vehicle.getCurrent();
	}
	
	@Override
	public void clearColorFilter() {
		vehicle.clearColorFilter();
		if (arrow != null)
		{
			arrow.clearColorFilter();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return vehicle.equals(o);
	}
	
	@Override
	public int getChangingConfigurations() {
		return vehicle.getChangingConfigurations();
	}
	
	@Override

	public Drawable.ConstantState getConstantState() {
		return vehicle.getConstantState();
	}
	
	@Override
	public boolean getPadding(Rect padding) {
		return vehicle.getPadding(padding);
	}
	
	@Override
	public int[] getState() {
		return vehicle.getState();
	}
	
	@Override
	public Region getTransparentRegion() {
		return vehicle.getTransparentRegion();
	}
	
	@Override
	public int hashCode() {
		return vehicle.hashCode();
	}
	
	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		vehicle.inflate(r, parser, attrs);
		if (arrow != null)
		{
			arrow.inflate(r, parser, attrs);
		}
	}
	
	@Override
	public void invalidateSelf() {
		vehicle.invalidateSelf();
		if (arrow != null)
		{
			arrow.invalidateSelf();
		}
	}
	
	@Override
	public boolean isStateful() {
		return vehicle.isStateful();
	}
	
	@Override
	public Drawable mutate() {
		//NOTE: even though mutate() returns a Drawable, it also changes the state of the current item (i think)
		//so we need to do it on each one
		
		if (arrow != null)
		{
			arrow.mutate();
		}
		return vehicle.mutate();
	}
	
	@Override
	public void scheduleSelf(Runnable what, long when) {
		vehicle.scheduleSelf(what, when);
		if (arrow != null)
		{
			arrow.scheduleSelf(what, when);
		}
	}
	
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		vehicle.setBounds(left, top, right, bottom);
		if (arrow != null)
		{
			arrow.setBounds(left, top, right, bottom);
		}
	}
	
	@Override
	public void setBounds(Rect bounds) {
		vehicle.setBounds(bounds);
		if (arrow != null)
		{
			arrow.setBounds(bounds);
		}
	}
	
	@Override
	public void setChangingConfigurations(int configs) {
		vehicle.setChangingConfigurations(configs);
		if (arrow != null)
		{
			arrow.setChangingConfigurations(configs);
		}
	}
	
	@Override
	public void setColorFilter(int color, Mode mode) {
		vehicle.setColorFilter(color, mode);
		if (arrow != null)
		{
			arrow.setColorFilter(color, mode);
		}
	}
	
	@Override
	public void setDither(boolean dither) {
		vehicle.setDither(dither);
		if (arrow != null)
		{
			arrow.setDither(dither);
		}
	}
	
	@Override
	public void setFilterBitmap(boolean filter) {
		vehicle.setFilterBitmap(filter);
		if (arrow != null)
		{
			arrow.setFilterBitmap(filter);
		}
	}
	
	@Override
	public boolean setState(int[] stateSet) {
		boolean busModified = vehicle.setState(stateSet);
		boolean arrowModified = (arrow != null ? arrow.setState(stateSet) : false);
		
		//we have to make sure all three are called. If we inline this, the condition may short circuit and not all setState will be called
		return arrowModified || busModified;
	}
	
	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		boolean busModified = vehicle.setVisible(visible, restart);
		boolean arrowModified = (arrow != null ? arrow.setVisible(visible, restart) : false);
		
		//we have to make sure all three are called. If we inline this, the condition may short circuit and not all setVisible will be called
		return arrowModified || busModified;
	}
	
	@Override
	public void unscheduleSelf(Runnable what) {
		vehicle.unscheduleSelf(what);
		if (arrow != null)
		{
			arrow.unscheduleSelf(what);
		}
	}
}
