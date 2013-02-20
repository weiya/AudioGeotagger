package com.vuw.audiogeotagger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * This class realize the play functionality of the interview. This module employs
 * a separate thread to play a list of MapObject derived from parsing the xml file.
 * For forward and rewind, the current thread is stopped and a new thread is started
 * to play the interview at the specified location
 * @author Weiya Xu
 *
 */
public class Preview extends MapActivity {
	private MapView mapView;
	private MapController mapController;
	private Parser parser;
	private boolean first = true;
	private playThread pt;
	private Handler percentHandler = new Handler();
	private SeekBar mSeekBar = null;
	private MediaPlayer mediaPlayer = null;
	private TextView currentTime = null;
	private TextView totalTime = null;
	private TextView title = null;
	private Button button = null;
	private boolean canPause = false;
	private List<MapObject> objects;
	private Drawable pin;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);

		mapView = (MapView) findViewById(R.id.preview);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(false);

		mapController = mapView.getController();
		mapController.setZoom(16);

		mediaPlayer = new MediaPlayer();
		mSeekBar = (SeekBar) findViewById(R.id.seek);
		currentTime = (TextView) findViewById(R.id.currentTime);
		totalTime = (TextView) findViewById(R.id.totalTime);
		title = (TextView) findViewById(R.id.previewtitle);
		title.setText(getIntent().getStringExtra("title"));
		button = (Button) findViewById(R.id.playButton);

		pin = mapView.getResources().getDrawable(R.drawable.pin); 
		pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());

		try {
			parser = new Parser(openFileInput(getIntent().getStringExtra("xmlPath")));
			parser.parse();
			objects = parser.getObjects();
			initOverlays(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				final int dest = seekBar.getProgress();
				final int mMax = mediaPlayer.getDuration();
				final int sMax = mSeekBar.getMax();
				mediaPlayer.seekTo(mMax*dest/sMax - 1500);
				pt = new playThread(mMax*dest/sMax);
				pt.start();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				pt.interrupt();
				pt.setFlag();
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}			
		});

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				button.setBackgroundDrawable(getResources().getDrawable(R.drawable.play));
				pt.setFlag();
				first = true;
				initOverlays(false);
			}
		});

		playMusic(getIntent().getStringExtra("audioPath"));

		int mMax = mediaPlayer.getDuration();
		int mins = mMax/1000/60;
		int seconds = mMax/1000 - 60 * mins;
		String m = null, s = null;
		if(mins < 10) m = "0" + mins;
		else m = mins + "";
		if(seconds < 10) s = "0" + seconds;
		else s = seconds + "";
		totalTime.setText(m + ":" + s);
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			percentHandler.removeCallbacks(start);
			percentHandler.removeCallbacks(updatesb);
			if(pt != null) pt.setFlag();
			mediaPlayer.stop();
			mediaPlayer.release();
			this.finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void playMusic(String path) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		} catch(Exception e ) {

		}
	}

	public void startSeekBarUpdate() {
		percentHandler.post(start);
	}

	Runnable start = new Runnable() {
		@Override
		public void run() {
			percentHandler.post(updatesb);
		}
	};

	Runnable updatesb = new Runnable() {
		public void run() {
			int position = mediaPlayer.getCurrentPosition();
			int mMax = mediaPlayer.getDuration();
			int sMax = mSeekBar.getMax();
			mSeekBar.setProgress(position*sMax/mMax);
			int mins = position/1000/60;
			int seconds = position/1000 - 60 * mins;
			String m = null, s = null;
			if(mins < 10) m = "0" + mins;
			else m = mins + "";
			if(seconds < 10) s = "0" + seconds;
			else s = seconds + "";
			currentTime.setText(m + ":" + s);
			percentHandler.postDelayed(updatesb, 1000);
		}
	};

	public void playButton(View view) {
		if(canPause == false) {
			mediaPlayer.start();
			startSeekBarUpdate();
			if(first) {
				pt = new playThread(0);
				pt.start();
				first = false;
			} else {
				pt.setPause(false);
			}
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
			canPause = true;
		} else {
			startSeekBarUpdate();
			mediaPlayer.pause();
			pt.setPause(true);
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.play));
			canPause = false;
		}
	}

	class playThread extends Thread {
		long location;
		boolean changed = false;
		boolean pause = false;
		public playThread(long location) {
			super();
			this.location = location;
		}
		public void run() {
			int i = 0;
			int index = 0;

			if(location != 0) {
				index = find(location);
				i= seek(location);
				hideOverlays(index, i);
				
			} 

			boolean first = true;
			for(; i<objects.size(); i++) {
				if(changed) break;
				MapObject mo = objects.get(i);
				String type = mo.getType();
				if(type.equals("line")) {
					try {
						while(pause) Thread.sleep(1);
						if(!first) Thread.sleep(mo.getIdle());
					} catch(Exception e) {}
					if(changed) break;
					mapView.getController().animateTo(getView(i));
					if(changed) break;
					showOverlay(mo);
				} else if(type.equals("marker")) {
					try {
						while(pause) Thread.sleep(1);
						if(!first) Thread.sleep(mo.getIdle());
					} catch(Exception e) {}

					if(changed) break;
					mapView.getController().animateTo(getView(i));
					if(changed) break;
					showOverlay(mo);
				} else if(type.equals("move")) {
					try {
						while(pause) Thread.sleep(1);
						if(!first) Thread.sleep(mo.getIdle());
					} catch(Exception e) {}
					if(changed) break;
					mapView.getController().animateTo(mo.getPoints().get(0));
					if(changed) break;
					mapView.getController().setZoom(mo.getLevel());

				} else if(type.equals("dragmarker")) {
					try {
						while(pause) Thread.sleep(1);
						if(!first) Thread.sleep(mo.getIdle());
					} catch(Exception e) {}

					if(changed) break;
					mapView.getController().animateTo(getView(i));
					if(changed) break;
					hideMarker(mo);
					
					if(changed) break;
					showOverlay(mo);
					
				} else if(type.equals("undo")) {
					try {
						while(pause) Thread.sleep(1);
						if(!first) Thread.sleep(mo.getIdle());
					} catch(Exception e) {}
					if(changed) break;
					undoOverlay(i);
				} else {
					if(changed) break;
					mapView.getController().setCenter(mo.getPoints().get(0));
					if(changed) break;
					mapView.getController().setZoom(mo.getLevel());
				}

				first = false;
				if(!changed) {
					while(pause) {
						try {
							Thread.sleep(1);
						} catch(Exception e) {}
					}
					mapView.postInvalidate();
				}
				else break;
			}
		}

		public void setFlag() {
			changed = true;
		}

		public void setStart() {
			changed = false;;
		}
		
		public void setPause(boolean status) {
			pause = status;
		}

	}

	public int find(long location) {
		for(int i=0; i<mapView.getOverlays().size()-1; i++) {
			Overlay ol1 = mapView.getOverlays().get(i);
			Overlay ol2 = mapView.getOverlays().get(i+1);
			MapMarkersOverlay mol1, mol2;
			MapLinesOverlay lol1, lol2;
			long time1 = 0, time2 = 0;
			if(ol1 instanceof MapMarkersOverlay) {
				mol1 = (MapMarkersOverlay) ol1;
				time1 = mol1.getTime();
			} else if(ol1 instanceof MapLinesOverlay) {
				lol1 = (MapLinesOverlay) ol1;
				time1 = lol1.getTime();
			}
			if(ol2 instanceof MapMarkersOverlay) {
				mol2 = (MapMarkersOverlay) ol2;
				time2 =  mol2.getTime();
			} else if(ol2 instanceof MapLinesOverlay) {
				lol2 = (MapLinesOverlay) ol2;
				time2 = lol2.getTime();
			}

			if(time1 <= location && time2 >= location) {
				return i;
			}
		}
		return 0;
	}


	public synchronized void hideOverlays(int index, int index2) {
		initMapView();
		for(int i=index; i<mapView.getOverlays().size(); i++) {
			Overlay overlay = mapView.getOverlays().get(i);
			if(overlay instanceof MapLinesOverlay) {
				MapLinesOverlay mo = (MapLinesOverlay) overlay;
				mo.setVisible(false);
			} else if(overlay instanceof MapMarkersOverlay) {
				MapMarkersOverlay mo = (MapMarkersOverlay) overlay;
				mo.setVisible(false);
			}
		}
		
		for(int j=index2; j>=0; j--) {
			MapObject object = objects.get(j);
			if(object.getType().equals("undo")) undoOverlay(j);
			else if(object.getType().equals("dragmarker")) {
				hideMarker(object);
			}
		}
	}

	public void initMapView() {
		for(Overlay overlay: mapView.getOverlays()) {
			if(overlay instanceof MapLinesOverlay) {
				MapLinesOverlay mo = (MapLinesOverlay) overlay;
				mo.setVisible(true);
			} else {
				MapMarkersOverlay mo = (MapMarkersOverlay) overlay;
				mo.setVisible(true);
			}
		}
	}

	public int seek(long location) {
		for(int i=0; i<objects.size()-1; i++) {
			MapObject mo1 = objects.get(i);
			MapObject mo2 = objects.get(i+1);
			if(mo1.getCurrentseconds() <= location && mo2.getCurrentseconds() >= location) return i;
		}
		return 0;
	}

	public GeoPoint getView(int index) {
		for(int i=index-1; i>=0; i--) {
			MapObject mo = objects.get(i);
			if(mo.getType().equals("move") || mo.getType().equals("view")) {
				return mo.getPoints().get(0);
			}
		}
		return null;
	}

	public synchronized void hideMarker(MapObject object) {
		for(Overlay overlay:mapView.getOverlays()) {
			if(overlay instanceof MapMarkersOverlay) {
				GeoPoint target = ((MapMarkersOverlay) overlay).getPoint();
				if(target.getLatitudeE6() == object.getPoints().get(1).getLatitudeE6() &&
						target.getLongitudeE6() == object.getPoints().get(1).getLongitudeE6()) {
					((MapMarkersOverlay) overlay).setVisible(false);
					break;
				}
			}
		}	
	}
	
	public synchronized void showOverlay(MapObject object) {
		for(Overlay overlay: mapView.getOverlays()) {
			if(overlay instanceof MapLinesOverlay) {
				MapLinesOverlay mo = (MapLinesOverlay) overlay;
				if(mo.getTime() == object.getCurrentseconds()) {
					mo.setVisible(true);
					break;
				}
			} else {
				MapMarkersOverlay mo = (MapMarkersOverlay) overlay;
				if(mo.getTime() == object.getCurrentseconds()) {
					mo.setVisible(true);
					break;
				}
			}
		}
		
	}
	
	public void initOverlays(boolean add) {
		if(add) {
			for(MapObject mo: objects) {
				String type = mo.getType();
				if(type.equals("line")) {
					mapView.getOverlays().add(new MapLinesOverlay(pin, mo.getPoints(), mo.getCurrentseconds()));
				} else if(type.equals("marker") || type.equals("dragmarker")) {
					mapView.getOverlays().add(new MapMarkersOverlay(pin, mo.getPoints().get(0), mapView.getContext(), mo.getCurrentseconds()));
				}
			}
		}
		
		for(Overlay overlay: mapView.getOverlays()) {
			if(overlay instanceof MapLinesOverlay) {
				MapLinesOverlay mo = (MapLinesOverlay) overlay;
				mo.setVisible(false);
			} else {
				MapMarkersOverlay mo = (MapMarkersOverlay) overlay;
				mo.setVisible(false);
			}
		}
	}
	
	public void undoOverlay(int index) {
		for(int i=index-1; i>=0; i--) {
			MapObject object = objects.get(i);
			String type = object.getType();
			if(type.equals("line") || type.equals("marker") || type.equals("dragmarker")) {
				if(setInvisible(object.getCurrentseconds())) break;
			}
		}
	}
	
	public boolean setInvisible(long target) {
		for(Overlay ol: mapView.getOverlays()) {
			if(ol instanceof MapLinesOverlay) {
				MapLinesOverlay mol = (MapLinesOverlay) ol;
				if(mol.getTime() == target && mol.isVisible()) {
					mol.setVisible(false);
					return true;
				}
			} else {
				MapMarkersOverlay mol = (MapMarkersOverlay) ol;
				if(mol.getTime() == target && mol.isVisible()) {
					mol.setVisible(false);
					return true;
				}
			}
		}
		return false;
	}

}