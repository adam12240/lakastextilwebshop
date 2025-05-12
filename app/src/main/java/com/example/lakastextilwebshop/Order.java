package com.example.lakastextilwebshop;

import com.google.firebase.Timestamp;
import java.util.List;

public class Order {
    public String id;
    public List<OrderItem> items;
    public Timestamp timestamp;

    public Order(String id, List<OrderItem> items, Timestamp timestamp) {
        this.id = id;
        this.items = items;
        this.timestamp = timestamp;
    }
}
