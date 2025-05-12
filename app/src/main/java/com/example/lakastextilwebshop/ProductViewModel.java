package com.example.lakastextilwebshop;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProductViewModel extends ViewModel {
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(new ArrayList<>());

    public ProductViewModel() {
        fetchProducts();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void refreshProducts() {
        fetchProducts();
    }

    private void fetchProducts() {
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnSuccessListener(result -> {
                    List<Product> productList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : result) {
                        Long idLong = doc.getLong("id");
                        String name = doc.getString("name");
                        Double price = doc.getDouble("price");
                        String description = doc.getString("description");
                        if (idLong != null && name != null && price != null) {
                            productList.add(new Product(idLong.intValue(), name, price, description != null ? description : ""));
                        }
                    }
                    products.setValue(productList);
                });
    }

    public void updateProduct(Product product) {
        FirebaseFirestore.getInstance().collection("products")
                .whereEqualTo("id", product.getId())
                .get()
                .addOnSuccessListener(result -> {
                    if (!result.isEmpty()) {
                        result.getDocuments().get(0).getReference().update(
                                "name", product.getName(),
                                "price", product.getPrice(),
                                "description", product.getDescription()
                        );
                    }
                });
    }

    public void deleteProduct(int productId) {
        FirebaseFirestore.getInstance().collection("products")
                .whereEqualTo("id", productId)
                .get()
                .addOnSuccessListener(result -> {
                    if (!result.isEmpty()) {
                        result.getDocuments().get(0).getReference().delete();
                    }
                });
    }
}