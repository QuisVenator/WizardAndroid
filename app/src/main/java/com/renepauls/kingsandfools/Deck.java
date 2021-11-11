package com.renepauls.kingsandfools;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deck {
    private static final String[] sets = {"clubs", "diamonds", "hearts", "spades"};
    public static Map<String, Card> availableCards;
    private List<Card> shuffled;
    private int index = 0;

    public static void initialize(Context context) {
        availableCards = new HashMap<>();
        for(String set : sets) {
            String resourceName = set+"_ace";
            int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            availableCards.put(resourceName, new Card(set, 14, resourceName, resId));
            for(int i = 2; i < 14; i++) {
                resourceName = set+"_"+i;
                resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                availableCards.put(set+"_"+i, new Card(set, i, resourceName, resId));
            }
        }

        // Add 4 wizard and 4 jester
        String resourceName =  "wizard";
        int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        for(int i = 0; i < 4; i++) {
            availableCards.put(resourceName+i, new Card("wizard", 0, resourceName, resId));
        }
        resourceName =  "jester";
        resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        for(int i = 0; i < 4; i++) {
            availableCards.put(resourceName+i, new Card("jester", 0, resourceName, resId));
        }
    }

    public void shuffle() {
        if(shuffled == null) {
            shuffled = new ArrayList<>(availableCards.values());
        }
        Collections.shuffle(shuffled);
    }

    private Card get(int i) {
        if (shuffled == null) {
            shuffle();
        }
        return shuffled.get(i);
    }
    public Card getNext() {
        if (index >= 60) {
            return null;
        }
        return get(index++);
    }

    public static Card getCardFromResourceName(String resourceName) {
        return availableCards.get(resourceName);
    }

    public List<Hand> getHands(int handNumber, int cardsPerHand) {
        if(handNumber * cardsPerHand + index > 60)
            throw new IndexOutOfBoundsException("Not enough cards in hand!");

        List<Hand> hands = new ArrayList<>();
        for(int i = 0; i < handNumber; i++) {
            Hand hand = new Hand();
            for(int j = 0; j < cardsPerHand; j++) {
                hand.add(getNext());
            }
            hands.add(hand);
        }
        return hands;
    }
}
