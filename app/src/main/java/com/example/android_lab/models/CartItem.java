package com.example.android_lab.models;

public interface CartItem {
    String getId();
    double getPrice();
    String getName();
    String getImageUrl();

    int getQuantity();

    void setQuantity(int newQty);
}
