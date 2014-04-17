package com.alexhogberg.main;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapHelper {
	
	private static final int ZOOM_LEVEL = 18;
	private GoogleMap currentMap;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEEE, LLLL M y H:m:s", Locale.ENGLISH);

	public MapHelper(GoogleMap map) {
		currentMap = map;
	}
	
	public void generateMapOptions() {
		currentMap.setIndoorEnabled(true);
		currentMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	}

	/**
	 * Mathematical function to calculate the distance between two points in a coordinate
	 * @param start start position
	 * @param end end position
	 * @return number of meters between the two points
	 */
	public double getDistance(LatLng start, LatLng end) {
		double pk = (double) (180/3.14169);

	    double a1 = (start.latitude / pk);
	    double a2 = (start.longitude / pk);
	    double b1 = (end.latitude / pk);
	    double b2 = (end.longitude / pk);

	    double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
	    double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
	    double t3 = Math.sin(a1)*Math.sin(b1);
	    double tt = Math.acos(t1 + t2 + t3);
	    
	    Double returnVal = Double.valueOf(6366000*tt);
	   

	    return returnVal.intValue();
	}
	
	/**
	 * Draws a line between the currentMarker and another point
	 * @param map the map to draw on
	 * @param lat latitude
	 * @param lat longitude
	 */
	public Polyline DrawLine(Marker start, Marker stop) {
		Polyline mapLine;
		PolylineOptions line =
				new PolylineOptions().add(
					start.getPosition(),
		            stop.getPosition()
		            ).width(5).color(Color.RED);
		mapLine = currentMap.addPolyline(line);
		return mapLine;
	}
	
	/**
	 * Zooms the map to a given position
	 * @param position
	 */
	public void zoomTo(Marker position) {
		CameraUpdate center = CameraUpdateFactory.newLatLng(position.getPosition());
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(ZOOM_LEVEL);
		
		currentMap.moveCamera(center);
		currentMap.animateCamera(zoom);
	}
	
	/**
	 * Create a new target marker
	 * @param position current position
	 * @param title the given title
	 * @return a MarkerOptions object
	 */
	public MarkerOptions createTargetMarker(LatLng position, String title) {
		MarkerOptions mO = new MarkerOptions();
		mO.position(position);
		if(title == null) {
			Date d = new Date();
			String formattedDate = dateFormat.format(d);
			mO.title("Last parking: " + formattedDate);
		} else {
			mO.title(title);
		}
		
		mO.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		
		return mO;
	}
	
	public MarkerOptions createPositionMarker(LatLng position, Marker target) {
		MarkerOptions mO = new MarkerOptions();
		mO.position(position);
		
		String prefix = "You are here! (";
		double distance = getDistance(target.getPosition(),position);
		String suffix = " m away)";
		String title = prefix + "" + distance + "" + suffix;
		
		mO.title(title);
		mO.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		
		return mO;
	}

}
