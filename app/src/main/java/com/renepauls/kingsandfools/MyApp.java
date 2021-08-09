package com.renepauls.kingsandfools;

import android.app.Application;

public class MyApp extends Application {
    protected GameLogic gameLogic;

    @Override
    public void onCreate() {
        super.onCreate();

        gameLogic = new GameLogic();
    }
}
