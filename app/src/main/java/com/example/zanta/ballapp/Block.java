package com.example.zanta.ballapp;

import android.util.Log;

/**
 * Created by Zanta on 02/11/2017.
 */

public class Block {
    private int left, right, id, ballSize;
    private boolean youth;

    public Block(int l,int r, int ballS, int generateId){
        left = l;
        right = r;
        youth = true;
        ballSize = ballS;
        id = generateId;
    }

    public void moveForward(int move, boolean direction){
        if(direction) {
            left += move;
            right += move;
        }else{
            left -= move;
            right -= move;
        }
    }


    public int getL(){
        return left;
    }
    public int getR(){return right;}
    public int getId(){return id;}

    //Savoir si un point est sur l'axe vertical d'un bloc
    public boolean isInRange(float posX){
        boolean inRange = false;

        if(left - ballSize - 1 <= posX && posX <= right + ballSize + 1)
            inRange = true;

        return inRange;
    }

    public float closestBorder(float posX){
        float closest;
        float distLeft = posX - left;
        float distRight = posX - right;
        distLeft = Math.abs(distLeft);
        distRight = Math.abs(distRight);
        closest = left - ballSize - 1;

        if(distLeft > distRight)
            closest = right + ballSize + 1;

        return closest;
    }

    public boolean isYoungest(){
        boolean oldYouth = youth;
        if(youth)
            youth = false;
        return oldYouth;
    }
}
