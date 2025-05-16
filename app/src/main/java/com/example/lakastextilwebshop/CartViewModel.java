// CartViewModel.java
package com.example.lakastextilwebshop;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartViewModel extends ViewModel {
    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public void addToCart(Product product, int quantity) {
        List<CartItem> current = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        for (int i = 0; i < current.size(); i++) {
            CartItem item = current.get(i);
            if (item.getProduct().getId() == product.getId()) {
                current.set(i, item.copy(item.getQuantity() + quantity));
                cartItems.setValue(current);
                return;
            }
        }
        current.add(new CartItem(product, quantity));
        cartItems.setValue(current);
    }

    public void removeFromCart(Product product) {
        List<CartItem> current = new ArrayList<>(Objects.requireNonNull(cartItems.getValue()));
        for (int i = 0; i < current.size(); i++) {
            CartItem item = current.get(i);
            if (item.getProduct().getId() == product.getId()) {
                current.remove(i);
                cartItems.setValue(current);
                return;
            }
        }
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
    }
}