package boston.Bus.Map;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Matrix;
import android.util.AttributeSet;

/**
 * This draws a bus with an arrow in it. There might be a simpler way than subclassing Drawable; if so let me know
 * 
 * 
 * 
 * @author schneg
 *
 */
public class BusDrawable extends Drawable {
	private final Drawable drawable;
	private final Drawable arrow;
	private final int heading;
	
	public BusDrawable(Drawable drawable, int heading, Drawable arrow)
	{
		this.drawable = drawable;
		this.heading = heading;
		this.arrow = arrow;
	}
	
	@Override
	public void draw(Canvas canvas) {
		//put the arrow in the bus window
		
		int arrowLeft = -(arrow.getIntrinsicWidth() / 4);
		int arrowTop = -drawable.getIntrinsicHeight() + 7;
		int arrowWidth = (int)(arrow.getIntrinsicWidth() * 0.60); 
		int arrowHeight = (int)(arrow.getIntrinsicHeight() * 0.60);
		int arrowRight = arrowLeft + arrowWidth;
		int arrowBottom = arrowTop + arrowHeight;
		
		//first draw the bus	
		drawable.draw(canvas);

		
		arrow.setBounds(arrowLeft, arrowTop, arrowRight, arrowBottom);
		
		canvas.save();
		//set rotation pivot at the center of the arrow image
		canvas.rotate(heading, arrowLeft + arrowWidth/2, arrowTop + arrowHeight / 2);
		
		Rect rect = arrow.getBounds();
		arrow.draw(canvas);
		arrow.setBounds(rect);
		canvas.restore();

		
		
	}

	
	
	@Override
	public int getOpacity() {
		// TODO Auto-generated method stubpadding
		arrow.getOpacity();
		return drawable.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		drawable.setAlpha(alpha);
		arrow.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		drawable.setColorFilter(cf);
		arrow.setColorFilter(cf);
	}
	
	@Override
	public int getIntrinsicHeight() {
		// TODO Auto-generated method stub
		return drawable.getIntrinsicHeight() + arrow.getIntrinsicHeight();
	}
	@Override
	public int getIntrinsicWidth() {
		// TODO Auto-generated method stub
		return drawable.getIntrinsicWidth() + arrow.getIntrinsicWidth();
	}
	
	@Override
	public int getMinimumHeight() {
		// TODO Auto-generated method stub
		return drawable.getMinimumHeight() + arrow.getMinimumHeight();
	}
	
	@Override
	public int getMinimumWidth() {
		// TODO Auto-generated method stub
		return drawable.getMinimumWidth() + arrow.getMinimumWidth();
	}
	
	@Override
	public Drawable getCurrent() {
		// TODO Auto-generated method stub
		arrow.getCurrent();
		return drawable.getCurrent();
	}
	
	@Override
	public void clearColorFilter() {
		// TODO Auto-generated method stub
		arrow.clearColorFilter();
		drawable.clearColorFilter();
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return drawable.equals(o);
	}
	
	@Override
	public int getChangingConfigurations() {
		// TODO Auto-generated method stub
		arrow.getChangingConfigurations();
		return drawable.getChangingConfigurations();
	}
	
	@Override
	public ConstantState getConstantState() {
		// TODO Auto-generated method stub
		arrow.getConstantState();
		return drawable.getConstantState();
	}
	
	@Override
	public boolean getPadding(Rect padding) {
		// TODO Auto-generated method stub
		arrow.getPadding(padding);
		return drawable.getPadding(padding);
	}
	
	@Override
	public int[] getState() {
		// TODO Auto-generated method stub
		arrow.getState();
		return drawable.getState();
	}
	
	@Override
	public Region getTransparentRegion() {
		// TODO Auto-generated method stub
		arrow.getTransparentRegion();
		return drawable.getTransparentRegion();
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return drawable.hashCode();
	}
	
	@Override
	public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		drawable.inflate(r, parser, attrs);
		arrow.inflate(r, parser, attrs);
	}
	
	@Override
	public void invalidateSelf() {
		// TODO Auto-generated method stub
		drawable.invalidateSelf();
		arrow.invalidateSelf();
	}
	
	@Override
	public boolean isStateful() {
		// TODO Auto-generated method stub
		arrow.isStateful();
		return drawable.isStateful();
	}
	
	@Override
	public Drawable mutate() {
		// TODO Auto-generated method stub
		arrow.mutate();
		return drawable.mutate();
	}
	
	@Override
	public void scheduleSelf(Runnable what, long when) {
		// TODO Auto-generated method stub
		drawable.scheduleSelf(what, when);
		arrow.scheduleSelf(what, when);
	}
	
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		drawable.setBounds(left, top, right, bottom);
		arrow.setBounds(left, top, right, bottom);
	}
	
	@Override
	public void setBounds(Rect bounds) {
		// TODO Auto-generated method stub
		drawable.setBounds(bounds);
		arrow.setBounds(bounds);
	}
	
	@Override
	public void setChangingConfigurations(int configs) {
		// TODO Auto-generated method stub
		drawable.setChangingConfigurations(configs);
		arrow.setChangingConfigurations(configs);
	}
	
	@Override
	public void setColorFilter(int color, Mode mode) {
		// TODO Auto-generated method stub
		drawable.setColorFilter(color, mode);
		arrow.setColorFilter(color, mode);
	}
	
	@Override
	public void setDither(boolean dither) {
		// TODO Auto-generated method stub
		arrow.setDither(dither);
		drawable.setDither(dither);
	}
	
	@Override
	public void setFilterBitmap(boolean filter) {
		// TODO Auto-generated method stub
		arrow.setFilterBitmap(filter);
		drawable.setFilterBitmap(filter);
	}
	
	@Override
	public boolean setState(int[] stateSet) {
		// TODO Auto-generated method stub
		arrow.setState(stateSet);
		return drawable.setState(stateSet);
	}
	
	@Override
	public boolean setVisible(boolean visible, boolean restart) {
		// TODO Auto-generated method stub
		arrow.setVisible(visible, restart);
		return drawable.setVisible(visible, restart);
	}
	
	@Override
	public void unscheduleSelf(Runnable what) {
		// TODO Auto-generated method stub
		drawable.unscheduleSelf(what);
		arrow.unscheduleSelf(what);
	}
}
