package com.vuw.audiogeotagger;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;

/**
 * This class represents an action users take on the map such as move, draw lines and add markers.
 * In addtion, this class includes a range of information including the type, location and date of 
 * action
 * @author Weiya Xu
 *
 */
public class MapObject {
	private String type;
	private List<GeoPoint> points = new ArrayList<GeoPoint>();
	private String timestamp;
	private int level;
	private long idle;
	private long currentseconds;
		
	public MapObject(String type, List<GeoPoint> points, int level, long idle, long currentseconds, String timestamp) {
		this.type = type;
		this.points = points;
		this.timestamp = timestamp;
		this.level = level;
		this.idle = idle;
		this.currentseconds = currentseconds;
	}

	public String getType() { return this.type; }
	public List<GeoPoint> getPoints() { return this.points; }
	public int getLevel() { return this.level; }
	public long getIdle() { return this.idle; }
	public long getCurrentseconds() { return this.currentseconds; }
	public String getTimeStamp() { return this.timestamp; }
	

}
