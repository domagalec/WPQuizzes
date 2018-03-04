package com.kdomagala.wpquizzes;

import android.content.Intent;
import android.graphics.Bitmap;

public class Quiz {
    private static final String ITEM_SEP = System.getProperty("line.separator");

    public final static String TITLE = "title";
    public final static String RESULT = "result";


    private String mTitle;
    private String mResult;
    private Bitmap mBitmap;

    Quiz(String title, String result, Bitmap bitmap) {

        this.mTitle = title;
        this.mResult = result;
        this.mBitmap = bitmap;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) { mTitle = title;}

    public String getResult() {return mResult; }

    public void setResult(String result) {mResult = result;}

    public Bitmap getBitmap() {return mBitmap;}

    public void setBitmap(Bitmap bitmap){mBitmap = bitmap;}

    public static void packageIntent(Intent intent, String title) {

        intent.putExtra(Quiz.TITLE, title);

    }
}
