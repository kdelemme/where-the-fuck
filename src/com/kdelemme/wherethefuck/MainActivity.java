package com.kdelemme.wherethefuck;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kdelemme.wherethefuck.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class MainActivity extends Activity {

	private Button btnPullRestaurant;
	private Button btnPullBar;
	private GoogleMap mMap;
	private List<Restaurant> mRestaurantList;
	private List<Bar> mBarList;
	private Marker mMarker;
	private int mCurrentRestaurantIndex;
	private int mCurrentBarIndex;
	private boolean mDataAlreadyPulled;

	
	private void init() {
		btnPullRestaurant = (Button) findViewById(R.id.btnPullRestaurant);
		btnPullRestaurant.setClickable(false);
		
		btnPullBar = (Button) findViewById(R.id.btnPullBar);
		btnPullBar.setClickable(false);
		
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setMyLocationEnabled(true);
		
		mRestaurantList = new ArrayList<Restaurant>(32);
		mBarList = new ArrayList<Bar>(32);
		mDataAlreadyPulled = false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		init();
		getLocation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	private void getLocation() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	moveMapAtLocation(location);
		 
		    	if (!mDataAlreadyPulled) {
			    	pullRestaurants(location);
			    	pullBars(location);
		    	}    
		    }

			public void onProviderDisabled(String arg0) {}
			public void onProviderEnabled(String arg0) {}
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		};

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locationListener);
	}
	
	//TODO Create pullPlaces and use only one HTTP request but filter results.
	private void pullRestaurants(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
			
		StringBuilder url = new StringBuilder();
		url.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
		url.append(lat + "," + lng);
		url.append("&radius=1500&types=restaurant&rankby=prominence&opennow=true&sensor=true&key=");
		url.append(Constant.GOOGLE_API_KEY);

		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url.toString(), new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	try {
					JSONObject jsonResponse = new JSONObject(response);
					JSONArray results = jsonResponse.getJSONArray("results");

					final int resultsLength = results.length();
					for (int i=0; i<resultsLength; i++) {
						JSONObject result = (JSONObject) results.get(i);
						JSONObject geo = result.getJSONObject("geometry");
						JSONObject loc = geo.getJSONObject("location");
						
						double lat = loc.getDouble("lat");
						double lng = loc.getDouble("lng");
						String title = result.getString("name");
						
						mRestaurantList.add(new Restaurant(lat, lng, title));
					}
					
					mCurrentRestaurantIndex = -1;
					
					if (mRestaurantList != null && mRestaurantList.size() != 0) {
				    	//We have fetch restaurants, so we activate the buttons
				    	btnPullRestaurant.setClickable(true);
				    	btnPullRestaurant.setText(R.string.btnPullRestaurant);
				    	btnPullRestaurant.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								displayNextRestaurant();
							}
						});
				    	
				    	mDataAlreadyPulled = true;
			    	}
					
					
					displayNextRestaurant();
				} catch (JSONException e) {
					e.printStackTrace();
				}
		    }
		});
		
	}
	
	private void pullBars(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();
			
		StringBuilder url = new StringBuilder();
		url.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
		url.append(lat + "," + lng);
		url.append("&radius=1500&types=bar&rankby=prominence&opennow=true&sensor=true&key=");
		url.append(Constant.GOOGLE_API_KEY);

		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url.toString(), new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	try {
					JSONObject jsonResponse = new JSONObject(response);
					JSONArray results = jsonResponse.getJSONArray("results");

					final int resultsLength = results.length();
					for (int i=0; i<resultsLength; i++) {
						JSONObject result = (JSONObject) results.get(i);
						JSONObject geo = result.getJSONObject("geometry");
						JSONObject loc = geo.getJSONObject("location");
						
						double lat = loc.getDouble("lat");
						double lng = loc.getDouble("lng");
						String title = result.getString("name");
						
						mBarList.add(new Bar(lat, lng, title));
					}
					
					mCurrentBarIndex = -1;
					
					
					if (mBarList != null && mBarList.size() != 0) {
				    	btnPullBar.setClickable(true);
				    	btnPullBar.setText(R.string.btnPullBar);
				    	btnPullBar.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								displayNextBar();
							}
						});
				    	
				    	mDataAlreadyPulled = true;
			    	}
					
					//We want to display a restaurant at load time.
					//displayNextBar();
				} catch (JSONException e) {
					e.printStackTrace();
				}
		    }
		});
		
	}
	
	private void moveMapAtLocation(Location location) {
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
	}
	
	private void displayNextRestaurant() {
		if (mRestaurantList != null && mRestaurantList.size() != 0) {
			mCurrentRestaurantIndex = (mCurrentRestaurantIndex + 1) % mRestaurantList.size();
			Restaurant currentRestaurant = mRestaurantList.get(mCurrentRestaurantIndex);
			
			displayPlace(currentRestaurant);
		}
	}
	
	private void displayNextBar() {
		if (mBarList != null && mBarList.size() != 0) {
			mCurrentBarIndex = (mCurrentBarIndex + 1) % mBarList.size();
			Bar currentBar = mBarList.get(mCurrentBarIndex);
			
			displayPlace(currentBar);
		}
	}
	
	private void displayPlace(Place place) {
		LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
		
		if (mMarker != null) {
			mMarker.remove();
		}
		
		mMarker = mMap.addMarker(new MarkerOptions()
        	.position(latLng)
        	.title(place.getTitle())
        	.icon(BitmapDescriptorFactory.defaultMarker(place.getMarkerColor())));
		
		mMarker.showInfoWindow();
	}
	
	private void drawDirection(LatLng from, LatLng to) {
		//TODO Implement with Google Direction API
	}
}
