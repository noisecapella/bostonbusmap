/*
    BostonBusMap
 
    Copyright (C) 2009  George Schneeloch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
package boston.Bus.Map;

import java.util.ArrayList;
import java.util.List;


import boston.Bus.Map.BusLocations.BusLocation;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.widget.Toast;


/**
 * 
 * The bus overlay on the MapView
 * Much of this was borrowed from the helpful tutorial at http://developer.android.com/guide/tutorials/views/hello-mapview.html
 * 
 */
public class BusOverlay extends com.google.android.maps.ItemizedOverlay<com.google.android.maps.OverlayItem> {

	private ArrayList<com.google.android.maps.OverlayItem> overlays = new ArrayList<com.google.android.maps.OverlayItem>();
	private Context context;
	private List<BusLocation> busLocations;
	
	public BusOverlay(Drawable defaultMarker, Context context, List<BusLocation> busLocations) {
		super(boundCenterBottom(defaultMarker));

		this.context = context;
		this.busLocations = busLocations;
	}

	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return overlays.size();
	}
	
	public void addOverlay(com.google.android.maps.OverlayItem item)
	{
		overlays.add(item);
		populate();
	}
	
	@Override
	protected com.google.android.maps.OverlayItem createItem(int i)
	{
		return overlays.get(i);
	}


	public void clear() {
		overlays.clear();
	}

	@Override
	protected boolean onTap(int i)
	{
		//TODO: make this look nicer. Right now it's a box in the center of the screen that stays for a few seconds
		//there's probably a way to do it like Google Maps does it when you click an overlay, produce a bubble with text inside it
		Toast.makeText(context, overlays.get(i).getTitle(), Toast.LENGTH_SHORT).show();
		return true;
	}
	
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		super.draw(canvas, mapView, shadow);
		
		//the following code used to write the bus id on the bus icon, but that looked pretty ugly
		/*TextPaint textPaint = new TextPaint();
		textPaint.setColor(Color.BLACK);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(9);
		
		for (int i = 0; i < size(); i++)
		{
			com.google.android.maps.OverlayItem item = getItem(i);
			
			BusLocation busLocation = busLocations.get(i);
			
			GeoPoint geoPoint = item.getPoint();
			Point point = new Point();
			mapView.getProjection().toPixels(geoPoint, point);
			canvas.drawText(new Integer(busLocation.id).toString(), point.x - 10, point.y - 16, textPaint);
		}
		
		*/
	}
}
