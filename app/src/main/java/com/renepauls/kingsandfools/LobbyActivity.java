package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LobbyActivity extends AppCompatActivity {
    private MyApp app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        app = (MyApp) getApplication();

        ((TextView)findViewById(R.id.textViewLobby)).append(app.gameLogic.getSessionId());
    }

    private void addPlayerToList(String name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", String.valueOf(this.isFinishing()));
    }
}