package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LobbyActivity extends AppCompatActivity implements IPlayerList {
    private MyApp app;
    private LinearLayout playerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        app = (MyApp) getApplication();

        ((TextView)findViewById(R.id.textViewLobby)).append(app.gameLogic.getSessionId());
        playerList = findViewById(R.id.playerList);

        app.gameLogic.subscribePlayerList(this);
    }

    public void addPlayerToList(String name) {
        TextView newPlayer = new TextView(this);
        newPlayer.setText(name);

        playerList.addView(newPlayer);
    }

    public void removePlayerFromList(String name) {
        for(int i = 0; i < playerList.getChildCount(); i++) {
            TextView playerView = (TextView) playerList.getChildAt(i);
            if(playerView.getText().equals(name)) {
                playerList.removeView(playerView);
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lifecycle", String.valueOf(this.isFinishing()));
    }
}