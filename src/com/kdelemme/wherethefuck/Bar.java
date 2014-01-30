package com.kdelemme.wherethefuck;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Bar implements Place {

	private double mLatitude;
	private double mLongitude;
	private String mTitle;
	
	public Bar(double lat, double lng, String title) {
		mLatitude = lat;
		mLongitude = lng;
		mTitle = title;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setmLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setmTitle(String title) {
		this.mTitle = title;
	}
	
	public float getMarkerColor() {
		return BitmapDescriptorFactory.HUE_RED;
	}

}
