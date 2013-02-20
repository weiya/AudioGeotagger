package com.vuw.audiogeotagger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.google.android.maps.GeoPoint;
import android.util.Xml;

/**
 * This class is used to parse the xml file and produce a list of MapObject while parsing
 * @author Weiya Xu
 *
 */
public class Parser {
	private InputStream inputStream;
	private int latitude;
	private int longitude;
	private int level;
	private boolean move = false;
	private boolean line = false;
	private List<GeoPoint> points;
	private long idle;
	private long currentSeconds = 0;
	private boolean view = false;
	private boolean marker = false;
	private boolean dragmarker = false;
	private boolean undo = false;
	private int targetLatitude;
	private int targetLongitude;
	private String timestamp;
	private List<MapObject> objects = new ArrayList<MapObject>();

	public Parser(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void parse() throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();  
		parser.setInput(inputStream, "utf-8");  
		int eventType = parser.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT) {  
			switch (eventType) {  
			case XmlPullParser.START_DOCUMENT:  
				break;  
			case XmlPullParser.START_TAG:  
				String name = parser.getName();

				if(name.equals("type")) {
					String text = parser.nextText();
					if(text.equals("move")) {
						move = true;
						points = new ArrayList<GeoPoint>();
					} else if(text.equals("initial_view")) {
						view = true;
						points = new ArrayList<GeoPoint>();
					} else if(text.equals("marker")) {
						marker = true;
						points = new ArrayList<GeoPoint>();
					} else if(text.equals("line")) {
						line = true;
						points = new ArrayList<GeoPoint>();
					} else if(text.equals("dragmarker")) {
						dragmarker = true;
						points = new ArrayList<GeoPoint>();
					} else if(text.equals("undo")) undo = true;
					break;
				} else if(name.equals("latitude")) {
					latitude = Integer.valueOf(parser.nextText());
					break;
				} else if(name.equals("longitude")) {
					longitude = Integer.valueOf(parser.nextText());
					if(line) {
						points.add(new GeoPoint(latitude, longitude));
					}
					break;
				} else if(name.equals("level")) {
					level = Integer.valueOf(parser.nextText());
					break;
				} else if(name.equals("seconds")) {
					idle = Long.valueOf(parser.nextText());
					currentSeconds += idle; 
					break;
				} else if(name.equals("targetLatitude")) {
					targetLatitude = Integer.valueOf(parser.nextText());
					break;
				} else if(name.equals("targetLongitude")) {
					targetLongitude = Integer.valueOf(parser.nextText());
					break;
				} else if(name.equals("timestamp")) {
					timestamp = parser.nextText();
					break;
				}
				break;
			case XmlPullParser.END_TAG:
				String tag = parser.getName();
				if(!tag.equals("event")) break;
				if(line) {
						MapObject mo = new MapObject("line", points, -1, idle, currentSeconds, timestamp);
						objects.add(mo);
						line = false;
				} else if(marker) {
					List<GeoPoint> p = new ArrayList<GeoPoint>();
					p.add(new GeoPoint(latitude, longitude));
					MapObject mo = new MapObject("marker", p, -1, idle, currentSeconds, timestamp);
					objects.add(mo);
						marker = false;
				} else if(move) {
					List<GeoPoint> p = new ArrayList<GeoPoint>();
					p.add(new GeoPoint(latitude, longitude));
					MapObject mo = new MapObject("move", p, level, idle, currentSeconds, timestamp);
					objects.add(mo);
					move = false;
				} else if(view) {
					List<GeoPoint> p = new ArrayList<GeoPoint>();
					p.add(new GeoPoint(latitude, longitude));
					MapObject mo = new MapObject("view", p, level, idle, currentSeconds, timestamp);
					objects.add(mo);
					view = false;
				} else if(dragmarker) {
					List<GeoPoint> p = new ArrayList<GeoPoint>();
					p.add(new GeoPoint(latitude, longitude));
					p.add(new GeoPoint(targetLatitude, targetLongitude));
					MapObject mo = new MapObject("dragmarker", p, -1, idle, currentSeconds, timestamp);
					objects.add(mo);
					dragmarker = false;
				} else if(undo) {
					objects.add(new MapObject("undo", new ArrayList<GeoPoint>(), -1, idle, currentSeconds, timestamp));
					undo = false;
				}
				break;  
			default:  
				break;
			}  
			eventType = parser.next();  
		}
	}
	
	public List<MapObject> getObjects() {
		return this.objects;
	}
}