package com.example.lakastextilwebshop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersScreen extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView errorText;
    private OrdersAdapter adapter;
    private List<Order> orders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_screen, container, false);

        recyclerView = view.findViewById(R.id.orders_recycler);
        progressBar = view.findViewById(R.id.orders_progress);
        errorText = view.findViewById(R.id.orders_error);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrdersAdapter(orders);
        recyclerView.setAdapter(adapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            showError("Please log in to view your orders.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(result -> {
                    orders.clear();
                    for (QueryDocumentSnapshot doc : result) {
                        List<OrderItem> items = new ArrayList<>();
                        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) doc.get("items");
                        if (itemsList != null) {
                            for (Map<String, Object> item : itemsList) {
                                int productId = ((Long) item.get("productId")).intValue();
                                String name = (String) item.get("name");
                                double price = (Double) item.get("price");
                                int quantity = ((Long) item.get("quantity")).intValue();
                                items.add(new OrderItem(productId, name, price, quantity));
                            }
                        }
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        orders.add(new Order(doc.getId(), items, timestamp));
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    errorText.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                    if (orders.isEmpty()) errorText.setText("No previous orders.");
                })
                .addOnFailureListener(e -> {
                    showError("Error: " + e.getLocalizedMessage());
                });
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }
}