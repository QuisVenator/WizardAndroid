package com.renepauls.kingsandfools;

import android.content.Intent;
import android.renderscript.Sampler;
import android.util.Log;
import java.util.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GameLogic {
    private static boolean isHost;
    private String trump = null;
    private String leading = null;
    private String name = "Anonimous";
    private Card currentWinning;

    private static final int sessionIdCharacterCount = 5;
    private static final String validSessionCharacters = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
    private String sessionId;
    private Session currentSession;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference sessionReference = null;
    private ChildEventListener playerAddListener;
    private ValueEventListener turnListener;
    private ValueEventListener cardPlayedListener;
    private ValueEventListener startGameListener;
    private int myTurn;
    private String playerKey;
    private HashMap<String, String> connectedPlayersDict = new HashMap<>();

    //TODO this is bad, do MVVM
    public LobbyActivity lobbyActivity = null;

    public String getSessionId() {
        return sessionId;
    }

    public boolean allowedToPlay(Card card, Hand hand) {
        if(!hasTurn()) return false;
        if(leading == null) return true;
        if(card.getType().equals(leading)) return true;
        if(!hand.hasType(leading)) return true;
        if(card.getType().equals("wizard") || card.getType().equals("jester")) return true;
        return false;
    }

    public boolean playCard(Card card, Hand hand) {
        if(!allowedToPlay(card, hand)) return false;

        hand.remove(card);
        updateTrumpAndWinning(card);

        // TODO animate shit
        // TODO update database and pass turn
        endTurn(card);
        hand.remove(card);

        return true;
    }

    public void joinGame(String id, MainMenuActivity menu) {
        currentSession = new Session();
        sessionId = id.toUpperCase();

        sessionReference = database.getReference("sessions/"+sessionId);
        sessionReference.child("open").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    menu.joinGame(id, GameLogicCodes.DATABASE_ERROR);
                }
                else {
                    if(task.getResult().getValue() != null && (boolean)task.getResult().getValue()){
                        playerKey = addPlayer();
                        subscribeToGameStart();
                        menu.joinGame(id, GameLogicCodes.SUCCESS);
                    } else {
                        Log.d("Placeholder", "Game doesn't exist or is closed");
                        menu.joinGame(id, GameLogicCodes.GAME_NOT_FOUND_OR_CLOSED);
                    }
                }
            }
        });

        isHost = false;
    }
    public String hostGame() {
        currentSession = new Session();
        DatabaseReference myRef = database.getReference("sessions");

        sessionId = generateSessionId();
        myRef.child(sessionId).setValue(currentSession);
        sessionReference = myRef.child(sessionId);
        isHost = true;

        playerKey = addPlayer();
        subscribeToGameStart();
        return sessionId;
    }
    public void subscribePlayerList(IPlayerList playerList) {
        playerAddListener = sessionReference.child("playerList").addChildEventListener(new ChildEventListener() {
            // This also fires once for every item already in the list
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                addPlayer(snapshot.getKey(), snapshot.getValue().toString());
                if(snapshot.getKey().equals(playerKey)) {
                    setTurnNow();
                }
                playerList.addPlayerToList(snapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                throw new RuntimeException("Unexpected database event, player in list changed");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                removePlayer(snapshot.getKey());
                playerList.removePlayerFromList(snapshot.getValue().toString());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                throw new RuntimeException("Unexpected database event, player moved?");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new RuntimeException("Unexpected database event, list of players deleted");
            }
        });
    }
    private void onTurnSubscriber() {
        turnListener = sessionReference.child("currentTurn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentSession.currentTurn = (int) snapshot.getValue();
                if(hasTurn()) {
                    // TODO inform that currently has turn
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // TODO throw exception?
            }
        });
    }
    private void onCardPlayedSubscriber() {
        cardPlayedListener = sessionReference.child("lastCardPlayed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // TODO animate and shit
                currentSession.lastCardPlayed = snapshot.getValue(Card.class);
                updateTrumpAndWinning(currentSession.lastCardPlayed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // TODO throw exception?
            }
        });
    }
    private void subscribeToGameStart() {
        startGameListener = sessionReference.child("open").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(boolean) snapshot.getValue()) {
                    lobbyActivity.startGame();
                    return;
                } else {
                    Log.d("startgame", "What?");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // TODO throw exception?
            }
        });
    }

    public int getConnectedPlayerCount() {
        return currentSession.playerCount;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    private void updateTrumpAndWinning(Card card) {
        //if this is first card played
        if(currentWinning == null) {
            currentWinning = card;
            trump = currentWinning.getType();
        }
        //if the winning card is a jester and this one is not (first jester always wins)
        else if(currentWinning.getType().equals("jester") && !card.getType().equals("jester")) {
            currentWinning = card;
            trump = currentWinning.getType();
        }
        //if the winning card is a wizard, that can't be beat (first wizard always wins)
        else if(currentWinning.getType().equals("wizard"))
        { /*nothing*/ }
        //if the wining is not a wizard, but a wizard was played
        else if(card.getType().equals("wizard"))
            currentWinning = card;
            //if trump is winning and no trump was played
        else if(currentWinning.getType().equals(trump) && !card.getType().equals(trump))
        { /*nothing*/ }
        //at least one card isn't trump, so if they are equal they must both be leading
        else if (currentWinning.getType().equals(card.getType()) && currentWinning.getValue() < card.getValue())
            currentWinning = card;
    }

    private String addPlayer() {
        // TODO get some way to let players choose a name
        DatabaseReference childRef = sessionReference.child("playerList").push();
        String playerKey = childRef.getKey();
        childRef.setValue(name);
        sessionReference.child("playerCount").setValue(ServerValue.increment(1));

        return playerKey;
    }

    private void endTurn(Card card) {
        // update locally
        currentSession.lastCardPlayed = card;
        currentSession.turnStarted = System.currentTimeMillis();
        currentSession.currentTurn += 1;
        currentSession.currentTurn /= currentSession.playerCount;

        // prepare update for database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lastCardPlayed", currentSession.lastCardPlayed);
        childUpdates.put("/turnStarted", currentSession.turnStarted);
        childUpdates.put("/currentTurn", currentSession.currentTurn);

        // update database
        sessionReference.updateChildren(childUpdates);
    }

    public void addPlayer(String key, String playerName) {
        connectedPlayersDict.put(key, playerName);
        currentSession.playerCount++;
    }
    public void removePlayer(String key) {
        connectedPlayersDict.remove(key);
    }

    public void setTurnNow() {
        myTurn = currentSession.playerCount-1;
    }

    private static String generateSessionId() {
        String sessionId = "";
        for(int i = 0; i < sessionIdCharacterCount; i++) {
            int charAt = (int)(Math.random()* validSessionCharacters.length());
            sessionId += validSessionCharacters.charAt(charAt);
        }
        return sessionId;
    }

    private boolean hasTurn() {
        return currentSession != null && currentSession.currentTurn != -1 && myTurn != -1 && currentSession.currentTurn == myTurn;
    }

    public void startGame() {
        Log.d("startgame", "function called");
        //update locally
        currentSession.open = false;

        //prepare update for database
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/open", currentSession.open);

        //update database
        sessionReference.updateChildren(childUpdates);
    }

    public void removeListeners() {
        if(playerAddListener != null) {
            sessionReference.child("playerList").removeEventListener(playerAddListener);
            playerAddListener = null;
        }
        if(turnListener != null) {
            sessionReference.child("currentTurn").removeEventListener(turnListener);
            turnListener = null;
        }
        if(cardPlayedListener != null) {
            sessionReference.child("lastCardPlayed").removeEventListener(cardPlayedListener);
            cardPlayedListener = null;
        }
        if(startGameListener != null) {
            sessionReference.child("open").removeEventListener(startGameListener);
            startGameListener = null;
        }
    }
}
