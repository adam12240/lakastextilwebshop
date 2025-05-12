package com.example.lakastextilwebshop;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView categoryRecycler, featuredRecycler;
    private ProductAdapter featuredAdapter;
    private List<Product> featuredProducts = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        categoryRecycler = findViewById(R.id.category_list);
        featuredRecycler = findViewById(R.id.featured_list);

        List<String> categories = Arrays.asList("Függönyök", "Ágyneműk", "Törölközők", "Asztalneműk", "Díszpárnák");
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecycler.setAdapter(categoryAdapter);

        featuredAdapter = new ProductAdapter(featuredProducts, product -> {
            // TODO: Navigate to ProductDetailsScreen or show product details
            // Example: show a Toast or open a fragment
        });
        featuredRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredRecycler.setAdapter(featuredAdapter);

        TextView featuredTitle = findViewById(R.id.featured_title);
        featuredTitle.setText("Kiemelt termékek");

        loadFeaturedProducts();
    }

    private void loadFeaturedProducts() {
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(result -> {
                    featuredProducts.clear();
                    for (QueryDocumentSnapshot doc : result) {
                        Long idLong = doc.getLong("id");
                        String name = doc.getString("name");
                        Double price = doc.getDouble("price");
                        String desc = doc.getString("description");
                        if (idLong != null && name != null && price != null) {
                            featuredProducts.add(new Product(idLong.intValue(), name, price, desc != null ? desc : ""));
                        }
                    }
                    // Show only first 3 as featured
                    if (featuredProducts.size() > 3) {
                        featuredProducts = featuredProducts.subList(0, 3);
                    }
                    featuredAdapter.notifyDataSetChanged();
                });
    }
}