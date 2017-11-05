package com.example.zanta.ballapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Zanta on 05/11/2017.
 */

public class GameOverActivity extends AppCompatActivity {

    private FloatingActionButton fabSave, fabReset;
    private int score;
    private MediaPlayer afterLifeMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);

        setSupportActionBar(toolbar);

        afterLifeMusic= MediaPlayer.create(this,R.raw.krone);
        afterLifeMusic.setLooping(true);

        final AppCompatActivity me = this;
        fabSave = (FloatingActionButton) findViewById(R.id.fabS);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ScoreActivity.class);
                intent.putExtra("GameOver", true);
                intent.putExtra("score", score);
                startActivity(intent);
                me.finish();
            }
        });

        fabReset = (FloatingActionButton) findViewById(R.id.fabR);
        fabReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getBaseContext(), MainActivity.class);
                //startActivity(intent);
                me.finish();
            }
        });

        Intent intent = getIntent();
        score = intent.getIntExtra("score", -1);
        intent.removeExtra("score");

        TextView tv = (TextView) findViewById(R.id.tvScore);
        tv.setText("Score: " + score);
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
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        afterLifeMusic.start();

    }

    @Override
    public void onPause() {
        afterLifeMusic.pause();
        super.onPause();
    }

    @Override
    public void onDestroy(){
        afterLifeMusic.stop();
        super.onDestroy();
    }
}
