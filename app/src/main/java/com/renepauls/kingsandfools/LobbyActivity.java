package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LobbyActivity extends AppCompatActivity {
    private MyApp app;
    private LinearLayout playerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        app = (MyApp) getApplication();

        ((TextView)findViewById(R.id.textViewLobby)).append(app.gameLogic.getSessionId());
        playerList = findViewById(R.id.playerList);

        addPlayerToList(app.gameLogic.getName());
    }

    private void addPlayerToList(String name) {
        TextView newPlayer = new TextView(this);
        newPlayer.setText(name);

        playerList.addView(newPlayer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", String.valueOf(this.isFinishing()));
    }
}