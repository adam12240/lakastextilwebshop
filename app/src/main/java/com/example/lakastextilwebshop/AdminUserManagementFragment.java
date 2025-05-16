package com.example.lakastextilwebshop;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminUserManagementFragment extends Fragment {
    private static final String TAG = "AdminUserManagement";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListView usersListView;
    private EditText nameEdit, priceEdit, descEdit;
    private Button uploadButton;
    private TextView messageText;
    private ArrayAdapter<String> usersAdapter;
    private List<AppUser> users = new ArrayList<>();
    private String currentUserUid;

    private Spinner updateProductSpinner;
    private Button deleteProductButton;
    private EditText updateProductNameEdit, updateProductPriceEdit, updateProductDescEdit;
    private Button updateProductButton;
    private TextView updateProductMessageText;
    private boolean isCurrentUserAdmin = false;

    private ArrayAdapter<String> productSpinnerAdapter;
    private List<Product> productListForSpinner = new ArrayList<>();
    private Product selectedProductForUpdate = null;

    public static class Product {
        String firestoreDocId;
        long customId;
        String name;
        double price;
        String description;

        public Product(String firestoreDocId, long customId, String name, double price, String description) {
            this.firestoreDocId = firestoreDocId;
            this.customId = customId;
            this.name = name;
            this.price = price;
            this.description = description;
        }

        @NonNull
        @Override
        public String toString() {
            return name + " (Azonosító: " + customId + ")";
        }
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user_management, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        usersListView = view.findViewById(R.id.users_list);
        nameEdit = view.findViewById(R.id.product_name);
        priceEdit = view.findViewById(R.id.product_price);
        descEdit = view.findViewById(R.id.product_desc);
        uploadButton = view.findViewById(R.id.upload_button);
        deleteProductButton = view.findViewById(R.id.delete_product_button);

        updateProductSpinner = view.findViewById(R.id.update_product_spinner);
        updateProductNameEdit = view.findViewById(R.id.update_product_name);
        updateProductPriceEdit = view.findViewById(R.id.update_product_price);
        updateProductDescEdit = view.findViewById(R.id.update_product_desc);
        updateProductButton = view.findViewById(R.id.update_product_button);
        updateProductMessageText = view.findViewById(R.id.update_product_message_text);
        messageText = view.findViewById(R.id.message_text);


        view.findViewById(R.id.update_product_button);
        view.findViewById(R.id.delete_product_button);
        view.findViewById(R.id.update_product_message_text);
        deleteProductButton.setOnClickListener(v -> confirmDeleteProduct());


        if (auth.getCurrentUser() == null) {
            messageText.setText("Nem vagy bejelentkezve.");
            return view;
        }
        currentUserUid = auth.getCurrentUser().getUid();

        checkAdminAndLoad();


        uploadButton.setOnClickListener(v -> uploadProduct());
        updateProductButton.setOnClickListener(v -> updateProduct());

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void checkAdminAndLoad() {
        db.collection("users").document(currentUserUid)
                .get()
                .addOnSuccessListener(doc -> {
                    isCurrentUserAdmin = doc.getBoolean("isAdmin") != null && Boolean.TRUE.equals(doc.getBoolean("isAdmin"));
                    if (isCurrentUserAdmin) {
                        loadUsers();
                        loadProductsForSpinner();
                    } else {
                        messageText.setText("Ehhez nem férhetsz hozzá.");
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    private void loadProductsForSpinner() {

        db.collection("products")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null) return;
                    productListForSpinner.clear();
                    List<String> productDisplayNames = new ArrayList<>();
                    productDisplayNames.add("Válassz terméket...");

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Long customIdLong = document.getLong("id");
                        String name = document.getString("name");
                        Double priceDouble = document.getDouble("price");
                        String description = document.getString("description");

                        if (customIdLong != null && name != null && priceDouble != null) {
                            Product product = new Product(
                                    document.getId(),
                                    customIdLong,
                                    name,
                                    priceDouble,
                                    description != null ? description : ""
                            );
                            productListForSpinner.add(product);
                            productDisplayNames.add(product.toString());
                        } else {
                            Log.w(TAG, "Hiba" + document.getId());
                        }
                    }

                    productSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, productDisplayNames);
                    productSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    updateProductSpinner.setAdapter(productSpinnerAdapter);
                    updateProductSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (getContext() == null) return;
                            if (position == 0) {
                                selectedProductForUpdate = null;
                                updateProductNameEdit.setText("");
                                updateProductPriceEdit.setText("");
                                updateProductDescEdit.setText("");
                                updateProductMessageText.setText("");
                            } else {
                                selectedProductForUpdate = productListForSpinner.get(position - 1);
                                updateProductNameEdit.setText(selectedProductForUpdate.name);
                                updateProductPriceEdit.setText(String.valueOf(selectedProductForUpdate.price));
                                updateProductDescEdit.setText(selectedProductForUpdate.description);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedProductForUpdate = null;
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Hiba a termékek betöltésekor a legördülő menühöz.", e);
                    if (getContext() != null) {
                        updateProductMessageText.setText("Hiba a termékek betöltésekor a legördülő menühöz.");
                        updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    }
                });
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(result -> {
            users.clear();
            List<String> userStrings = new ArrayList<>();
            for (DocumentSnapshot doc : result) {
                String uid = doc.getId();
                String email = doc.getString("email");
                boolean isAdmin = doc.getBoolean("isAdmin") != null && Boolean.TRUE.equals(doc.getBoolean("isAdmin"));
                if (email != null) {
                    users.add(new AppUser(uid, email, isAdmin));
                    userStrings.add(email + (isAdmin ? " (admin)" : ""));
                }
            }
            usersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, userStrings);
            usersListView.setAdapter(usersAdapter);

            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                AppUser user = users.get(position);
                if (user.uid.equals(currentUserUid)) {
                    Toast.makeText(requireContext(), "Saját magadat nem tudod lefokozni!", Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleAdmin(user);
            });
        });
    }

    @SuppressLint("SetTextI18n")
    private void toggleAdmin(AppUser user) {
        DocumentReference userRef = db.collection("users").document(user.uid);
        db.runTransaction(transaction -> {
            transaction.update(userRef, "isAdmin", !user.isAdmin);
            return null;
        }).addOnSuccessListener(aVoid -> {
            messageText.setText("Módosítottam: " + user.email);
            loadUsers();
        }).addOnFailureListener(e -> messageText.setText("Módosítás nem sikerült: " + e.getMessage()));
    }

    @SuppressLint("SetTextI18n")
    private void uploadProduct() {
        String name = nameEdit.getText().toString().trim();
        String priceStr = priceEdit.getText().toString().trim();
        String desc = descEdit.getText().toString().trim();
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            messageText.setText("Név és érvényes ár szükséges.");
            return;
        }
        if (name.isEmpty()) {
            messageText.setText("Név és érvényes ár szükséges.");
            return;
        }
        db.collection("products").get().addOnSuccessListener(result -> {
            int maxId = 0;
            for (DocumentSnapshot doc : result) {
                Long id = doc.getLong("id");
                if (id != null && id > maxId) maxId = id.intValue();
            }
            Map<String, Object> newProduct = new HashMap<>();
            newProduct.put("id", maxId + 1);
            newProduct.put("name", name);
            newProduct.put("price", price);
            newProduct.put("description", desc);
            db.collection("products").add(newProduct).addOnSuccessListener(ref -> {
                messageText.setText("Termék feltöltve");
                nameEdit.setText("");
                priceEdit.setText("");
                descEdit.setText("");
            }).addOnFailureListener(e -> messageText.setText("Nem sikerült feltölteni a terméket."));
            loadProductsForSpinner();
        });
    }


    @SuppressLint("SetTextI18n")
    private void updateProduct() {
        if (getContext() == null) return;

        if (selectedProductForUpdate == null || updateProductSpinner.getSelectedItemPosition() == 0) {
            updateProductMessageText.setText("Kérlek, válassz egy terméket a módosításhoz a legördülő menüből.");
            updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        String newName = updateProductNameEdit.getText().toString().trim();
        String newPriceStr = updateProductPriceEdit.getText().toString().trim();
        String newDesc = updateProductDescEdit.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        if (!TextUtils.isEmpty(newName) && !newName.equals(selectedProductForUpdate.name)) {
            updates.put("name", newName);
        }

        if (!TextUtils.isEmpty(newPriceStr)) {
            try {
                double newPrice = Double.parseDouble(newPriceStr);
                if (newPrice <= 0) {
                    updateProductMessageText.setText("Az új árnak pozitív számnak kell lennie.");
                    updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    return;
                }
                if (newPrice != selectedProductForUpdate.price) {
                    updates.put("price", newPrice);
                }
            } catch (NumberFormatException e) {
                updateProductMessageText.setText("Érvénytelen új ár formátum. Kérlek, számot adj meg.");
                updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                return;
            }
        }

        if (!newDesc.equals(selectedProductForUpdate.description)) {
            updates.put("description", newDesc);
        }

        if (updates.isEmpty()) {
            updateProductMessageText.setText("Nem történt változás a módosítandó adatokban.");
            updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light));
            return;
        }

        DocumentReference productDocRef = db.collection("products").document(selectedProductForUpdate.firestoreDocId);

        productDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        updateProductMessageText.setText("A '" + selectedProductForUpdate.name + "' termék sikeresen módosítva.");
                        updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                        updateProductNameEdit.setText("");
                        updateProductPriceEdit.setText("");
                        updateProductDescEdit.setText("");
                        loadProductsForSpinner();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating product: " + selectedProductForUpdate.name, e);
                    if (getContext() != null) {
                        updateProductMessageText.setText("Hiba a termék módosításakor: " + e.getMessage());
                        updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void confirmDeleteProduct() {
        if (getContext() == null) return;

        if (selectedProductForUpdate == null || updateProductSpinner.getSelectedItemPosition() == 0) {
            updateProductMessageText.setText("Kérlek, válassz egy terméket a törléshez a legördülő menüből.");
            updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Termék törlése")
                .setMessage("Biztosan törölni szeretnéd a " + selectedProductForUpdate.name + " nevű terméket?\nEz a művelet nem vonható vissza.")
                .setPositiveButton("Törlés", (dialog, which) -> deleteProduct())
                .setNegativeButton("Mégse", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @SuppressLint("SetTextI18n")
    private void deleteProduct() {
        if (getContext() == null || selectedProductForUpdate == null) {
            if (getContext() != null) {
                updateProductMessageText.setText("Hiba: Nem található a kiválasztott termék a törléshez.");
                updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
            return;
        }

        DocumentReference productDocRef = db.collection("products").document(selectedProductForUpdate.firestoreDocId);

        productDocRef.delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        updateProductMessageText.setText("A '" + selectedProductForUpdate.name + "' termék sikeresen törölve.");
                        updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));

                        updateProductNameEdit.setText("");
                        updateProductPriceEdit.setText("");
                        updateProductDescEdit.setText("");
                        selectedProductForUpdate = null;

                        loadProductsForSpinner();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Hiba a termék törlésekor" + selectedProductForUpdate.name, e);
                    if (getContext() != null) {
                        updateProductMessageText.setText("Hiba a termék törlésekor: " + e.getMessage());
                        updateProductMessageText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                    }
                });
    }

    public static class AppUser {
        public String uid, email;
        public boolean isAdmin;
        public AppUser(String uid, String email, boolean isAdmin) {
            this.uid = uid;
            this.email = email;
            this.isAdmin = isAdmin;
        }
    }
}