package com.alexhogberg.gps;
import com.alexhogberg.main.MapHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;


/**
 * A class that listens to GPS location changes and acts accordingly
 * 
 * @author Alexander
 * 
 */
public class GPSListener implements LocationListener {
	
	private static final int ARRIVED_RANGE = 4;
	
	private Marker target;
	private Marker position;
	private GoogleMap map;
	private Polyline mapLine;
	private Context context;
	private MapHelper mH;
	/**
	 * Updates the users position
	 */
	public GPSListener(GoogleMap googleMap, Marker currentPosition, Marker currentMarker, Context c) {
		target = currentMarker;
		position = currentPosition;
		context = c;
		map = googleMap;
		mH = new MapHelper(map);
	}
	
	public void setCurrentTarget(Marker target) {
		this.target = target;
	}
	
	public void setCurrentPosition(Marker position) {
		this.position = position;
	}
	
	public Marker getCurrentPosition() {
		return position;
	}
	
	@Override
	public void onLocationChanged(Location loc) {
		if (mapLine != null)
			mapLine.remove();
		
		if (target != null && position != null) {
			
			if (position.getPosition().latitude != loc.getLatitude()
					&& position.getPosition().longitude != loc
							.getLongitude()) {
				LatLng currPos = new LatLng(loc.getLatitude(), loc.getLongitude());
				position.setPosition(currPos);
				double distance = mH.getDistance(position.getPosition(), target.getPosition());
				
				if(distance > ARRIVED_RANGE)
					position.setTitle("You are here! (" + distance + " m away)");
				else
					position.setTitle("You have arrived!");
				
				
				mapLine = mH.DrawLine(target, position);
				position.showInfoWindow();
				if (mH.getDistance(target.getPosition(),
						position.getPosition()) < ARRIVED_RANGE) {
					position.setTitle("You have arrived!");
				}
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(context.getApplicationContext(), "Gps Disabled",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(context.getApplicationContext(), "Gps Enabled",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
