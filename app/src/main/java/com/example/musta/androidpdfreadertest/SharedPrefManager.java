package com.example.musta.androidpdfreadertest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by musta on 1/4/18.
 */

public class SharedPrefManager {
    private static SharedPrefManager instance = null;
    private static Context mContext;
    private static SharedPreferences mSharedPreference;
    private static String PREF_NAME = "doc_pref";
    private String TAG = SharedPrefManager.class.getSimpleName();

    private SharedPrefManager() {
    }

    public static SharedPrefManager getInstance(Context context) {
        mContext = context;
        mSharedPreference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (instance == null) instance = new SharedPrefManager();
        return instance;
    }

    public void updateCurrentPage(int currentPage) {
        Log.i(TAG, "updateCurrentPage: " + currentPage);
        if (currentPage < 0) return;
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.putInt("page", currentPage);
        editor.apply();
    }

    public int getCurrentPage() {
        int page = mSharedPreference.getInt("page", 0);
        Log.i(TAG, "getCurrentPage: " + page);
        return page;
    }

}
