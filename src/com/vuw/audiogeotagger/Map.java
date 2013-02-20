package com.vuw.audiogeotagger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlSerializer;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * This class is used to displays google map on the screen, which allows users to move,
 * zoom in/out using one or two figures. It contains the code for all buttons that enable
 * users to search for a location, change the view mode, draw shapes and markers, undo 
 * last action, start and pause recording and finally save the interview. The xml file is 
 * created and modified in this module when an operation is performed on the map.
 * @author Weiya Xu
 *
 */
public class Map extends MapActivity implements LocationListener {
	private MapView mapView;
	private LocationManager locationManager;
	private MapController mapController;
	private MyLocationOverlay myLocation;
	private boolean enableTool;
	private MediaRecorder recorder;
	private boolean isPause = true;
	private List<String> list = new ArrayList<String>();
	private int idx = 0;
	private String savePath = "/sdcard/Android/data/com.vuw.audiogeotagger/files/";
	private boolean drawMode = false;
	private boolean markerMode = false;
	private List<GeoPoint> points = new ArrayList<GeoPoint>();
	private StringWriter  writer;
	private XmlSerializer serializer;
	private long previousTime;
	private long currentTime;
	private boolean firstRecord = true;
	private String audioFilePath = "/sdcard/Android/data/com.vuw.audiogeotagger/files/";
	private String date;
	private String formatDate;
	public final int MSG_VIEW_LONGPRESS = 10001;
	public final int MSG_VIEW_ADDRESSNAME = 10002;
	public final int MSG_VIEW_ADDRESSNAME_FAIL = 10004;
	public final int MSG_VIEW_LOCATIONLATLNG = 10003;
	public final int MSG_VIEW_LOCATIONLATLNG_FAIL = 10005;
	private GeoPoint locPoint;
	private String query;
	private int lastX, lastY;
	private long lastDownTime;
	private boolean firstTouch = true;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		findControl();

		final Button viewMode = (Button) findViewById(R.id.viewMode);
		viewMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String mode = (String) viewMode.getText();
				if(mode.equals("Standard")) {
					mapView.setSatellite(false);
					viewMode.setText("Satellite");
				} else {
					mapView.setSatellite(true);
					viewMode.setText("Standard");
				}
			}
		});

		final Button editMode = (Button) findViewById(R.id.drawMove);
		editMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String mode = (String) editMode.getText();
				if(mode.equals("Move")) {
					drawMode = false;
					editMode.setText("Draw");
				} else {
					drawMode = true;
					editMode.setText("Move");
				}
			}
		});

		Button save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!isPause) {
					Toast.makeText(getApplicationContext(), "Please stop recording first before saving", Toast.LENGTH_SHORT).show();
					return;
				}
				if(firstRecord) {
					Toast.makeText(getApplicationContext(), "Please start recording in order to save it.", Toast.LENGTH_SHORT).show();
					return;
				}
				try {
					serializer.endTag("", "events");
					serializer.endTag("", "Interview");
					serializer.endDocument();
					OutputStream outputStream = openFileOutput(date + ".xml", MODE_PRIVATE);
					System.out.println(date+".xml"+"===================");
					OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
					outputStreamWriter.write(writer.toString());
					Log.v("XmlSerialize", writer.toString());
					outputStreamWriter.close();
					outputStream.close();
					writer.flush();
					writer.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
				//----------------------
				File audio = new File(audioFilePath);
				FileInputStream fileInputStream = null;
				FileOutputStream fileOutputStream = null;

				try {
					fileOutputStream = new FileOutputStream(audio);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				for(int i = 0; i < list.size(); i++) {   
					try {  
						fileInputStream = new FileInputStream(new File(list.get(i)));  
						byte[] myByte = new byte[fileInputStream.available()];    
						int length = myByte.length;  

						if(i == 0) {  
							while(fileInputStream.read(myByte) != -1) fileOutputStream.write(myByte, 0, length);
						} else {  
							while(fileInputStream.read(myByte) != -1) fileOutputStream.write(myByte, 6, length-6); 
						}  

						fileOutputStream.flush();  
						fileInputStream.close();  
					} catch (Exception e) {  
						e.printStackTrace();  
					}      
				} 

				try {  
					fileOutputStream.close();  
				} catch (IOException e) {  
					e.printStackTrace();
				}
				
				for(int i=0; i<list.size(); i++) {
					File f = new File(list.get(i));
					f.delete();
				}
				//-----------------------
				Intent intent = new Intent();
				intent.putExtra("xmlPath", date + ".xml");
				intent.putExtra("audioPath", audioFilePath);
				if(!getIntent().getStringExtra("title").equals("")) {
					intent.putExtra("title", getIntent().getStringExtra("title"));
				} else {
					intent.putExtra("title", formatDate);
				}
				intent.putExtra("description", getIntent().getStringExtra("description"));
				intent.setClass(Map.this, Save.class);
				Map.this.startActivity(intent);
				
				finish();
			}
		});

		final Button record = (Button) findViewById(R.id.record);
		record.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(firstRecord) {
					date = getSimpleTimeStamp();
					formatDate = getTimeStamp();
					audioFilePath += date + ".amr"; 
					writer = new StringWriter();
					serializer = Xml.newSerializer();
					try {
						serializer.setOutput(writer);
						serializer.startDocument("utf-8", true);
						serializer.startTag("", "Interview");
						serializer.startTag("", "title");
						if(!getIntent().getStringExtra("title").equals("")) {
							serializer.text(getIntent().getStringExtra("title"));
						} else {
							serializer.text(formatDate);
						}
						serializer.endTag("", "title");
						serializer.startTag("", "description");
						serializer.text(getIntent().getStringExtra("description"));
						serializer.endTag("", "description");
						serializer.startTag("", "audio");
						serializer.text(audioFilePath);
						serializer.endTag("", "audio");
						serializer.startTag("", "events");
					} catch(Exception e) {
						e.printStackTrace();
					}

				}

				if(!isPause) {
					isPause = true;
					record.setText("Start");
					list.add(savePath);
					recorder.stop();
					recorder.release();
					return;
				}
				//-------------
				try {
					GeoPoint point = enhancedCenter(mapView.getMapCenter());
					serializer.startTag("", "event");
					serializer.startTag("", "type");
					serializer.text("initial_view");
					serializer.endTag("", "type");
					serializer.startTag("", "latitude");
					serializer.text(String.valueOf(point.getLatitudeE6()));
					serializer.endTag("", "latitude");
					serializer.startTag("", "longitude");
					serializer.text(String.valueOf(point.getLongitudeE6()));
					serializer.endTag("", "longitude");
					serializer.startTag("", "level");
					serializer.text(String.valueOf(mapView.getZoomLevel()));
					serializer.endTag("", "level");
					serializer.startTag("", "timestamp");
					serializer.text(formatDate);
					serializer.endTag("", "timestamp");
					serializer.startTag("", "seconds");
					serializer.text("0");
					serializer.endTag("", "seconds");
					serializer.endTag("", "event");
				} catch(Exception e) {
					e.printStackTrace();
				}
				//-------------
				savePath = "/sdcard/Android/data/com.vuw.audiogeotagger/files/" + (idx++) + ".amr";
				recorder = new MediaRecorder();
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				recorder.setOutputFile(savePath);
				isPause = false;
				record.setText("Pause");
				try {
					recorder.prepare();
					recorder.start();
				} catch(IllegalStateException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				}
				previousTime = System.currentTimeMillis();
				if(firstRecord) {
					firstRecord = false;
				}
			}
		});

		final Drawable marker = getResources().getDrawable(R.drawable.pin); 
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

		mapView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_MOVE) {
					if(firstTouch) {
						markerMode = isLongPressed(lastX, lastY, event.getX(), event.getY(), lastDownTime, System.currentTimeMillis(), 200);
					}
					if(drawMode && !markerMode) {
						points.add(mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY()));
						return true;
					} else if(drawMode && markerMode) {
						GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
						currentTime = System.currentTimeMillis();
						long idle = currentTime - previousTime;
						previousTime = currentTime;
						try {
							serializer.startTag("", "event");
							serializer.startTag("", "type");
							serializer.text("dragmarker");
							serializer.endTag("", "type");
							serializer.startTag("", "latitude");
							serializer.text(String.valueOf(p.getLatitudeE6()));
							serializer.endTag("", "latitude");
							serializer.startTag("", "longitude");
							serializer.text(String.valueOf(p.getLongitudeE6()));
							serializer.endTag("", "longitude");
							serializer.startTag("", "timestamp");
							serializer.text(getTimeStamp());
							serializer.endTag("", "timestamp");
							serializer.startTag("", "seconds");
							serializer.text(String.valueOf(idle));
							serializer.endTag("", "seconds");
						} catch(Exception e) {
							e.printStackTrace();
						}
						if(firstTouch) {
							MapMarkersOverlay markerOverlay = new MapMarkersOverlay(marker, p, mapView.getContext());  
							int i = removeMarker(p);
							if(i != -1) { 
								MapMarkersOverlay mol = (MapMarkersOverlay) mapView.getOverlays().get(i);
								try {
									serializer.startTag("", "targetLatitude");
									serializer.text(String.valueOf(mol.getPoint().getLatitudeE6()));
									serializer.endTag("", "targetLatitude");
									serializer.startTag("", "targetLongitude");
									serializer.text(String.valueOf(mol.getPoint().getLongitudeE6()));
									serializer.endTag("", "targetLongitude");
								} catch(Exception e) {}
								mapView.getOverlays().remove(i);
								mapView.getOverlays().add(markerOverlay);
								firstTouch = false;
								mapView.invalidate();
							}
						} else {
							MapMarkersOverlay mol = (MapMarkersOverlay) mapView.getOverlays().get(mapView.getOverlays().size()-1);
							
							try {
								serializer.startTag("", "targetLatitude");
								serializer.text(String.valueOf(mol.getPoint().getLatitudeE6()));
								serializer.endTag("", "targetLatitude");
								serializer.startTag("", "targetLongitude");
								serializer.text(String.valueOf(mol.getPoint().getLongitudeE6()));
								serializer.endTag("", "targetLongitude");
							} catch(Exception e) {}
							MapMarkersOverlay markerOverlay = new MapMarkersOverlay(marker, p, mapView.getContext());  
							mapView.getOverlays().remove(mapView.getOverlays().size() - 1);
							mapView.getOverlays().add(markerOverlay);
							mapView.invalidate();
						}
						try {
							serializer.endTag("", "event");
						} catch(Exception e) {}
						
						return true;
					} else {
						try {
							currentTime = System.currentTimeMillis();
							long idle = currentTime - previousTime;
							previousTime = currentTime;
							GeoPoint point = enhancedCenter(mapView.getMapCenter());
							serializer.startTag("", "event");
							serializer.startTag("", "type");
							serializer.text("move");
							serializer.endTag("", "type");
							serializer.startTag("", "latitude");
							serializer.text(String.valueOf(point.getLatitudeE6()));
							serializer.endTag("", "latitude");
							serializer.startTag("", "longitude");
							serializer.text(String.valueOf(point.getLongitudeE6()));
							serializer.endTag("", "longitude");
							serializer.startTag("", "timestamp");
							serializer.text(getTimeStamp());
							serializer.endTag("", "timestamp");
							serializer.startTag("", "level");
							serializer.text(String.valueOf(mapView.getZoomLevel()));
							serializer.endTag("", "level");
							serializer.startTag("", "seconds");
							serializer.text(String.valueOf(idle));
							serializer.endTag("", "seconds");
							serializer.endTag("", "event");
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
				if (event.getAction() == MotionEvent.ACTION_UP && drawMode) {
					if(markerMode) {
						markerMode = false;
						points = new ArrayList<GeoPoint>();
						return false;
					}
					if(points.size() > 10) {
						currentTime = System.currentTimeMillis();
						long idle = currentTime - previousTime;
						previousTime = currentTime;
						MapLinesOverlay linesOverlay = new MapLinesOverlay(marker, points);
						mapView.getOverlays().add(linesOverlay);
						mapView.invalidate();
						try {
							serializer.startTag("", "event");
							serializer.startTag("", "type");
							serializer.text("line");
							serializer.endTag("", "type");
							for(GeoPoint p: points) {
								serializer.startTag("", "latitude");
								serializer.text(String.valueOf(p.getLatitudeE6()));
								serializer.endTag("", "latitude");
								serializer.startTag("", "longitude");
								serializer.text(String.valueOf(p.getLongitudeE6()));
								serializer.endTag("", "longitude");
							}
							serializer.startTag("", "timestamp");
							serializer.text(getTimeStamp());
							serializer.endTag("", "timestamp");
							serializer.startTag("", "seconds");
							serializer.text(String.valueOf(idle));
							serializer.endTag("", "seconds");
							serializer.endTag("", "event");
						} catch(Exception e) {
							e.printStackTrace();
						}
						points = new ArrayList<GeoPoint>();
					} else {
						currentTime = System.currentTimeMillis();
						long idle = currentTime - previousTime;
						previousTime = currentTime;
						GeoPoint point = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
						MapMarkersOverlay markerOverlay = new MapMarkersOverlay(marker, point, mapView.getContext());  
						mapView.getOverlays().add(markerOverlay);
						points = new ArrayList<GeoPoint>();
						mapView.invalidate();
						try {
							serializer.startTag("", "event");
							serializer.startTag("", "type");
							serializer.text("marker");
							serializer.endTag("", "type");
							serializer.startTag("", "latitude");
							serializer.text(String.valueOf(point.getLatitudeE6()));
							serializer.endTag("", "latitude");
							serializer.startTag("", "longitude");
							serializer.text(String.valueOf(point.getLongitudeE6()));
							serializer.endTag("", "longitude");
							serializer.startTag("", "timestamp");
							serializer.text(getTimeStamp());
							serializer.endTag("", "timestamp");
							serializer.startTag("", "seconds");
							serializer.text(String.valueOf(idle));
							serializer.endTag("", "seconds");
							serializer.endTag("", "event");
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					lastX = (int) event.getX();
					lastY = (int) event.getY();
					lastDownTime = System.currentTimeMillis();
					firstTouch = true;
				}
				return false;
			}
		});
	}

	private void init() {
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			new AlertDialog.Builder(Map.this).setTitle("Improve location accuracy").setMessage("To enhance your Maps experience:\n\nTurn on GPS")
			.setCancelable(false).setPositiveButton("Settings", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}).setNegativeButton("Skip", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(Map.this, "GPS is unavailable", Toast.LENGTH_SHORT).show();
				}
			}).show();

		} else {
			enableMyLocation();
			enableTool = true;
		}
	}

	private void findControl() {
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(false);

		mapController = mapView.getController();
		mapController.setZoom(16);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, Map.this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, Map.this);
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

	private void enableMyLocation() {
		List<Overlay> overlays = mapView.getOverlays();
		myLocation = new MyLocationOverlay(this, mapView);
		myLocation.enableMyLocation();
		myLocation.runOnFirstFix(new Runnable() {
			public void run() {
				mapController.animateTo(myLocation.getMyLocation());
			}
		});
		overlays.add(myLocation);
	}

	protected void onResume() {
		super.onResume();
		if (enableTool) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, Map.this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, Map.this);
			myLocation.enableMyLocation();
		} else {
			init();
		}
	}

	protected void onPause() {
		super.onPause();
		if (enableTool) {
			locationManager.removeUpdates(Map.this);
			myLocation.disableMyLocation();
		}
	}

	public void onLocationChanged(Location location) {}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public GeoPoint enhancedCenter(GeoPoint point) {
		Projection projection = mapView.getProjection();
		GeoPoint temp = new GeoPoint(0, 0);
		Point p = new Point();
		projection.toPixels(temp, p);
		temp = projection.fromPixels(p.x, p.y);
		point = new GeoPoint(point.getLatitudeE6() - temp.getLatitudeE6(), point.getLongitudeE6() - temp.getLongitudeE6());
		return point;
	}

	private String getTimeStamp() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     
		return (sDateFormat.format(new java.util.Date())); 
	}

	private String getSimpleTimeStamp() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");     
		return (sDateFormat.format(new java.util.Date())); 
	}

	public boolean onSearchRequested(){
		startSearch(null, false, null, false);
		return true;
	}

	public void onNewIntent(Intent intent) {  
		super.onNewIntent(intent);
		query = intent.getStringExtra(SearchManager.QUERY);
		SearchRecentSuggestions suggestions=new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
		suggestions.saveRecentQuery(query, null);
		CommonHelper.showProgress(this, "Searching for:\n" + query);
		new Thread(new Runnable() {
			public void run() {
				Address address;
				int count = 0;
				while(true) {
					count++;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					address = searchLocationByName(query);
					if(address == null && count > 5) {
						Message msg1 = new Message();
						msg1.what = MSG_VIEW_LOCATIONLATLNG_FAIL;
						mHandler.sendMessage(msg1);
						break;
					} else if(address == null) {
						continue;
					} else {
						break;
					}
				}

				if( address != null || count <= 5 ){
					Message msg = new Message();
					msg.what = MSG_VIEW_LOCATIONLATLNG;
					msg.obj = address;
					mHandler.sendMessage(msg);
				}
			}
		}).start();
	}

	private Address searchLocationByName(String addressName){
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.ENGLISH);
		try {
			List<Address> addresses = geoCoder.getFromLocationName(addressName, 1);
			Address address_send = null;
			for(Address address : addresses) {
				locPoint = new GeoPoint((int)(address.getLatitude() * 1E6), (int)(address.getLongitude() * 1E6));
				address.getAddressLine(1);
				address_send = address;
			}
			return address_send;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_VIEW_LOCATIONLATLNG:
				CommonHelper.closeProgress();
				Address address = (Address)msg.obj;
				locPoint = new GeoPoint((int)(address.getLatitude() * 1E6), (int)(address.getLongitude() * 1E6));
				mapView.getController().animateTo(locPoint);
				Drawable pin = getResources().getDrawable(R.drawable.pin); 
				pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
				mapView.getOverlays().add(new MapMarkersOverlay(pin, locPoint, mapView.getContext()));
				mapView.invalidate();
				break;
			case MSG_VIEW_LOCATIONLATLNG_FAIL:
				CommonHelper.closeProgress();
				Toast.makeText(Map.this, "Search failed", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	public void searchMap(View view) {
		onSearchRequested();
	}
	
	private boolean isLongPressed(float lastX, float lastY, float thisX,float thisY, long lastDownTime, long thisEventTime, long longPressTime) {
		float offsetX = Math.abs(thisX - lastX);
		float offsetY = Math.abs(thisY - lastY);
		long intervalTime = thisEventTime - lastDownTime;
		if(offsetX <=10 && offsetY<=10 && intervalTime >= longPressTime) {
			return true;
		}
		return false;
	}
	
	private int removeMarker(GeoPoint point) {
		for(int i = 0; i < mapView.getOverlays().size(); i++) {
			if(mapView.getOverlays().get(i) instanceof MapMarkersOverlay) {
				MapMarkersOverlay overlay = (MapMarkersOverlay) mapView.getOverlays().get(i);
				if(overlay.nearby(point)) return i;
				}
		}
		return -1;
	}
	
	public void undoButton(View view) {
		currentTime = System.currentTimeMillis();
		long idle = currentTime - previousTime;
		previousTime = currentTime;
		
		try {
			serializer.startTag("", "event");
			serializer.startTag("", "type");
			serializer.text("undo");
			serializer.endTag("", "type");
			serializer.startTag("", "timestamp");
			serializer.text(getTimeStamp());
			serializer.endTag("", "timestamp");
			serializer.startTag("", "seconds");
			serializer.text(String.valueOf(idle));
			serializer.endTag("", "seconds");
			serializer.endTag("", "event");
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(mapView.getOverlays().size() == 1) return;
		mapView.getOverlays().remove(mapView.getOverlays().size() - 1);
		mapView.invalidate();
	}
}    