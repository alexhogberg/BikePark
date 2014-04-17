package com.alexhogberg.main;

import java.util.Date;
import java.util.HashMap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.alexhogberg.android.R;
import com.alexhogberg.gps.GPSListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocationManager mlocManager;
	private GPSListener mlocListener;
	private GoogleMap mMap;
	private Button parkButton;
	private Button findButton;
	private Button resetButton;
	private Marker currentTargetMarker;
	private Marker currentPositionMarker;
	private Polyline mapLine;
	private MapHelper mH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		//Load the view
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Load the current GoogleMap from the fragment
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		
		//Start a new MapHelper class attached with the current map to deal with the interaction
		mH = new MapHelper(mMap);
		mH.generateMapOptions();
		
		//Connect to the GPS service
		mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocListener = new GPSListener(mMap, currentPositionMarker, currentTargetMarker, this);
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				mlocListener);
		
		// If the user has closed the app with a present parking, load these
		// settings
		loadSettings();
		
		
		// If GPS is disabled, send warning
		if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		// Puts a marker on the map where the user is currently located (via
		// GPS)
		parkButton = (Button) findViewById(R.id.park);
		parkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setMarkerFromPosition();
			}
		});

		// Puts a marker on the users current position and maps it between the
		// parked position
		findButton = (Button) findViewById(R.id.find);
		findButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				putPositionMarker();
			}
		});

		// Removes all markers on the map
		resetButton = (Button) findViewById(R.id.reset);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildDeleteMessageMarker();
			}
		});
	}

	private void loadSettings() {
		HashMap<String, Object> prefs = getSavedPrefs();
		// Load the previous settings if they exist and add a marker
		if ((double) prefs.get("lat") != 0 && (double) prefs.get("lon") != 0) {
			setMarkerWithValues((double) prefs.get("lat"), (double) prefs.get("lon"),
					new Date((long) prefs.get("date")).toString());
		}
		
	}

	/**
	 * Creates a marker where the user is located and maps it towards the parked
	 * position
	 * 
	 * @param zoomTo
	 *            whether to zoom to the current location or not.
	 */
	private void putPositionMarker() {
		if (currentPositionMarker != null)
			currentPositionMarker.remove();
		if (mapLine != null)
			mapLine.remove();

		if (currentTargetMarker != null) {
			Location loc = mlocManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (loc != null) {
				
				// Add a marker for the users current position
				LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
				
				//Notify the listener of the new marker
				MarkerOptions markerOptions = mH.createPositionMarker(pos, currentTargetMarker);
				Marker newMarker = mMap.addMarker(markerOptions);
				
				mlocListener.setCurrentPosition(newMarker);
				
				mH.zoomTo(mlocListener.getCurrentPosition());
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"You have no previous parkings!",
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Set a marker at the given GPS coordinates
	 */
	private void setMarkerFromPosition() {
		Location loc = mlocManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc != null) {
			
			mMap.clear();
			if (currentPositionMarker != null)
				currentPositionMarker = null;
			LatLng currPos = new LatLng(loc.getLatitude(), loc.getLongitude());
			currentTargetMarker = mMap.addMarker(mH.createTargetMarker(currPos, null));
			
			//Send the current marker to the listener
			mlocListener.setCurrentTarget(currentTargetMarker);
			currentTargetMarker.showInfoWindow();
			mH.zoomTo(currentTargetMarker);
			setSavedPrefs(loc.getLatitude(), loc.getLongitude());
		} else {
			Toast.makeText(getApplicationContext(),
					"Please make sure that the GPS is enabled.",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Sets a marker at a given latitude and longitude
	 * 
	 * @param lat
	 *            current latitude
	 * @param lon
	 *            current longitude
	 * @param d
	 *            the current date
	 */
	private void setMarkerWithValues(double lat, double lon, String date) {
		mMap.clear();
		if (currentPositionMarker != null)
			currentPositionMarker = null;
		LatLng currPos = new LatLng(lat, lon);
		String title = "Last parking: " + date;
		currentTargetMarker = mMap.addMarker(mH.createTargetMarker(currPos, title));
		mlocListener.setCurrentTarget(currentTargetMarker);
		currentTargetMarker.showInfoWindow();
		mH.zoomTo(currentTargetMarker);
	}
	/**
	 * Clear the map and remove any saved preferences
	 */
	protected void clearMap() {
		mMap.clear();
		currentTargetMarker = null;
		mlocListener.setCurrentTarget(null);
		mlocListener.setCurrentPosition(null);
		removeSavedPrefs();
	}

	/**
	 * Get the saved preferences from previous locations
	 * 
	 * @return a hashmap containing where and when the last parking occured
	 */
	private HashMap<String, Object> getSavedPrefs() {
		SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
		HashMap<String, Object> returnMap = new HashMap<String, Object>();

		returnMap.put("lat",
				Double.longBitsToDouble(settings.getLong("latitude", 0)));
		returnMap.put("lon",
				Double.longBitsToDouble(settings.getLong("longitude", 0)));
		returnMap.put("date", settings.getLong("date", 0));
		return returnMap;
	}

	/**
	 * Saves the position in the local phone settings
	 * 
	 * @param lat
	 *            latitude to save
	 * @param lon
	 *            longitude to save
	 * @param date
	 *            the date to save
	 */
	private void setSavedPrefs(double lat, double lon) {
		Date d = new Date();
		SharedPreferences settings = getSharedPreferences("POSITIONS", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("latitude", Double.doubleToLongBits(lat));
		editor.putLong("longitude", Double.doubleToLongBits(lon));
		editor.putLong("date", d.getTime());

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
		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
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
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								clearMap();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {

					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}


}
