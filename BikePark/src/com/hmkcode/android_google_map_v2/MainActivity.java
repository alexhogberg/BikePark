package com.hmkcode.android_google_map_v2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.alexhogberg.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocationManager mlocManager;
	private LocationListener mlocListener;
	private GoogleMap mMap;
	private Button parkButton;
	private Button findButton;
	private Button resetButton;
	private Marker currentMarker;
	private Marker currentPosition;
	private Polyline mapLine;
	private SimpleDateFormat format = new SimpleDateFormat("EEEE, LLLL M y H:m:s", Locale.ENGLISH);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//If the user has closed the app with a present parking, load these settings
		HashMap<String, Object> prefs = getSavedPrefs();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setIndoorEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener();
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

		//If GPS is disabled, send warning
		if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	        buildAlertMessageNoGps();
	    }

		//Load the previous settings if they exist and add a marker
		if((double) prefs.get("lat") != 0 && (double) prefs.get("lon") != 0) {
			setMarker((double) prefs.get("lat"), (double) prefs.get("lon"), new Date((long) prefs.get("date")).toString());
		}
		
		//Puts a marker on the map where the user is currently located (via GPS)
		parkButton = (Button) findViewById(R.id.park);
		parkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Location loc = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(loc != null) {				
					Date d = new Date();
					String date = format.format(d);
					
					setMarker(loc.getLatitude(), loc.getLongitude(), date);
					setSavedPrefs(loc.getLatitude(), loc.getLongitude(), d.getTime());
				}
			}
		});
		
		//Puts a marker on the users current position and maps it between the parked position
		findButton = (Button) findViewById(R.id.find);
		findButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(currentMarker != null)
					putPositionMarker(true);
				else {
					Toast.makeText(getApplicationContext(),
							"You have no previous parkings!",
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		//Removes all markers on the map
		resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildDeleteMessageMarker();
			}
		});
	}
	
	/**
	 * Creates a marker where the user is located and maps it towards the parked position
	 * @param zoomTo whether to zoom to the current location or not.
	 */
	private void putPositionMarker(boolean zoomTo) {
		if(currentPosition != null)
			currentPosition.remove();
		if(mapLine != null)
			mapLine.remove();
		
			if(currentMarker != null) {
				Location loc = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(loc != null) {
					mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
					//Add a marker for the users current position
					currentPosition = mMap.addMarker(new MarkerOptions()
					        .position(new LatLng(loc.getLatitude(), loc.getLongitude())));
					currentPosition.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					if(getDistance(currentPosition.getPosition(), currentMarker.getPosition()) > 4) {
					currentPosition.setTitle(("You are here! ("+ getDistance(currentMarker.getPosition(), currentPosition.getPosition()) +" m away)"));
					} else {
						currentPosition.setTitle("You have arrived!");
					}
					//Draw a line between the two locations
					DrawLine(mMap, currentPosition.getPosition());
					if(zoomTo != false) {
						CameraUpdate center= CameraUpdateFactory.newLatLng(currentPosition.getPosition());
						CameraUpdate zoom=CameraUpdateFactory.zoomTo(18);
						mMap.moveCamera(center);
					    mMap.animateCamera(zoom);
					    
					}
					currentPosition.showInfoWindow();
				}
				
			
		}
	}
	
	/**
	 * Mathematical function to calculate the distance between two points in a coordinate
	 * @param start start position
	 * @param end end position
	 * @return number of meters between the two points
	 */
	private double getDistance(LatLng start, LatLng end) {
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
	private void DrawLine(GoogleMap map, LatLng end) {
		PolylineOptions line =
				new PolylineOptions().add(
					currentMarker.getPosition(),
		            end
		            ).width(5).color(Color.RED);
		mapLine = map.addPolyline(line);
	}
	
	/**
	 * Sets a marker at a given latitude and longitude
	 * @param lat current latitude
	 * @param lon current longitude
	 * @param d the current date
	 */
	private void setMarker(double lat, double lon, String d) {
		
		
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.clear();
		if(currentPosition != null)
			currentPosition = null;
		LatLng currPos = new LatLng(lat, lon);
		currentMarker = mMap.addMarker(new MarkerOptions()
		        .position(currPos)
		        .title("Last parking: " + d.toString()));
		CameraUpdate center= CameraUpdateFactory.newLatLng(currPos);
		CameraUpdate zoom=CameraUpdateFactory.zoomTo(18);
		currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		currentMarker.showInfoWindow();
		mMap.moveCamera(center);
	    mMap.animateCamera(zoom);
	}
	
	/**
	 * Get the saved preferences from previous locations
	 * @return a hashmap containing where and when the last parking occured
	 */
	private HashMap<String, Object> getSavedPrefs() {
		SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
		HashMap<String, Object> returnMap = new HashMap<String, Object>();
		
		returnMap.put("lat", Double.longBitsToDouble(settings.getLong("latitude", 0)));
		returnMap.put("lon", Double.longBitsToDouble(settings.getLong("longitude", 0)));
		returnMap.put("date", settings.getLong("date", 0));
		return returnMap;
	}
	
	/**
	 * Saves the position in the local phone settings
	 * @param lat latitude to save
	 * @param lon longitude to save
	 * @param date the date to save
	 */
	private void setSavedPrefs(double lat, double lon, long date) {
		SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("latitude", Double.doubleToLongBits(lat));
		editor.putLong("longitude", Double.doubleToLongBits(lon));
		editor.putLong("date", date);

		// Commit the edits!
		editor.commit();
	}
	
	/**
	 * Removes all saved position (used with the reset-button)
	 */
	private void removeSavedPrefs() {
		SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		
		editor.commit();
	}
	
	/**
	 * An alert that is being displayed if you dont have GPS enabled
	 */
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	private void buildDeleteMessageMarker() {		
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Are you sure you want to reset?")
	           .setCancelable(true)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	            	   mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	            	   mMap.clear();
	            	   currentMarker = null;
	            	   removeSavedPrefs();
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}

	/**
	 * A class that listens to GPS location changes and acts accordingly
	 * @author Alexander
	 *
	 */
	public class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			if(currentPosition != null && currentMarker != null) {
				if(currentPosition.getPosition().latitude != loc.getLatitude() 
					&& 
					currentPosition.getPosition().longitude != loc.getLongitude()) {
					putPositionMarker(false);
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(),
			"Gps Disabled",
			Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(),
			"Gps Enabled",
			Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)	{
		}
	}
}
