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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProductsScreen extends Fragment {

    private RecyclerView featuredRecycler, productsRecycler;
    private ProgressBar progressBar;
    private TextView errorText;
    private ProductsAdapter productsAdapter;
    private FeaturedProductsAdapter featuredAdapter;
    private List<Product> products = new ArrayList<>();
    private List<Product> featuredProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_screen, container, false);

        featuredRecycler = view.findViewById(R.id.featured_recycler);
        productsRecycler = view.findViewById(R.id.products_recycler);
        progressBar = view.findViewById(R.id.products_progress);
        errorText = view.findViewById(R.id.products_error);

        featuredRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        productsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        featuredAdapter = new FeaturedProductsAdapter(featuredProducts, this::onProductClick);
        productsAdapter = new ProductsAdapter(products, this::onProductClick);

        featuredRecycler.setAdapter(featuredAdapter);
        productsRecycler.setAdapter(productsAdapter);

        loadProducts();

        return view;
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        errorText.setVisibility(View.GONE);

        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(result -> {
                    products.clear();
                    for (QueryDocumentSnapshot doc : result) {
                        Integer id = doc.getLong("id") != null ? doc.getLong("id").intValue() : null;
                        String name = doc.getString("name");
                        Double price = doc.getDouble("price");
                        String desc = doc.getString("description");
                        if (id != null && name != null && price != null) {
                            products.add(new Product(id, name, price, desc != null ? desc : ""));
                        }
                    }
                    featuredProducts.clear();
                    featuredProducts.addAll(products.subList(0, Math.min(3, products.size())));
                    productsAdapter.notifyDataSetChanged();
                    featuredAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    errorText.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                    if (products.isEmpty()) errorText.setText("No products found.");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("Error: " + e.getLocalizedMessage());
                });
    }

    private void onProductClick(Product product) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, ProductDetailsScreen.newInstance(product.getId()))
                .addToBackStack(null)
                .commit();
    }
}