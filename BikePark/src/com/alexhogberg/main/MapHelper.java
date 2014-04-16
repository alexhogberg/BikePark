package com.alexhogberg.main;

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

	public MapHelper() {
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
	public Polyline DrawLine(GoogleMap map, Marker currentMarker, LatLng end) {
		Polyline mapLine;
		PolylineOptions line =
				new PolylineOptions().add(
					currentMarker.getPosition(),
		            end
		            ).width(5).color(Color.RED);
		mapLine = map.addPolyline(line);
		return mapLine;
	}
	
	public void zoomTo(GoogleMap map, Marker position) {
		CameraUpdate center = CameraUpdateFactory.newLatLng(position.getPosition());
		CameraUpdate zoom = CameraUpdateFactory.zoomTo(ZOOM_LEVEL);
		
		map.moveCamera(center);
		map.animateCamera(zoom);
	}
	
	public MarkerOptions createMarker(LatLng position, String title, String color) {
		MarkerOptions newMarkerOptions = new MarkerOptions();
		newMarkerOptions.position(position);
		
		
		newMarkerOptions.title(title);
		if(color.equals("green"))
			newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		if(color.equals("red"))
			newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		
		return newMarkerOptions;
	}

}
