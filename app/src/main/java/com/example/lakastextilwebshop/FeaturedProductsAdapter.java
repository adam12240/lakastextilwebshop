package com.example.lakastextilwebshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeaturedProductsAdapter extends RecyclerView.Adapter<FeaturedProductsAdapter.FeaturedViewHolder> {
    private final List<Product> products;
    private final ProductsAdapter.OnProductClickListener listener;

    public FeaturedProductsAdapter(List<Product> products, ProductsAdapter.OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_product, parent, false);
        return new FeaturedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Product p = products.get(position);
        holder.nameText.setText(p.getName());
        holder.itemView.setOnClickListener(v -> listener.onProductClick(p));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        FeaturedViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.featured_product_name);
        }
    }
}