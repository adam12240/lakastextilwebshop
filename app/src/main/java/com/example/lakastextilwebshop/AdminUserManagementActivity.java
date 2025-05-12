package com.example.lakastextilwebshop;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class AdminUserManagementActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListView usersListView;
    private EditText nameEdit, priceEdit, descEdit;
    private Button uploadButton;
    private TextView messageText;
    private ArrayAdapter<String> usersAdapter;
    private List<AppUser> users = new ArrayList<>();
    private boolean isCurrentUserAdmin = false;
    private String currentUserUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersListView = findViewById(R.id.users_list);
        nameEdit = findViewById(R.id.product_name);
        priceEdit = findViewById(R.id.product_price);
        descEdit = findViewById(R.id.product_desc);
        uploadButton = findViewById(R.id.upload_button);
        messageText = findViewById(R.id.message_text);

        if (auth.getCurrentUser() == null) {
            messageText.setText("Not signed in.");
            return;
        }
        currentUserUid = auth.getCurrentUser().getUid();

        checkAdminAndLoad();

        uploadButton.setOnClickListener(v -> uploadProduct());
    }

    private void checkAdminAndLoad() {
        db.collection("users").document(currentUserUid)
                .get()
                .addOnSuccessListener(doc -> {
                    isCurrentUserAdmin = doc.getBoolean("isAdmin") != null && doc.getBoolean("isAdmin");
                    if (isCurrentUserAdmin) {
                        loadUsers();
                    } else {
                        messageText.setText("Access denied. Admins only.");
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
                boolean isAdmin = doc.getBoolean("isAdmin") != null && doc.getBoolean("isAdmin");
                if (email != null) {
                    users.add(new AppUser(uid, email, isAdmin));
                    userStrings.add(email + (isAdmin ? " (admin)" : ""));
                }
            }
            usersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userStrings);
            usersListView.setAdapter(usersAdapter);

            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                AppUser user = users.get(position);
                if (user.uid.equals(currentUserUid)) {
                    Toast.makeText(this, "Cannot change your own admin status.", Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleAdmin(user);
            });
        });
    }

    private void toggleAdmin(AppUser user) {
        DocumentReference userRef = db.collection("users").document(user.uid);
        db.runTransaction(transaction -> {
            transaction.update(userRef, "isAdmin", !user.isAdmin);
            return null;
        }).addOnSuccessListener(aVoid -> {
            messageText.setText("Updated " + user.email);
            loadUsers();
        }).addOnFailureListener(e -> messageText.setText("Update failed."));
    }

    private void uploadProduct() {
        String name = nameEdit.getText().toString().trim();
        String priceStr = priceEdit.getText().toString().trim();
        String desc = descEdit.getText().toString().trim();
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            messageText.setText("Name and valid price required.");
            return;
        }
        if (name.isEmpty()) {
            messageText.setText("Name and valid price required.");
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
                messageText.setText("Product uploaded.");
                nameEdit.setText("");
                priceEdit.setText("");
                descEdit.setText("");
            }).addOnFailureListener(e -> messageText.setText("Failed to upload product."));
        });
    }

    // Simple POJO for user
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