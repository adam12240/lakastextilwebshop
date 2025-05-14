package com.example.lakastextilwebshop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class AdminUserManagementFragment extends Fragment {
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
        messageText = view.findViewById(R.id.message_text);

        if (auth.getCurrentUser() == null) {
            messageText.setText("Nem vagy bejelentkezve");
            return view;
        }
        currentUserUid = auth.getCurrentUser().getUid();

        checkAdminAndLoad();

        uploadButton.setOnClickListener(v -> uploadProduct());

        return view;
    }

    private void checkAdminAndLoad() {
        db.collection("users").document(currentUserUid)
                .get()
                .addOnSuccessListener(doc -> {
                    isCurrentUserAdmin = doc.getBoolean("isAdmin") != null && doc.getBoolean("isAdmin");
                    if (isCurrentUserAdmin) {
                        loadUsers();
                    } else {
                        messageText.setText("Ehhez nem férhetsz hozzá.");
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