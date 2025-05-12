package com.example.lakastextilwebshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private final List<Order> orders;

    public OrdersAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        holder.dateText.setText("Order: " + sdf.format(order.timestamp.toDate()));
        StringBuilder itemsText = new StringBuilder();
        for (OrderItem item : order.items) {
            itemsText.append("- ").append(item.name)
                    .append(" x").append(item.quantity)
                    .append(" (").append(String.format("%.2f", item.price * item.quantity)).append(" €)\n");
        }
        holder.itemsText.setText(itemsText.toString().trim());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, itemsText;
        OrderViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.order_date);
            itemsText = itemView.findViewById(R.id.order_items);
        }
    }
}