package com.example.lakastextilwebshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;

import java.util.List;

public class CartItemAdapter extends ArrayAdapter<CartItem> {
    public interface OnDeleteClickListener {
        void onDelete(CartItem item);
    }

    private final OnDeleteClickListener deleteClickListener;

    public CartItemAdapter(Context context, List<CartItem> items, OnDeleteClickListener listener) {
        super(context, 0, items);
        this.deleteClickListener = listener;
    }

    @NonNull
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CartItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_cart, parent, false);
        }
        TextView text = convertView.findViewById(R.id.cart_item_text);
        ImageButton deleteBtn = convertView.findViewById(R.id.delete_button);

        if (item != null && item.getProduct() != null) {
            text.setText(item.getProduct().getName() + " x" + item.getQuantity() + " - " +
                    String.format("%.2f", item.getProduct().getPrice() * item.getQuantity()) + " €");
        }

        deleteBtn.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(item);
        });

        return convertView;
    }
}