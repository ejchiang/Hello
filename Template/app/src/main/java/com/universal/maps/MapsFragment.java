package com.universal.maps;

import java.util.ArrayList;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.universal.Helper;
import com.universal.MainActivity;
import com.universal.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapsFragment extends Fragment {

	private LinearLayout ll;
	private TextView text;
	
	private MapView mMapView;
	private GoogleMap googleMap;
	
	private LocationManager locationManager;
	private Location loc;
	
	private ProgressDialog locationDialog;

	Activity mAct;
	Double lat;
	Double lon;
	String query;
	String[] maps;
	String[] places;
	
	int mode;
	int PLACES = 2;
	int ARRAY = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

			ll = (LinearLayout) inflater.inflate(R.layout.fragment_maps, container,
				false);
			
			setHasOptionsMenu(true);
			
			mMapView = (MapView) ll.findViewById(R.id.map);
			mMapView.onCreate(savedInstanceState);
			mMapView.onResume();

			if ((getResources().getString(R.string.ad_visibility).equals("0"))) {
				// Look up the AdView as a resource and load a request.
				AdView adView = (AdView) ll.findViewById(R.id.adView);
				AdRequest adRequest = new AdRequest.Builder().build();
				adView.loadAd(adRequest);
			}
		
		return ll;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAct = getActivity();
		Helper.isOnline(mAct, true, true);
		
		String data = this.getArguments().getString(MainActivity.DATA);
		if(data.startsWith("@")){
			maps = getResourceString(data.substring(1), mAct);
			mode = ARRAY;
		} else {
			mode = PLACES;
			query = data;
		}

		MapsInitializer.initialize(mAct);

		googleMap = mMapView.getMap();
		
		if (mode == ARRAY){

			lat = Double.parseDouble(maps[3]);
			lon = Double.parseDouble(maps[4]);

			LatLng loc = new LatLng(lat, lon);

			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,
				Integer.parseInt(maps[5])));

			Marker marker = googleMap.addMarker(new MarkerOptions().title(maps[1])
				.snippet(maps[2]).position(loc));
			marker.showInfoWindow();
		
			String stringres = this.getArguments().getString(MainActivity.DATA);
			maps = getResourceString(stringres, mAct);
			
			text = (TextView) ll.findViewById(R.id.textViewInfo);
			text.setText(Html.fromHtml(maps[0]));
		} else {
			text = (TextView) ll.findViewById(R.id.textViewInfo);
			text.setVisibility(View.GONE);
			currentLocation();
			
			googleMap.clear();
		}
	}

	public static String[] getResourceString(String name, Context context) {
		int nameResourceID = context.getResources().getIdentifier(name,
				"array", context.getApplicationInfo().packageName);
		if (nameResourceID == 0) {
			throw new IllegalArgumentException(
					"No resource string found with name " + name);
		} else {
			return context.getResources().getStringArray(nameResourceID);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	if (mode == ARRAY){
    		inflater.inflate(R.menu.maps_menu, menu);
    	}
 	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {    
        case R.id.navigate:
        	Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
        		    Uri.parse("http://maps.google.com/maps?daddr="+lat+"," + lon));
        		startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

		private ProgressDialog dialog;
		private Context context;
		private String places;

		public GetPlaces(Context context, String places) {
			this.context = context;
			this.places = places;
		}

		@Override
		protected void onPostExecute(ArrayList<Place> result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (null == result || result.size() < 1) {
				Helper.noConnection(mAct, true);
			} else {
				for (int i = 0; i < result.size(); i++) {
					googleMap.addMarker(new MarkerOptions()
							.title(result.get(i).getName())
							.position(
									new LatLng(result.get(i).getLatitude(),
											result.get(i).getLongitude()))
							// .icon(BitmapDescriptorFactory
							// .fromResource(R.drawable.pin))
							.snippet(result.get(i).getVicinity()));
				}
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(result.get(0).getLatitude(), result
								.get(0).getLongitude())) // Sets the center of
															// the map to
						// Mountain View
						.zoom(14) // Sets the zoom
						.tilt(30) // Sets the tilt of the camera to 30 degrees
						.build(); // Creates a CameraPosition from the builder
				googleMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setCancelable(false);
			dialog.setMessage(getResources().getString(R.string.loading));
			dialog.isIndeterminate();
			dialog.show();
		}

		@Override
		protected ArrayList<Place> doInBackground(Void... arg0) {
			PlacesService service = new PlacesService(
					getResources().getString(R.string.google_server_key));
			
			ArrayList<Place> findPlaces = service.findPlaces(loc.getLatitude(), // 28.632808
				     loc.getLongitude(), places); // 77.218276

			for (int i = 0; i < findPlaces.size(); i++) {

				Place placeDetail = findPlaces.get(i);
				Log.e("INFO", "places : " + placeDetail.getName());
			}
			return findPlaces;
		}

	}


	private void currentLocation() {
		locationManager = (LocationManager) mAct.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		String provider = locationManager
				.getBestProvider(criteria, false);

		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null) {
			locationManager.requestLocationUpdates(provider, 0, 0, listener);
			
			locationDialog = new ProgressDialog(mAct);
			locationDialog.setCancelable(false);
			locationDialog.setTitle(getResources().getString(R.string.maps_location_title));
			locationDialog.setMessage(getResources().getString(R.string.maps_location_subtitle));
			locationDialog.isIndeterminate();
			locationDialog.show();
		} else {
			loc = location;
			new GetPlaces(mAct, query).execute();
			Log.e("INFO", "location : " + location);
		}

	}

	private LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e("INFO", "location update : " + location);
			loc = location;
			new GetPlaces(getActivity(), query).execute();
			locationManager.removeUpdates(listener);
			
			if (locationDialog.isShowing()) {
				locationDialog.dismiss();
			}
		}
	};
}