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
 * Layout de l'activité MatinActivity
 * Permet de gérer le dessin de la balle et des obstacles
 */

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    /**
     * ATTRIBUTS
     * posX, posY, speed --> Coordonnées de la balle et vitesse de deplacement
     * xVal, yVal --> coordonnées de déplacement de la balle
     * blockStuckIndex --> ID du bloc responsable de la collision
     * gameOverIntent --> intent initialisé après avoir perdu, envoi vers GameOverActivity
     * DrawingThread --> Processus gérant le rafraichissement de l'image
     * stuckVertical, stuckHorizontal --> possible collision avec le bloc dans la direction indiqué
     * goDown --> permet de savoir ou se trouve la zone de "touchdown"
     */
    private float posX, posY, speed;
    private int xVal, yVal, yStep, score, nbObstacles, ballSize, blockStuckIndex;
    private boolean stuckHorizontal ,stuckVertical, goDown;
    private boolean gameRunning = false;
    private MediaPlayer mainMusic, scoreMusic, deathMusic;
    private Intent gameOverIntent;
    private Context myContext;
    private ArrayList<Obstacle> listObstacle;
    private SurfaceHolder mSurfaceHolder;
    private DrawingThread mThread;

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

    //Définit ou Vérifie si le jeu est en cours
    public boolean isGameRunning(){
        return gameRunning;
    }
    public void setGameRunning(boolean running){
        gameRunning = running;
    }

    //Le jeu est-il terminé?
    public boolean isGameOver(){return (gameOverIntent != null);}

    //Arreter la musique en cours
    public void stopMusic(){
        if(scoreMusic.isPlaying())
            scoreMusic.stop();

        if(deathMusic.isPlaying())
            deathMusic.stop();

        if(mainMusic.isPlaying())
            mainMusic.pause();
    }

    //Effacer toutes les musiques
    public void releaseMusic(){
        if(mainMusic.isPlaying())
            mainMusic.stop();

        mainMusic.release();
        deathMusic.release();
        scoreMusic.release();
    }

    //Lorsque le joueur a perdu
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

    //Permet d'obtenir l'index de l'obstacle le plus proche de la balle
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
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}


    //Appeler lorsque l'activité passe au premer plan
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        //Si le jeu n'a pas encore été lancé on initialise les variables
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

    //Permet de relancer le dessin et l'avancement des blocs et de la balle
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

    //Permet de relancer le dessin et l'avancement des blocs et de la balle
    public void stopDrawing(){
        if(mThread!=null)
            mThread.keepDrawing = false;

        if(listObstacle != null && !listObstacle.isEmpty()) {
            for (Obstacle obstacle : listObstacle) {
                obstacle.stopMoving = true;
            }
        }

    }

    //Permet de completement arrêter les processus de dessin ldavancement des blocs et de la balle
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

    //Permet d'augmenter le nombre de lignes d'obstacles de 1
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

    //Permet d'arreter le dessin et etc. lorsque MainActivity passe en arrière plan
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mThread != null) {
            if (mThread.keepDrawing)
                stopDrawing();
        }
    }

    //Fonction permettant de dessiner sur la surface
    private void drawStuff(Canvas pCanvas) {

        Paint paint = new Paint();

        //Dessin du fond
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        pCanvas.drawPaint(paint);

        //Dessin des obstacles
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

        //Dessin de la zone de "touchdown"
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
                if(score%5 == 0)
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
                if(score%5 == 0)
                    difficultyUp();
            }
        }

        //Dessin de la balle
        paint.setColor(Color.RED);
        pCanvas.drawCircle(posX, posY, 20, paint);

    }

    //Déplacement de la balle d'une unité et gestion des collisions (imparfaites)
    private void moveBall(){

        boolean borderStuck = false;

        //Calcul de la nouvele position de la balle
        float newPosX, newPosY, deplacementX, deplacementY = 0;
        deplacementX = xVal * speed;
        deplacementX = Math.round(deplacementX);
        deplacementY = yVal * speed;
        deplacementY = Math.round(deplacementY);
        newPosX = posX - deplacementX;
        newPosY = posY + deplacementY;

        //gestion de la collision avec les bords
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

        //Récupération de la ligne d'obstacle la plus proche et de ses blocs
        int numligne = getClosestObstacle(newPosY);
        Obstacle obstacle = listObstacle.get(numligne);
        HashMap<Integer, Block> mapBlock = obstacle.getMapBlock();

        //Si la ligne a généré des blocs alors gestion des collisions
        if(mapBlock != null && !mapBlock.isEmpty()) {

            //Possibilité de collision horizontal
            if (!stuckVertical && obstacle.isInRange(newPosY)) {
                stuckHorizontal = true;
            } else {
                stuckHorizontal = false;
            }

            //Erreur de collision si le bloc de collision disparait, donc reinitialisation
            if(stuckVertical && blockStuckIndex < obstacle.getOldest())
                stuckVertical = false;

            //Analyse bloc par bloc
            Set<Map.Entry<Integer, Block>> set = mapBlock.entrySet();
            Iterator<Map.Entry<Integer, Block>> it = set.iterator();
            while(it.hasNext()){
                Map.Entry<Integer, Block> entry = it.next();
                Block block = entry.getValue();
                if (stuckHorizontal) {
                    //Verification de contact
                    if (block.isInRange(newPosX)) {
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
                    //Verification qu'il s'agit du bloc qui a signalé une possible collision
                    if(entry.getKey() == blockStuckIndex) {
                        //Verification de contact
                        if (block.isInRange(newPosX)) {
                            if (obstacle.isInRange(newPosY)) {
                                newPosY = obstacle.closestBorder(posY);
                            }
                        } else {
                            stuckVertical = false;
                        }
                    }
                }

                //Possibilité de collision vertical
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

    }

    //Récupération des valeurs de déplacements de la balle
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

    //Processus de rafraichissement de la sruface
    private class DrawingThread extends Thread {

        // Utilisé pour arrêter le dessin quand il le faut
        boolean keepDrawing = true;

        @Override
        public void run() {

            //Rafraichissement tant que keepDrawing = true
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
