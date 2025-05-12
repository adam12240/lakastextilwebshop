package com.example.lakastextilwebshop;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class CheckoutScreen extends Fragment {
    private ListView cartListView;
    private TextView totalTextView, messageTextView;
    private Button cancelButton, checkoutButton;
    private ProgressBar progressBar;
    private CartViewModel cartViewModel;
    private FirebaseAuth auth;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_checkout, container, false);

        cartListView = view.findViewById(R.id.cart_list);
        totalTextView = view.findViewById(R.id.total_text);
        messageTextView = view.findViewById(R.id.message_text);
        cancelButton = view.findViewById(R.id.cancel_button);
        checkoutButton = view.findViewById(R.id.checkout_button);
        progressBar = view.findViewById(R.id.progress_bar);

        cartViewModel = CartViewModel.getInstance();
        auth = FirebaseAuth.getInstance();

        updateCartUI();

        cancelButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        checkoutButton.setOnClickListener(v -> {
            if (isLoading) return;
            if (auth.getCurrentUser() == null) {
                messageTextView.setText("Please sign in to complete purchase.");
                return;
            }
            List<CartItem> cartItems = cartViewModel != null ? cartViewModel.getCartItems() : null;
            if (cartItems == null || cartItems.isEmpty()) {
                messageTextView.setText("Cart is empty.");
                return;
            }
            for (CartItem item : cartItems) {
                if (item == null || item.getProduct() == null) {
                    messageTextView.setText("Invalid item in cart.");
                    return;
                }
            }

            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);

            List<Map<String, Object>> items = new ArrayList<>();
            for (CartItem item : cartItems) {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", item.getProduct().getId());
                map.put("name", item.getProduct().getName());
                map.put("price", item.getProduct().getPrice());
                map.put("quantity", item.getQuantity());
                items.add(map);
            }

            Map<String, Object> order = new HashMap<>();
            order.put("userId", auth.getCurrentUser().getUid());
            order.put("items", items);
            order.put("timestamp", Timestamp.now());

            FirebaseFirestore.getInstance()
                    .collection("orders")
                    .add(order)
                    .addOnSuccessListener(docRef -> {
                        cartViewModel.clearCart();
                        updateCartUI();
                        showSuccessNotification();
                        messageTextView.setText("Purchase successful!");
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        messageTextView.setText("Error: " + e.getLocalizedMessage());
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                    });
        });

        return view;
    }

    private void updateCartUI() {
        List<CartItem> cartItems = cartViewModel != null ? cartViewModel.getCartItems() : new ArrayList<>();
        List<String> itemStrings = new ArrayList<>();
        double total = 0.0;
        for (CartItem item : cartItems) {
            if (item == null || item.getProduct() == null) continue;
            itemStrings.add(item.getProduct().getName() + " x" + item.getQuantity() + " - " +
                    String.format("%.2f", item.getProduct().getPrice() * item.getQuantity()) + " €");
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        if (getContext() != null) {
            cartListView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, itemStrings));
        }
        totalTextView.setText("Total: " + String.format("%.2f", total) + " €");
        messageTextView.setText("");
    }

    private void showSuccessNotification() {
        Context context = getContext();
        if (context == null) return;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "order_channel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Orders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new Notification.Builder(context, channelId)
                .setContentTitle("Purchase Successful")
                .setContentText("Thank you for your order!")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        notificationManager.notify(1, notification);
    }
}