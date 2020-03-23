package kr.ac.hansung.example.cardproject;

import android.graphics.Rect;

/**
 * Created by seong on 2018-11-15.
 */

public class Card {
    public static final int CARD_SHOW=0;
    public static final int CARD_CLOSE=1;
    public static final int CARD_OPEN=2;
    public static final int CARD_MATCHED=3;
    public static final int IMAGE_A=0;
    public static final int IMAGE_B=1;
    public static final int IMAGE_C=2;
    public static final int IMAGE_D=3;
    public static final int IMAGE_E=4;
    public static final int IMAGE_F=5;
    public static final int IMAGE_G=6;
    public static final int IMAGE_H=7;
    public static final int IMAGE_I=8;
    public static final int IMAGE_J=9;
    public static final int IMAGE_K=10;
    public static final int IMAGE_L=11;

    public int state;
    public int color;

    public Card(int color){
        state = CARD_CLOSE;
        this.color = color;
    }
}
