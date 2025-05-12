package com.example.lakastextilwebshop;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel {
    private static CartViewModel instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartViewModel() {}

    public static CartViewModel getInstance() {
        if (instance == null) {
            instance = new CartViewModel();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void addToCart(Product product, int quantity) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getProduct().getId() == product.getId()) {
                cartItems.set(i, item.copy(item.getQuantity() + quantity));
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public void clearCart() {
        cartItems.clear();
    }
}