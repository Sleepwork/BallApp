package com.example.zanta.ballapp;

import android.graphics.Bitmap;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Zanta on 02/11/2017.
 */

public class Obstacle extends Thread{
    private int yAxis, spacing, speed, initialBlockL, initialBlockR, ballSize, widthTotal, idAvailable, top, bottom, youngest, oldest;
    public boolean keepRunning, stopMoving, direction;
    private HashMap<Integer, Block> mapBlock;

    public Obstacle(int y, int widthT, int bHeight, int ballS){
        yAxis = y;
        widthTotal = widthT;
        keepRunning = false;
        stopMoving = true;
        idAvailable = -1;
        ballSize = ballS;
        top = yAxis-bHeight+ballS*2;
        bottom = yAxis+bHeight-ballS*2;
        int choice = Math.round((float)Math.random() * (3 - 1) + 1);
        int blockWidth = 0;

        switch(choice){
            case 1:
                spacing = 50;
                speed = 4;
                blockWidth = 500;
                break;
            case 2:
                spacing = 100;
                speed = 6;
                blockWidth = 130;
                break;
            default:
                spacing = 75;
                speed = 7;
                blockWidth = 50;
        }

        direction = false;

        if(Math.random() < 0.5)
            direction = true;

        if(direction){
            initialBlockL = -blockWidth;
            initialBlockR= 0;
        } else {
            initialBlockL = widthTotal;
            initialBlockR= widthTotal + blockWidth;
        }

        mapBlock = new HashMap<Integer, Block>();
        Block b = new Block(initialBlockL, initialBlockR, ballSize, this.getIdAvailable());
        mapBlock.put(b.getId(), b);
        youngest = b.getId();
        oldest = youngest;
    }

    public int getTop(){return top;}
    public int getBottom(){return bottom;}
    public float distance(float posY){return Math.abs(yAxis - posY);}
    public int getOldest(){ return oldest;}

    public HashMap<Integer, Block> getMapBlock(){
        HashMap<Integer, Block> list = new HashMap<Integer, Block>();
        synchronized (mapBlock) {
            list.putAll(mapBlock);
        }
        return list;
    }

    public int getIdAvailable(){
        idAvailable++;
        return idAvailable;
    }

    // Savoir si un point est sur l'axe horizontal des obstacles
    public boolean isInRange(float posY){
        boolean inRange = false;

        if(top - ballSize - 1<= posY && posY <= bottom + ballSize + 1)
            inRange = true;

        return inRange;
    }

    public float closestBorder(float posY){
        float closest;
        closest = top - ballSize;
        float distTop;
        distTop = posY - top;
        distTop = Math.abs(distTop);
        float distBottom;
        distBottom = posY - bottom;
        distBottom = Math.abs(distBottom);

        if(distTop > distBottom)
            closest = bottom + ballSize;

        return closest;
    }

    public void run(){

        while(keepRunning){
            if(!stopMoving) {
                HashMap<Integer, Block> newMap = new HashMap<Integer, Block>();
                synchronized (mapBlock) {
                    newMap.putAll(mapBlock);
                }

                Set<Map.Entry<Integer, Block>> set = newMap.entrySet();
                Iterator<Map.Entry<Integer, Block>> it = set.iterator();

                while (it.hasNext()) {
                    Map.Entry<Integer, Block> entry = it.next();
                    Block b = entry.getValue();
                    b.moveForward(speed, direction);

                    if (oldest == b.getId()) { //The oldest
                        if (direction) {
                            if (b.getL() >= widthTotal+spacing) {
                                synchronized (mapBlock) {
                                    mapBlock.remove(entry.getKey());
                                }
                                oldest++;
                            }
                        } else {
                            if (b.getR() <= -spacing) {
                                synchronized (mapBlock) {
                                    mapBlock.remove(entry.getKey());
                                }
                                oldest++;
                            }
                        }
                    }

                    if (youngest == b.getId()) { // The youngest
                        if (direction) {
                            if (b.getL() >= spacing && b.isYoungest()) {
                                Block newB = new Block(initialBlockL, initialBlockR, ballSize, this.getIdAvailable());
                                youngest++;
                                synchronized (mapBlock) {
                                    mapBlock.put(newB.getId(), newB);
                                }
                            }
                        } else {
                            if (b.getR() <= widthTotal - spacing && b.isYoungest()) {
                                Block newB = new Block(initialBlockL, initialBlockR, ballSize, this.getIdAvailable());
                                youngest++;
                                synchronized (mapBlock) {
                                    mapBlock.put(newB.getId(), newB);
                                }
                            }
                        }
                    }

                }

                try {
                    Thread.sleep(18);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
