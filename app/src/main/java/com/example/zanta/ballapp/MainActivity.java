package com.example.zanta.ballapp;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    private SensorEventListener sView;
    private Sensor mSensor;
    private SensorManager manager;
    private boolean accelSupported;
    private MySurfaceView surfView;
    private FloatingActionButton fabPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        surfView = (MySurfaceView)  findViewById(R.id.surfaceView);
        sView = (SensorEventListener) findViewById(R.id.surfaceView);
        manager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        mSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        fabPlay = (FloatingActionButton) findViewById(R.id.fabP);
        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surfView.resumeDrawing();
                Log.i("MainActivity", "game start/resume");
                fabPlay.setVisibility(View.INVISIBLE);
            }
        });

        fabPlay.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.scoreTab:
                Intent intent = new Intent(this, ScoreActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        accelSupported = manager.registerListener(sView, mSensor, SensorManager.SENSOR_DELAY_GAME);
        if(surfView.isGameOver()) {
            fabPlay.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onPause() {
        if (accelSupported)
            manager.unregisterListener(sView, mSensor);
        //mainMusic.pause();
        if(surfView.isGameRunning()) {
            surfView.stopDrawing();
            surfView.stopMusic();
            fabPlay.setVisibility(View.VISIBLE);
            Log.i("MainActivity", "game pause");
        }
        super.onPause();
    }

    @Override
    public void onDestroy(){
        if(surfView != null) {
            surfView.stopDrawing();
            surfView.stopRunning();
            surfView.releaseMusic();
        }
        super.onDestroy();
    }

}
