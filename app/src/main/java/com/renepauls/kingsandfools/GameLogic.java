package com.renepauls.kingsandfools;

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
    private String lead = null;
    private String trump = null;
    private String name = "Anonimous";
    private Card currentWinning;
    private Hand currentHand;
    private int roundNumber = 0;

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
    private ValueEventListener handListener;
    //TODO remove next two listeners
    private ValueEventListener trumpListener;
    private ValueEventListener dealerToChooseTrumpListener;
    private int myTurn;
    private String playerKey;
    private HashMap<String, String> connectedPlayersDict = new HashMap<>();

    public LobbyActivity lobbyActivity = null;
    public MainActivity mainActivity = null;

    public String getSessionId() {
        return sessionId;
    }

    public boolean allowedToPlay(Card card, Hand hand) {
        if(!hasTurn()) return false;
        if(trump == "wizard") return false; // In this case we assume that the dealer is still choosing a trump
        if(lead == null) return true;
        if(card.getType().equals(lead)) return true;
        if(!hand.hasType(lead)) return true;
        if(card.getType().equals("wizard") || card.getType().equals("jester")) return true;
        return false;
    }

    public boolean playCard(Card card, Hand hand) {
        if(!allowedToPlay(card, hand)) return false;

        hand.remove(card);
        updateLeadAndWinning(card);

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
                playerList.addPlayerToList(snapshot.getValue().toString());
                if(snapshot.getKey().compareTo(playerKey) == 0)
                    myTurn = 0;
                else if (snapshot.getKey().compareTo(playerKey) < 0)
                    myTurn++;
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                throw new RuntimeException("Unexpected database event, player in list changed");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                removePlayer(snapshot.getKey());
                playerList.removePlayerFromList(snapshot.getValue().toString());
                if (snapshot.getKey().compareTo(playerKey) < 0)
                    myTurn--;
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
                updateLeadAndWinning(currentSession.lastCardPlayed);
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
                throw new IllegalStateException("startGameListener cancelled");
            }
        });
        handListener = sessionReference.child("hands").child(playerKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("dealcards", "onDataChange fired!!!");
                setCurrentHand((Hand)snapshot.getValue(Hand.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new IllegalStateException("handListener cancelled");
            }
        });
        trumpListener = sessionReference.child("trump").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    Card trumpCard = snapshot.getValue(Card.class);
                    trump = trumpCard.getType();
                    mainActivity.setTrump(trumpCard);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new IllegalStateException("trumpListener cancelled");
            }
        });
        dealerToChooseTrumpListener = sessionReference.child("dealerToChooseTrump").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null)
                    if(snapshot.getValue(int.class) == myTurn) {
                        Log.d("wizardtrump", "myturn is: "+myTurn);
                        mainActivity.askToChooseTrump();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new IllegalStateException("dealerToChooseTrumpListener cancelled");
            }
        });
    }

    private void setCurrentHand(Hand hand) {
        currentHand = hand;
        if(currentHand != null)
            mainActivity.addCards(currentHand.getCardsInHand());
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

    private void updateLeadAndWinning(Card card) {
        //if this is first card played
        if(currentWinning == null) {
            currentWinning = card;
            lead = currentWinning.getType();
        }
        //if the winning card is a jester and this one is not (first jester always wins)
        else if(currentWinning.getType().equals("jester") && !card.getType().equals("jester")) {
            currentWinning = card;
            lead = currentWinning.getType();
        }
        //if the winning card is a wizard, that can't be beat (first wizard always wins)
        else if(currentWinning.getType().equals("wizard"))
        { /*nothing*/ }
        //if the wining is not a wizard, but a wizard was played
        else if(card.getType().equals("wizard"))
            currentWinning = card;
            //if trump is winning and no trump was played
        else if(currentWinning.getType().equals(lead) && !card.getType().equals(lead))
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

    public void dealCards() {
        if(!isHost()) {
            throw new IllegalStateException("Only host can deal cards!");
        }

        ++roundNumber;
        if (60 / roundNumber < getConnectedPlayerCount()) {
            // TODO end game
            return;
        }
        Deck deck = new Deck();
        deck.shuffle();
        List<Hand> hands = deck.getHands(getConnectedPlayerCount(), roundNumber);
        int handNum = 0;
        for(String playerKey : connectedPlayersDict.keySet()) {
            sessionReference.child("hands").child(playerKey).setValue(hands.get(handNum++));
        }
        Card trump = deck.getNext();
        setTrump(trump);
        if(trump.getType() == "wizard")
            // this just updates 'dealerToChoseTrump' to current dealers turn number which every child is
            // listening for, compares with own turn number and then sets trump from its end when equal
            // the formula for determining the dealer is:
            //      roundnumber - 1 to get back to 0 indexed (turns are 0 indexed, rounds 1 indexed)
            //      then -1 because dealer is the player before the one with the first turn
            //      lastly modulus with playercount
            sessionReference.child("dealerToChooseTrump").setValue((getConnectedPlayerCount() + roundNumber - 2) % getConnectedPlayerCount());
    }

    public void setTrump(Card trump) {
        // Trump null means that this is the last round, so no trump is played, which is equal to a jester
        if(trump == null)
            trump = Deck.getJester();
        sessionReference.child("trump").setValue(trump);
    }

    public void addPlayer(String key, String playerName) {
        connectedPlayersDict.put(key, playerName);
        currentSession.playerCount++;
    }
    public void removePlayer(String key) {
        connectedPlayersDict.remove(key);
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
        if(handListener != null) {
            sessionReference.child("cards").child(playerKey).removeEventListener(handListener);
            handListener = null;
        }
    }
}
