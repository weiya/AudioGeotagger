package com.vuw.audiogeotagger;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * This is the overlay used to contain the shapes drawn by users
 * @author Weiya Xu
 *
 */
public class MapLinesOverlay extends ItemizedOverlay<OverlayItem> {
	private List<GeoPoint> points;
	private Paint paint;
	private long time;
	private boolean visible;

	public MapLinesOverlay(Drawable defaultMarker, List<GeoPoint> points, long time) {
		super(boundCenterBottom(defaultMarker));
		this.points = points;
		this.time = time;
		this.visible = true;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3);
		
		populate();
	}
	
	public MapLinesOverlay(Drawable defaultMarker, List<GeoPoint> points) {
		super(boundCenterBottom(defaultMarker));
		this.points = points;
		this.visible = true;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3);
		
		populate();
	}
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);  
		
		if(visible) {
			Point point1 = new Point();
			Point point2 = new Point();
			Projection projection = mapView.getProjection();

			for(int i=0; i<points.size()-1; i++) {
				projection.toPixels(points.get(i), point1);
				projection.toPixels(points.get(i+1), point2);
				canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint);
			}
		}
	}

	protected OverlayItem createItem(int arg0) {
		return null;
	}

	public int size() {
		return 0;
	}
	
	public long getTime() {
		return time;
	}
	
	public List<GeoPoint> getPoints() {
		return this.points;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public void highlight() {
		paint.setColor(Color.RED);
		paint.setStrokeWidth(5);
	}
	
	public void cancelHighlight() {
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3);
	}
}