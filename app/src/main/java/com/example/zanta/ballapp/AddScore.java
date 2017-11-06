package com.example.zanta.ballapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Zanta on 14/10/2017.
 */

public class AddScore extends Activity implements LocationListener{

    private boolean mLocationPermissionGranted;
    private LocationManager locationManager;
    private Location mLastKnownLocation;
    private String locationProvider;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Button btCancel;
    private Button btSave;
    private EditText editPseudo;
    private EditText editScore;
    private Activity me = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        //Intent intent = getIntent();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationProvider  = LocationManager.GPS_PROVIDER;
        if (ContextCompat.checkSelfPermission(me.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            locationManager.requestLocationUpdates(locationProvider, 5000, 1000.0f, this);
            mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        } else {
            ActivityCompat.requestPermissions(me,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        Log.i("Is Provider: ", ""+locationManager.isProviderEnabled(locationProvider));
        mLocationPermissionGranted = false;

        if(locationManager.getProviders(true) != null){
            for(String provider : locationManager.getProviders(true)){
                Log.i("Providers: ", provider);
            }
        }

        this.btCancel = (Button) findViewById(R.id.btnCancel);
        this.btSave = (Button) findViewById(R.id.btnSave);
        this.editScore = (EditText) findViewById(R.id.editScore);
        this.editPseudo = (EditText) findViewById(R.id.editPseudo);


        //if()
        //Button Listeners
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getBaseContext(), ScoreActivity.class);
                intent.putExtra("pseudo", editPseudo.getText().toString());

                if (Integer.parseInt(editScore.getText().toString()) < 1) {
                    intent.putExtra("score", "0");
                } else {
                    intent.putExtra("score", editScore.getText().toString());
                }

                //Toast.makeText(getBaseContext(), editScore.getText().toString(), Toast.LENGTH_LONG).show();


                if (ContextCompat.checkSelfPermission(me.getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                } else {
                    ActivityCompat.requestPermissions(me,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }

                if (mLocationPermissionGranted) {
                    Log.i("Is Provider: ", ""+locationManager.isProviderEnabled(locationProvider));
                    mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                    if(mLastKnownLocation!=null) {
                        intent.putExtra("lat", mLastKnownLocation.getLatitude());
                        intent.putExtra("lng", mLastKnownLocation.getLongitude());
                        Log.i("lat", "" + mLastKnownLocation.getLatitude());
                        Log.i("lng", "" + mLastKnownLocation.getLongitude());
                    }
                }

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Intent intent = getIntent();
        int score =  intent.getIntExtra("score", -1);
        if(score  >= 0){
            editScore.setText(""+score);
            intent.removeExtra("score");
        }
        editScore.setEnabled(false);
    }

    @Override
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
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mLocationPermissionGranted = true;
                        mLastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
                        Log.i("Location Manager", "Permission = true");
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i("Autorisation refusée", "Spammage toutes les 10 secs");

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
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
