package com.example.lakastextilwebshop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeScreen extends Fragment {
    private RecyclerView categoryRecycler, featuredRecycler;
    private FeaturedProductsAdapter featuredAdapter;
    private List<Product> featuredProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        categoryRecycler = view.findViewById(R.id.category_list);
        featuredRecycler = view.findViewById(R.id.featured_list);

        List<String> categories = Arrays.asList("Függönyök", "Ágyneműk", "Törölközők", "Asztalneműk", "Díszpárnák");
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecycler.setAdapter(categoryAdapter);

        featuredAdapter = new FeaturedProductsAdapter(featuredProducts, product -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailsScreen.newInstance(product.getId()))
                    .addToBackStack(null)
                    .commit();
        });
        featuredRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredRecycler.setAdapter(featuredAdapter);

        TextView featuredTitle = view.findViewById(R.id.featured_title);
        featuredTitle.setText("Kiemelt termékek");

        loadFeaturedProducts();

        return view;
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
                    if (featuredProducts.size() > 3) {
                        featuredProducts = featuredProducts.subList(0, 3);
                    }
                    featuredAdapter.notifyDataSetChanged();
                });
    }
}