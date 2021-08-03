package com.renepauls.kingsandfools;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deck {
    private static final String[] sets = {"clubs", "diamonds", "hearts", "spades"};
    private static Map<String, Card> availableCards;
    private List<Card> shuffled;

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
    }

    public void shuffle() {
        if(shuffled == null) {
            shuffled = new ArrayList<>(availableCards.values());
        }
        Collections.shuffle(shuffled);
    }

    public Card get(int i) {
        if (shuffled == null) {
            shuffle();
        }
        return shuffled.get(i);
    }

    public static Card getCardFromResourceName(String resourceName) {
        return availableCards.get(resourceName);
    }
}
