package com.example.simplecitysdkandroidexample;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.libtest.model.Point;
import com.libtest.model.WebAddress;
import com.libtest.util.CitySDKMapUtil;
import com.libtest.util.DirectionTask;
import com.libtest.util.MarkerTask;

public class MainActivity extends FragmentActivity implements LocationListener,
		OnMarkerClickListener {

	private GoogleMap map;
	private Handler handler;
	LocationManager locationManager;
	Location currentLocation;
	String provider;
	LatLng toPosition = new LatLng(41.108906, 29.009936);
	CitySDKMapUtil citySDKMapUtil;
	ArrayList<Point> itemListt = new ArrayList<Point>();

	public static double friendLatitude;
	public static double friendLongitude;

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
		if (requestCode == 1) {
			MarkerOptions markerOptions = new MarkerOptions().position(
					new LatLng(friendLatitude, friendLongitude)).title("");

			map.addMarker(markerOptions);
			System.out.println(friendLatitude + "," + friendLongitude);
		} else {
			Toast.makeText(this, "GPS Yok", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		citySDKMapUtil = new CitySDKMapUtil(this);

		map = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		map.setMyLocationEnabled(true);
		map.getUiSettings().isMyLocationButtonEnabled();
		map.setOnMarkerClickListener(this);

		if (locationManager != null) {
			boolean gpsIsEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (!gpsIsEnabled) {
				handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						citySDKMapUtil.showGPSDisabledAlertToUser();
					}
				}, 3000);
			}
			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, true);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 5000L, 10F, this);
			currentLocation = locationManager.getLastKnownLocation(provider);
		}
		if (currentLocation != null) {
			onLocationChanged(currentLocation);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.accommodation:
			setMarker("accommodation");
			return true;
		case R.id.parkingareas:
			setMarker("parkingareas");
			return true;
		case R.id.airports:
			setMarker("airports");
			return true;
		case R.id.Historical:
			setMarker("Historical");
			return true;
		case R.id.istanbulkart:
			setMarker("istanbulkart");
			return true;
		case R.id.pharmacy:
			setMarker("pharmacy");
			return true;
		case R.id.religion:
			setMarker("religion");
			return true;
		case R.id.station:
			setMarker("station");
			return true;
		case R.id.taxi:
			setMarker("taxi");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void setMarker(String layer) {
		map.clear();
		WebAddress address = new WebAddress("", layer);
		// String s =
		// "http://apicitysdk.ibb.gov.tr/nodes?layer=parkingareas&lat=40.993171&lon=29.026592&radius=300&geom&per_page=100";
		address.setLatitude(currentLocation.getLatitude());
		address.setLongitude(currentLocation.getLongitude());
		address.setPerPage(100);
		address.setRadius(2000);
		new MarkerTask(address) {

			@Override
			public void afterSync(ArrayList<Point> itemList_) {

				MainActivity.this.itemListt = itemList_;
				for (Point point : itemListt) {
					MarkerOptions markerOptions = new MarkerOptions().position(
							new LatLng(point.getLatitude(), point
									.getLongitude())).title(point.getName());
					if (point.getPhone() != null) {
						markerOptions.snippet(point.getPhone());
					}
					map.addMarker(markerOptions);

				}
			}
		}.execute();
	}

	@Override
	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng latLng = new LatLng(latitude, longitude);
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		map.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		if (marker.getSnippet() != null) {
			String phoneNumber = marker.getSnippet();
			if (marker.getSnippet().startsWith("212")
					|| marker.getSnippet().startsWith("216")
					|| marker.getSnippet().startsWith("(212")
					|| marker.getSnippet().startsWith("(216")) {
				phoneNumber = "0" + phoneNumber;
			}
			call(marker.getTitle(), phoneNumber);
		}
		toPosition = new LatLng(marker.getPosition().latitude,
				marker.getPosition().longitude);
		Point point = new Point(marker.getPosition().latitude,
				marker.getPosition().longitude, "");
		new DirectionTask(map, currentLocation, point) {

			@Override
			public void drawMarker() {
				for (Point point : itemListt) {
					MarkerOptions markerOptions = new MarkerOptions().position(
							new LatLng(point.getLatitude(), point
									.getLongitude())).title(point.getName());
					if (point.getPhone() != null) {
						markerOptions.snippet(point.getPhone());
					}
					map.addMarker(markerOptions);
				}
			}
		}.execute();
		return false;
	}

	@SuppressWarnings("deprecation")
	private void call(String name, final String phoneNo) {
		AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
		alert.setTitle("Call?");

		alert.setMessage("Are you sure want to call " + name + " ?");

		alert.setButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alert.setButton2("Yes", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String phoneNumber = "tel:" + phoneNo;
				Intent intent = new Intent(Intent.ACTION_CALL, Uri
						.parse(phoneNumber));
				startActivity(intent);
			}
		});
		alert.show();
	}

}
