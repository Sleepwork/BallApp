package com.example.zanta.ballapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Zanta on 10/10/2017.
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    private float posX, posY, speed;
    private int xVal, yVal, yStep, score, nbObstacles, ballSize, blockStuckIndex;
    private boolean stuckHorizontal ,stuckVertical, goDown;
    private boolean gameRunning = false;
    private MediaPlayer mainMusic, scoreMusic, deathMusic;
    private Intent gameOverIntent;
    private Context myContext;
    private ArrayList<Obstacle> listObstacle;


    // Le holder
    private SurfaceHolder mSurfaceHolder;

    // Le thread dans lequel le dessin se fera
    private DrawingThread mThread;

    //private Obstacle obstacle;

    public MySurfaceView (Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        ballSize = 20;
        myContext = context;

        scoreMusic=MediaPlayer.create(myContext,R.raw.hangouts_message);
        scoreMusic.setLooping(false);
        deathMusic=MediaPlayer.create(myContext,R.raw.shindeiru);
        deathMusic.setLooping(false);
        mainMusic=MediaPlayer.create(myContext,R.raw.rooftop_run_modern);
        mainMusic.setLooping(true);

        gameOverIntent = null;
    }

    public boolean isGameRunning(){
        return gameRunning;
    }
    public void setGameRunning(boolean running){
        gameRunning = running;
    }
    public boolean isGameOver(){return (gameOverIntent != null);}


    public void stopMusic(){
        if(scoreMusic.isPlaying())
            scoreMusic.stop();

        if(deathMusic.isPlaying())
            deathMusic.stop();

        if(mainMusic.isPlaying())
            mainMusic.pause();
    }

    public void releaseMusic(){

        if(mainMusic.isPlaying())
            mainMusic.stop();

        mainMusic.release();
        deathMusic.release();
        scoreMusic.release();
    }

    public void gameOver(){

        deathMusic.start();
        while(deathMusic.isPlaying()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopMusic();

        gameOverIntent = new Intent(myContext, GameOverActivity.class);
        gameOverIntent.putExtra("GameOver", true);
        gameOverIntent.putExtra("score", score);
        myContext.startActivity(gameOverIntent);

    }

    public int getClosestObstacle(final float currentPosY){
        ArrayList<Obstacle> sortObstacle = new ArrayList<Obstacle>();
        sortObstacle.addAll(listObstacle);

        // Descending Order
        Collections.sort(sortObstacle, new Comparator<Obstacle>() {

            @Override
            public int compare(Obstacle m1, Obstacle m2) {
                int result = Math.round(m1.distance(currentPosY) - m2.distance(currentPosY));
                if(result == 0)
                    result++;
                return result;
            }
        });

        return listObstacle.indexOf(sortObstacle.get(0));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Que faire quand le surface change ? (L'utilisateur tourne son téléphone par exemple)
        //Log.i("Test3", "");

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //Log.i("Test2", ""+mSurfaceHolder.equals(holder));
        if(!gameRunning) {

            listObstacle = new ArrayList<Obstacle>();
            stuckHorizontal = false;
            stuckVertical = false;
            blockStuckIndex = -1;
            goDown = true;
            nbObstacles = 1;
            score = 0;
            posX = getWidth()/2;
            posY = ballSize;
            yStep = getHeight()/(nbObstacles + 1);
            int height = yStep/2;


            for (int i = 1; i < (nbObstacles + 1); i++) {
                Obstacle obstacle = new Obstacle(yStep * i, getWidth(), height, ballSize);
                obstacle.setDaemon(true);
                obstacle.keepRunning = true;
                obstacle.stopMoving = false;
                listObstacle.add(obstacle);
            }

            for (Obstacle obstacle : listObstacle) {
                obstacle.start();
            }
            gameRunning = true;
        }


    }

    public void resumeDrawing(){
        if(gameRunning) {
            for (Obstacle obstacle : listObstacle) {
                obstacle.stopMoving = false;
            }

            mThread = new DrawingThread();
            mThread.keepDrawing = true;
            mThread.start();
            mainMusic.start();
        }
    }

    public void stopDrawing(){
        if(mThread!=null)
            mThread.keepDrawing = false;

        if(listObstacle != null && !listObstacle.isEmpty()) {
            for (Obstacle obstacle : listObstacle) {
                obstacle.stopMoving = true;
            }
        }
        //obstacle.keepRunning = false;

    }

    public void stopRunning(){
        if(listObstacle != null && !listObstacle.isEmpty()) {
            int i = 0;
            while (i < listObstacle.size()) {
                Obstacle obstacle = listObstacle.get(i);
                obstacle.keepRunning = false;
                try {
                    obstacle.join();
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void difficultyUp(){
        stopDrawing();
        stopRunning();

        listObstacle = new ArrayList<Obstacle>();
        stuckHorizontal = false;
        stuckVertical = false;
        blockStuckIndex = -1;
        nbObstacles++;
        yStep = getHeight()/(nbObstacles + 1);
        int height = yStep/2;

        for(int i = 1; i<(nbObstacles + 1); i++){
            Obstacle obstacle = new Obstacle(yStep*i, getWidth(), height, ballSize);
            obstacle.setDaemon(true);
            obstacle.keepRunning = true;
            obstacle.stopMoving = false;
            listObstacle.add(obstacle);
        }

        for(Obstacle obstacle : listObstacle){
            obstacle.start();
        }


        mThread = new DrawingThread();
        mThread.keepDrawing = true;
        mThread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mThread != null) {
            if (mThread.keepDrawing)
                stopDrawing();
        }
        //Log.i("Test4", "");

    }

    private void drawStuff(Canvas pCanvas) {

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        pCanvas.drawPaint(paint);

        paint.setColor(Color.BLACK);

        for(Obstacle obstacle : listObstacle) {
            int yTop = obstacle.getTop();
            int yBottom = obstacle.getBottom();

            HashMap<Integer, Block> mapBlock = obstacle.getMapBlock();
            if (mapBlock != null && !mapBlock.isEmpty()) {
                Set<Map.Entry<Integer, Block>> set = mapBlock.entrySet();
                Iterator<Map.Entry<Integer, Block>> it = set.iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Block> entry = it.next();
                    Block blockToDraw = entry.getValue();
                    pCanvas.drawRect(blockToDraw.getL(), yTop, blockToDraw.getR(), yBottom, paint);
                }
            }
        }

        paint.setTextSize(75);
        paint.setColor(Color.BLUE);
        if(goDown) {
            int top = getHeight()-yStep/2;
            pCanvas.drawRect(0, top, getWidth(), getHeight(), paint);
            paint.setColor(Color.BLACK);
            pCanvas.drawText(""+score, getWidth()/2, top+yStep/4, paint);
            if(top <= posY ) {
                goDown = false;
                score++;
                if(scoreMusic.isPlaying())
                    scoreMusic.stop();
                scoreMusic.start();
                if(score%7 == 0)
                    difficultyUp();
            }

        }else{
            int bottom = yStep/2;
            pCanvas.drawRect(0, 0, getWidth(), bottom, paint);
            paint.setColor(Color.BLACK);
            pCanvas.drawText(""+score, getWidth()/2, bottom/2, paint);
            if(bottom >= posY ) {
                goDown = true;
                score++;
                if(scoreMusic.isPlaying())
                    scoreMusic.stop();
                scoreMusic.start();
                if(score%7 == 0)
                    difficultyUp();
            }
        }

        paint.setColor(Color.RED);
        pCanvas.drawCircle(posX, posY, 20, paint);

    }

    private void moveBall(){

        boolean borderStuck = false;
        float newPosX, newPosY, deplacementX, deplacementY = 0;
        deplacementX = xVal * speed;
        deplacementX = Math.round(deplacementX);
        deplacementY = yVal * speed;
        deplacementY = Math.round(deplacementY);
        newPosX = posX - deplacementX;
        newPosY = posY + deplacementY;

        if(newPosX >= getWidth()) {
            newPosX = getWidth();
            borderStuck = true;
        } else if(newPosX <= 0){
            newPosX = 0;
            borderStuck = true;
        }

        if(newPosY >= getHeight()){
            newPosY = getHeight();

        }else if(newPosY <= 0){
            newPosY = 0;

        }

        int numligne = getClosestObstacle(newPosY);
        //Log.i("moveBall", "N°" + numligne);
        Obstacle obstacle = listObstacle.get(numligne);
        HashMap<Integer, Block> mapBlock = obstacle.getMapBlock();

        if(mapBlock != null && !mapBlock.isEmpty()) {

            if (!stuckVertical && obstacle.isInRange(newPosY)) {
                stuckHorizontal = true;
            } else {
                stuckHorizontal = false;
            }

            if(stuckVertical && blockStuckIndex < obstacle.getOldest())
                stuckVertical = false;

            //Log.i("moveBall", "stuckHorizontal " + stuckHorizontal);
            //Log.i("moveBall", "stuckVertical " + stuckVertical);

            Set<Map.Entry<Integer, Block>> set = mapBlock.entrySet();
            Iterator<Map.Entry<Integer, Block>> it = set.iterator();
            while(it.hasNext()){
                Map.Entry<Integer, Block> entry = it.next();
                Block block = entry.getValue();
                if (stuckHorizontal) {
                    if (block.isInRange(newPosX)) {

                        //Log.i("moveBall", "moveX");

                        newPosX = block.closestBorder(newPosX);
                        if (borderStuck) {
                            stopDrawing();
                            stopRunning();
                            setGameRunning(false);
                            stopMusic();
                            gameOver();
                        }
                    }

                }else if (stuckVertical) {
                    if(entry.getKey() == blockStuckIndex) {
                        if (block.isInRange(newPosX)) {
                            if (obstacle.isInRange(newPosY)) {
                                newPosY = obstacle.closestBorder(posY);
                                //Log.i("moveBall stuckVertical", block.getL() + ": " + posY + "/" + deplacementY);
                            }

                        } else {
                            stuckVertical = false;
                        }
                    }
                }

                if(!stuckVertical&&!stuckHorizontal){
                    if (block.isInRange(newPosX)) {
                        stuckVertical = true;
                        blockStuckIndex = entry.getKey();
                    }
                }
            }
        }

        posX = newPosX;
        posY = newPosY;

        //Log.i("moveBall", ""+getWidth());
        //Log.i("moveBall", "posX " + posX);
        //Log.i("moveBall", "posY " + posY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            xVal = Math.round(event.values[0]/10);
            yVal = Math.round(event.values[1]/10);
            speed = (9.81f - Math.abs(Math.round(event.values[2]))) * 2f;
            //Log.i("SensorChanged", "" + xVal + " " + yVal + " " + speed);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private class DrawingThread extends Thread {

        // Utilisé pour arrêter le dessin quand il le faut
        boolean keepDrawing = true;

        @Override
        public void run() {

            while (keepDrawing) {
                Canvas canvas = null;

                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    // On s'assure qu'aucun autre thread n'accède au holder
                    synchronized (mSurfaceHolder) {
                        // Et on dessine
                        drawStuff(canvas);
                        moveBall();
                    }
                } finally {
                    if (canvas != null)
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                // Pour dessiner à 60 fps
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {}
            }
        }
    }
}
