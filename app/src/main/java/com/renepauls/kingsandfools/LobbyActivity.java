package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class LobbyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
    }

    private void addPlayerToList(String name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", String.valueOf(this.isFinishing()));

    }
}