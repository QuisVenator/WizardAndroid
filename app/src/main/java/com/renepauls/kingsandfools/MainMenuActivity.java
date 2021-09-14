package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainMenuActivity extends AppCompatActivity {
    private MyApp app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        app = (MyApp) getApplication();
    }

    public void hostNewGame(View view) {
        app.gameLogic.hostGame();
        Intent intentLobby = new Intent(this, LobbyActivity.class);
        startActivity(intentLobby);
    }

    public void joinGame(View view) {
        // Show dialog asking for lobby code
        final EditText txtUrl = new EditText(this);

        // Set the default text
        txtUrl.setHint("Lobby Code");

        new AlertDialog.Builder(this)
                .setTitle("Lobby Code")
                .setMessage("Please ask your host for the lobby code")
                .setView(txtUrl)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String lobbyCode = txtUrl.getText().toString();
                        joinGame(lobbyCode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Just returns to main menu
                    }
                })
                .show();

    }

    // Gets called by dialog
    public void joinGame(String lobbyCode) {
        Log.d("joingame", lobbyCode);
        app.gameLogic.joinGame(lobbyCode);
        Intent intentLobby = new Intent(this, LobbyActivity.class);
        startActivity(intentLobby);
    }
}