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

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class MainMenuActivity extends AppCompatActivity {
    private MyApp app;
    private View lastView;


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
        lastView = view;
        // Show dialog asking for lobby code
        final EditText txtUrl = new EditText(this);

        // Set the default text
        txtUrl.setHint("Lobby Code");
        MainMenuActivity menu = this;

        new AlertDialog.Builder(this)
                .setTitle("Lobby Code")
                .setMessage("Please ask your host for the lobby code")
                .setView(txtUrl)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String lobbyCode = txtUrl.getText().toString();
                        app.gameLogic.joinGame(lobbyCode, menu);
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
    public void joinGame(String lobbyCode, GameLogicCodes code) {
        Log.d("joingame", code.toString());
        Snackbar mySnackbar = Snackbar.make(lastView, "placeholder", BaseTransientBottomBar.LENGTH_LONG);
        switch (code) {
            case SUCCESS:
                Intent intentLobby = new Intent(this, LobbyActivity.class);
                startActivity(intentLobby);
                return;
            case DATABASE_ERROR:
                mySnackbar.setText("Unexpected error, please contact devs");
                break;
            case GAME_NOT_FOUND_OR_CLOSED:
                mySnackbar.setText("Could not find that game. Are you online?");
                break;
        }
        mySnackbar.show();
    }
}