package com.renepauls.kingsandfools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Hand {
    private List<Card> cardsInHand = new ArrayList<>();

    public List<Card> getCardsInHand() {
        return cardsInHand;
    }

    public void setCardsInHand(List<Card> cardsInHand) {
        this.cardsInHand = cardsInHand;
    }

    public Hand() {}
    public Hand(Collection<Card> cards) {
        cardsInHand = new ArrayList<>(cards);
    }

    public boolean isempty() {
        return cardsInHand.isEmpty();
    }

    public boolean remove(Card card) {
        return cardsInHand.remove(card);
    }
    public boolean remove(String type, int value) {
        Card card = new Card(type, value, null, -1);
        return  remove(card);
    }

    public boolean add(Card card) {
        return cardsInHand.add(card);
    }

    public boolean hasType(String type) {
        for(Card card : cardsInHand) if (card.getType().equals(type)) return true;
        return false;
    }
}
