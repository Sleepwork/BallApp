package com.example.zanta.ballapp;

/**
 * Created by Zanta on 12/10/2017.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //private boolean mLocationPermissionGranted;
    //private Location mLastKnownLocation;
    private LatLng mDefaultLocation;
    //private CameraPosition mCameraPosition = null;
    //private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final float DEFAULT_ZOOM = 7.0f;
    //private LocationManager locationManager;
    //private String locationProvider = LocationManager.GPS_PROVIDER;
    //private EditText editTxt;
    private String pPseudo;
    private double pLat, pLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pPseudo = "";
        pLat = 0;
        pLng = 0;

        Intent intent = this.getIntent();
        if(intent == null){
            Log.i("OnCrete", "Pas de données");
            this.finish();
        } else {
            pPseudo = intent.getStringExtra("pseudo");
            pLat = Double.parseDouble(intent.getStringExtra("lat"));
            pLng = Double.parseDouble(intent.getStringExtra("lng"));
            mDefaultLocation = new LatLng(pLat, pLng);
        }
        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //mLocationPermissionGranted = false;

        //editTxt = (EditText) findViewById(R.id.editTxt);
    }

    private void setPlayerLocation() {

        /*if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }*/
    /*
     * Before getting the device location, you must check location
     * permission, as described earlier in the tutorial. Then:
     * Get the best and most recent location of the device, which may be
     * null in rare cases when a location is not available.
     */
        /*if (mLocationPermissionGranted) {
            //mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        }*/

        // Set the map's camera position to the current location of the device.
        /*if (mCameraPosition != null) {
            Log.i("getDeviceLocation", "I don't know!");
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            Log.i("getDeviceLocation", "Current location is known. Moving the camera : " + mLastKnownLocation.getLatitude() + ":" + mLastKnownLocation.getLongitude());
            LatLng myLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, DEFAULT_ZOOM));
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.addMarker(new MarkerOptions().position(myLatLng).title("Home"));
        } else {*/
            Log.i("setPlayerLocation", "On a Good Mood");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            //mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title(pPseudo));
        //}
    }

   /*private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                //getDeviceLocation();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mLastKnownLocation = null;
                //getDeviceLocation();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("Autorisation acceptée", "Lancement de l'application");
                    if (locationManager != null && ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //displayPosition();
                        //locationManager.addProximityAlert(20, 10, 1000.0f, -1, proximityIntent);
                        //locationManager.requestLocationUpdates(locationProvider, 500, 100, mListener);
                        mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                        mLocationPermissionGranted = true;

                        Log.i("Location Manager", "Décollage");
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i("Autorisation refusée", "destruction imminente");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get the current location of the device and set the position of the map.
        // Turn on the My Location layer and the related control on the map.
        setPlayerLocation();
        //updateLocationUI();

    }

    /*public void setNewMarker(View v) {

        if (mLocationPermissionGranted) {
            //mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            String markerName = editTxt.getText().toString();

            if(!markerName.isEmpty() && markerName != "" && mLastKnownLocation != null){
                Log.i("setNewMarker", markerName + " " + mLastKnownLocation.getLatitude() +"/" + mLastKnownLocation.getLongitude());
                LatLng myLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(myLatLng).title(markerName));

            } else {
                Toast.makeText(this, "Not a good time", Toast.LENGTH_LONG);
            }
        }

    }*/

}

