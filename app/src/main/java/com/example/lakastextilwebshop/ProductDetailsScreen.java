package com.example.lakastextilwebshop;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProductDetailsScreen extends Fragment {

    private static final String ARG_PRODUCT_ID = "productId";
    private int productId;

    private TextView nameText, priceText, descText;
    private Button addToCartBtn;
    private ProgressBar progressBar;
    private CartViewModel cartViewModel;

    public static ProductDetailsScreen newInstance(int productId) {
        ProductDetailsScreen fragment = new ProductDetailsScreen();
        Bundle args = new Bundle();
        args.putInt(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getInt(ARG_PRODUCT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);

        nameText = view.findViewById(R.id.product_name);
        priceText = view.findViewById(R.id.product_price);
        descText = view.findViewById(R.id.product_desc);
        addToCartBtn = view.findViewById(R.id.add_to_cart_btn);
        progressBar = view.findViewById(R.id.product_progress);

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        loadProduct();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void loadProduct() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("products")
                .whereEqualTo("id", productId)
                .get()
                .addOnSuccessListener(result -> {
                    Product product = null;
                    for (QueryDocumentSnapshot doc : result) {
                        String name = doc.getString("name");
                        Double priceObj = doc.getDouble("price");
                        double price = priceObj != null ? priceObj : 0.0;
                        String desc = doc.getString("description");
                        product = new Product(productId, name, price, desc != null ? desc : "");
                        break;
                    }
                    progressBar.setVisibility(View.GONE);
                    if (product != null) {
                        showProduct(product);
                    } else {
                        nameText.setText("Product not found.");
                        addToCartBtn.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    nameText.setText("Error loading product.");
                    addToCartBtn.setEnabled(false);
                });
    }

    @SuppressLint("DefaultLocale")
    private void showProduct(Product product) {
        nameText.setText(product.getName());
        priceText.setText(String.format("%.2f €", product.getPrice()));
        descText.setText(product.getDescription());

        addToCartBtn.setOnClickListener(v -> {
            cartViewModel.addToCart(product, 1);
            Toast.makeText(getContext(), "Added to cart: " + product.getName(), Toast.LENGTH_SHORT).show();
        });
    }
}