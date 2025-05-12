package com.example.lakastextilwebshop;

public class OrderItem {
    public int productId;
    public String name;
    public double price;
    public int quantity;

    public OrderItem(int productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}