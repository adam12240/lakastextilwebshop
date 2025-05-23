package com.example.lakastextilwebshop;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class CartScreen extends Fragment {
    private ListView cartListView;
    private TextView totalTextView, emptyTextView;
    private CartViewModel cartViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_screen, container, false);

        cartListView = view.findViewById(R.id.cart_list);
        totalTextView = view.findViewById(R.id.total_text);
        emptyTextView = view.findViewById(R.id.empty_text);
        Button checkoutButton = view.findViewById(R.id.checkout_button);
        Button ordersButton = view.findViewById(R.id.orders_button);

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), this::updateCartUI);

        checkoutButton.setOnClickListener(v -> {
            List<CartItem> cartItems = cartViewModel.getCartItems().getValue();
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(getContext(), "A kosarad üres.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileScreen())
                        .addToBackStack(null)
                        .commit();
            } else {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CheckoutScreen())
                        .addToBackStack(null)
                        .commit();
            }
        });

        ordersButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new OrdersScreen())
                .addToBackStack(null)
                .commit());

        return view;
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateCartUI(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            cartListView.setVisibility(View.GONE);
            totalTextView.setText("Összesen: 0.00 €");
        } else {
            emptyTextView.setVisibility(View.GONE);
            cartListView.setVisibility(View.VISIBLE);
            double total = 0.0;
            for (CartItem item : cartItems) {
                if (item != null && item.getProduct() != null) {
                    total += item.getProduct().getPrice() * item.getQuantity();
                }
            }
            CartItemAdapter adapter = new CartItemAdapter(requireContext(), cartItems, item -> cartViewModel.removeFromCart(item.getProduct()));
            cartListView.setAdapter(adapter);
            totalTextView.setText("Összesen: " + String.format("%.2f", total) + " €");
        }
    }
}