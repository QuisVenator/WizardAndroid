package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

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
        app.gameLogic.lobbyActivity = this;
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

    public void startGame(View view) {
        if(app.gameLogic.getConnectedPlayerCount() >= 3 && app.gameLogic.isHost()) {
            app.gameLogic.startGame();
        } else if(app.gameLogic.isHost()) {
            Snackbar mySnackbar = Snackbar.make(view, "Need at least 3 players to start", BaseTransientBottomBar.LENGTH_LONG);
            mySnackbar.show();
        } else {
            Snackbar mySnackbar = Snackbar.make(view, "Only the host can start the game", BaseTransientBottomBar.LENGTH_LONG);
            mySnackbar.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all listeners from database
        app.gameLogic.removeListeners();
        Log.d("playerTurn", String.valueOf(this.isFinishing()));
    }

    public void startGame() {
        Log.d("startgame", "Starting activity...");
        Intent intentGame = new Intent(this, MainActivity.class);
        startActivity(intentGame);
        return;
    }
}