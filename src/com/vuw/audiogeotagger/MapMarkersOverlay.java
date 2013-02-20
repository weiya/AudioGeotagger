package com.vuw.audiogeotagger;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * This class is used to contain the markers added by users
 * @author Weiya Xu
 *
 */
public class MapMarkersOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private GeoPoint point; 
	private long time;
	private boolean visible; 
	private boolean selected;

	public MapMarkersOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
	}
	
	public MapMarkersOverlay(Drawable defaultMarker, GeoPoint point, Context context, long time) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		  this.point = point;
		  this.time = time;
		  this.visible = true;
		  this.selected = false;
		  populate();
		}
	
	public MapMarkersOverlay(Drawable defaultMarker, GeoPoint point, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
		  this.point = point;
		  this.visible = true;
		  this.selected = false;
		  populate();
		}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}

	protected OverlayItem createItem(int arg0) {
		return mOverlays.get(arg0);
	}

	public int size() {
		return mOverlays.size();
	}
	
	protected boolean onTap(int index) {
		  OverlayItem item = mOverlays.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.show();
		  return true;
	}
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);                   

		if(visible) {
			Point screenPts = new Point();
			mapView.getProjection().toPixels(point, screenPts);

			Bitmap bmp = null;
			if(selected) bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.selectedpin);
			else bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pin);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y, null);
		}
	}
	
	public boolean nearby(GeoPoint otherPoint) {
		if(Math.abs(point.getLatitudeE6() - otherPoint.getLatitudeE6()) <= 1000) {
			if(Math.abs(point.getLongitudeE6() - otherPoint.getLongitudeE6()) <= 1000) {
				return true;
			}
		}
		return false;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return this.visible;
	}
	
	public GeoPoint getPoint() {
		return this.point;
	}
	
	public void highlight() {
		this.selected = true;
	}
	
	public void cancelHighlight() {
		this.selected = false;
	}

}