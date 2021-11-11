package com.renepauls.kingsandfools;

import androidx.annotation.Nullable;

public class Card {
    private String type;
    private int value;
    private String resourceName;
    private int resId;

    public Card() {}
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
    public void setValue(int value) {
        this.value = value;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public void setResId(int resId) {
        this.resId = resId;
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
