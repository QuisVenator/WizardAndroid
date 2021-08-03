package com.renepauls.kingsandfools;

import java.util.ArrayList;
import java.util.Date;
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
        currentTurn = 0;
        playerCount = 1;
        turnStarted = (new Date()).getTime();
    }
}
