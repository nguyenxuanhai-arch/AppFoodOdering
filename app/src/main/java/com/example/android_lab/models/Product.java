package com.example.android_lab.models;

import java.io.Serializable;

public class Product implements CartItem, java.io.Serializable {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private String description;
    private boolean isPopular;
    private int quantity = 1;
    private String type;

    public Product() {}

    public Product(String id, String name, double price, String imageUrl,
                   boolean isPopular, String description, int quantity, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isPopular = isPopular;
        this.description = description;
        this.quantity = quantity;
        this.type = type;
    }

    public Product(String productId, String name, double price, boolean switchValue, String description, int quantityStr) {
        this.id = productId;
        this.name = name;
        this.price = price;
        this.isPopular = switchValue;
        this.description = description;
        this.quantity = quantityStr;
    }

    // Getter & Setter
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public boolean isPopular() { return isPopular; }

    public void setPopular(boolean popular) { isPopular = popular; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getType() { return type; }

    public void setType(String product) {
        this.type = product;
    }
}
