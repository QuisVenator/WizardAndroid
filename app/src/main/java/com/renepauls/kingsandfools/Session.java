package com.renepauls.kingsandfools;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Session {
    public boolean open = true;
    public int currentTurn;
    public long turnStarted;
    public int playerCount;
    public Card lastCardPlayed = null;

    public Session() {
        this.playerCount = playerCount;
        currentTurn = -1;
        playerCount = 0;
        turnStarted = (new Date()).getTime();
    }

}
