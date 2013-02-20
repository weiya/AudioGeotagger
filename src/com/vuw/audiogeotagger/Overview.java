package com.vuw.audiogeotagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * This class provides the information of all lines and markers drawn during the interview
 * and highlights selected item on the map. This module makes use of a sliding menu to
 * display the detailed information that can be shown and hidden by swiping on the screen.
 * @author Weiya Xu
 *
 */
public class Overview extends MapActivity implements OnTouchListener, OnGestureListener {
	private LinearLayout layout_left;
	private LinearLayout layout_right;
	private TextView titleView;
	private ImageView slidingIcon;
	private ListView listView;
	private MapView mapView;
	private MapController mapController;
	private boolean hasMeasured = false;
	private int MAX_WIDTH = 0;
	private final static int SPEED = 30;
	private GestureDetector mGestureDetector;
	private boolean isScrolling = false;
	private float mScrollX;
	private int window_width;
	private List<HashMap<String, String>> itemsList;
	private SimpleAdapter listViewAdapter;
	private List<MapObject> objects;
	private ProgressDialog dialog;
	private int lastSelected = -1;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.overview);
		
		mapView = (MapView) findViewById(R.id.overview_map);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(false);

		mapController = mapView.getController();
		mapController.setZoom(16);
	
		InitView();
	}

	private void InitView() {
		layout_left = (LinearLayout) findViewById(R.id.layout_left);
		layout_right = (LinearLayout) findViewById(R.id.layout_right);
		slidingIcon = (ImageView) findViewById(R.id.detail_image);
		titleView = (TextView) findViewById(R.id.overview_title);
		titleView.setText(getIntent().getStringExtra("title"));
		listView = (ListView) findViewById(R.id.overview_list);
		itemsList = new ArrayList<HashMap<String, String>>();
		initListView();
		slidingIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LayoutParams layoutParams = (LayoutParams) layout_left.getLayoutParams();
				if (layoutParams.leftMargin >= 0) {
					new AsynMove().execute(-SPEED);
				} else {
					new AsynMove().execute(SPEED);
				}
			}
		});
		
		layout_left.setOnTouchListener(this);  
		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setIsLongpressEnabled(false);
		getMaxWidth();
	}
	
	private void initListView() {
		CommonHelper.showProgress(this, "Please wait while retrieving information...");
		
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				try {
					Parser parser = new Parser(openFileInput(getIntent().getStringExtra("xmlPath")));
					parser.parse();
					objects = parser.getObjects();
				} catch(Exception e) {}
				Drawable pin = mapView.getResources().getDrawable(R.drawable.pin);
				pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
				HashMap<String, String> map = new HashMap<String, String>();
				boolean initialView = true;
				for(MapObject object: objects) {
					String type = object.getType();
					if(type.equals("line")) {
						mapView.getOverlays().add(new MapLinesOverlay(pin, object.getPoints(), object.getCurrentseconds()));
					} else if(type.equals("marker")) {
						mapView.getOverlays().add(new MapMarkersOverlay(pin, object.getPoints().get(0), mapView.getContext(), object.getCurrentseconds()));
					} else if(type.equals("view") && initialView) {
						mapController.setCenter(object.getPoints().get(0));
						initialView = false;
					}
					if(type.equals("line") || type.equals("marker")) {
						map.put("address", getLocationAddress(object.getPoints().get(0)));
						map.put("date", object.getTimeStamp());
						itemsList.add(map);
						map = new HashMap<String, String>();
					}
				}
				mHandler.sendEmptyMessage(0);
			}
		}).start();

		listViewAdapter = new SimpleAdapter(getApplicationContext(), itemsList, R.layout.overviewitem, new String[] { "address", "date" }, 
				new int[] {R.id.address, R.id.date});
		listView.setAdapter(listViewAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				highlight(position, view);
			}
		});
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			mapView.invalidate();
			listViewAdapter.notifyDataSetChanged();
			CommonHelper.closeProgress();
		}
	};
	
	private void highlight(int position, View view) {		
		if(lastSelected != -1) listView.getChildAt(lastSelected).setBackgroundDrawable(null);
		view.setBackgroundDrawable(getResources().getDrawable(R.drawable.selecteditem));
		
		if(lastSelected != -1) {
			Overlay overlay = mapView.getOverlays().get(lastSelected);
			if(overlay instanceof MapLinesOverlay) ((MapLinesOverlay) overlay).cancelHighlight();
			else ((MapMarkersOverlay) overlay).cancelHighlight();
		}
		Overlay overlay = mapView.getOverlays().get(position);
		if(overlay instanceof MapLinesOverlay) {
			MapLinesOverlay mlo = (MapLinesOverlay) overlay;
			mlo.highlight();
			mapController.animateTo(mlo.getPoints().get(mlo.getPoints().size()/2));
		} else {
			MapMarkersOverlay mmo = (MapMarkersOverlay) overlay;
			mmo.highlight();
			mapController.animateTo(mmo.getPoint());
		}
		lastSelected = position;

		mapView.invalidate();
		listViewAdapter.notifyDataSetChanged();
	}
	
	private String getLocationAddress(GeoPoint point) {
		String addr = "";
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			List<Address> addresses = geoCoder.getFromLocation(point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6, 1);
			Address address = addresses.get(0);
			int maxLine = address.getMaxAddressLineIndex();
			if(maxLine >= 2){
				addr =  address.getAddressLine(1) + ", " + address.getAddressLine(2);
			}else {
				addr = address.getAddressLine(1);
			}
		} catch (IOException e) {
			addr = "";
			e.printStackTrace();
		}
		return addr;
	}

	private void getMaxWidth() {
		ViewTreeObserver viewTreeObserver = layout_left.getViewTreeObserver();
		viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {
			public boolean onPreDraw() {
				if (!hasMeasured) {
					window_width = getWindowManager().getDefaultDisplay().getWidth();
					LayoutParams layoutParams = (LayoutParams) layout_left.getLayoutParams();
					layoutParams.width = window_width;
					layout_left.setLayoutParams(layoutParams);
					MAX_WIDTH = layout_right.getWidth();
					hasMeasured = true;
				}
				return true;
			}
		});
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP && isScrolling) {
			LayoutParams layoutParams = (LayoutParams) layout_left.getLayoutParams();
			if (layoutParams.leftMargin < -window_width / 2) {
				new AsynMove().execute(-SPEED);
			} else {
				new AsynMove().execute(SPEED);
			}
		}
		return mGestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		mScrollX = 0;
		isScrolling = false;
		return true;
	}

	public void onShowPress(MotionEvent e) {}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		isScrolling = true;
		mScrollX += distanceX;
		LayoutParams layoutParams = (LayoutParams) layout_left.getLayoutParams();
		layoutParams.leftMargin -= mScrollX;
		if (layoutParams.leftMargin >= 0) {
			isScrolling = false;
			layoutParams.leftMargin = 0;
		} else if (layoutParams.leftMargin <= -MAX_WIDTH) {
			isScrolling = false;
			layoutParams.leftMargin = -MAX_WIDTH;
		}
		layout_left.setLayoutParams(layoutParams);
		return false;
	}

	public void onLongPress(MotionEvent e) {}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	private class AsynMove extends AsyncTask<Integer, Integer, Void> {
		protected Void doInBackground(Integer... params) {
			int times = 0;
			if (MAX_WIDTH % Math.abs(params[0]) == 0)
				times = MAX_WIDTH / Math.abs(params[0]);
			else
				times = MAX_WIDTH / Math.abs(params[0]) + 1;// ”–”‡ ˝

			for (int i = 0; i < times; i++) {
				publishProgress(params[0]);
				try {
					Thread.sleep(Math.abs(params[0]));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		protected void onProgressUpdate(Integer... values) {
			LayoutParams layoutParams = (LayoutParams) layout_left.getLayoutParams();
			if (values[0] > 0) {
				layoutParams.leftMargin = Math.min(layoutParams.leftMargin + values[0], 0);
			} else {
				layoutParams.leftMargin = Math.max(layoutParams.leftMargin + values[0], -MAX_WIDTH);
			}
			layout_left.setLayoutParams(layoutParams);
		}
	}

	protected boolean isRouteDisplayed() {
		return false;
	}
}