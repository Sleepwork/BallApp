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

/*
    Recommencer le projet avec comme base MapsActivity
    la page principale permet de lancer le jeu ou réinitialisé le jeu
    lorsque le jeu est fini on peut proposer via un bouton d'enregistrer le score,
        se pose alors la question de l'enregistrement du score dans le tableau
    Elle possèdera une variable qui contiendra l'état du jeu 0,1,2
    elle permet également via le bouton option "3 petits points" d'aller vers le tableau des scores ou A propos
    Tous les "enfants" devront avoir une fleche de retour/back button
    Le tableau des scores et la map échangeront un fichier Hashmap qui contiendra l'id du jour, son pseudo et sa position
    Ce fichier devra être sauvegarder/charger depuis un xml ou une BD SQL

    Le tableau des scores quand à lui pourra permettre à l'utilisateur 2 actions:
        - enregistrer son score via un bouton si l'état du jeu est 2 (nécessite des autorisations)
        - consulter la position d'un joueur sélectionné dans la liste sur une maps
     Il devra également d'aller vers la map lorsque l'utilisateur sélectionne un joueur dans le tableau

     La map devra afficher la position du joueur sélectionné avec un marqueur qui aura pour titre son pseudo

     A propos, page pour écrire ce que j'ai envie

     Enregistrer un score, devra demander l'autorisation de localisation,
     en cas de rejet afficher une alertdialog pour expliquer la necessité de l'autorisation et revenir sur le tableau
     en cas d'accord avoir un edittext pour saisir le pseudo

     INTENT:
        De main à tableau, l'état du jeu
        tableau à map, l'id du joueur à localiser

     QUESTION
        Création d'une nouvelle activité pour l'ajout d'un pseudo&score dans le tableau?
        Persistence des pseudo, modification des scores des pseudo autorisé en cas de sélection d'un pseudo existant?
 */
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
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.scoreTab:
                Intent intent = new Intent(this, ScoreActivity.class);
                startActivity(intent);
                break;
            /*case android.R.id.home:
                if (accelSupported)
                    manager.unregisterListener(sView, mSensor);
                surfView.stopDrawing();
                this.finish();
                break;*/
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
