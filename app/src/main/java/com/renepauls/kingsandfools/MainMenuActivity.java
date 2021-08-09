package com.renepauls.kingsandfools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        Intent intentLobby = new Intent(this, LobbyActivity.class);
        startActivity(intentLobby);
    }
}