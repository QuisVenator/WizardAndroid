package com.renepauls.kingsandfools;

import androidx.annotation.Nullable;

public class Card {
    private final String type;
    private final int value;
    private final String resourceName;
    private final int resId;

    public Card (String type, int value, String resourceName, int resId) {
        this.resourceName = resourceName;
        this.value = value;

        //TODO these might be null, this is stupid, may never be fixed though
        this.type = type;
        this.resId = resId;
    }

    public int getValue() {
        return value;
    }
    public String getType() {
        return type;
    }
    public String getResourceName() {
        return resourceName;
    }
    public int getResId() {
        return resId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == Card.class) {
            Card cardObj = (Card)obj;

            return cardObj.getType().equals(this.getType()) &&
                    cardObj.getValue() == this.getValue();
        } else return false;
    }
}
