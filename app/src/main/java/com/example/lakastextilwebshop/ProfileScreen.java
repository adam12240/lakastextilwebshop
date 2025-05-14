package com.example.lakastextilwebshop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileScreen extends Fragment {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private TextView nameText, emailText, adminText, errorText;
    private Button signInBtn, signOutBtn;
    private Button adminPanelBtn; // Add this field
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_screen, container, false);

        nameText = view.findViewById(R.id.profile_name);
        emailText = view.findViewById(R.id.profile_email);
        adminText = view.findViewById(R.id.profile_admin);
        errorText = view.findViewById(R.id.profile_error);
        signInBtn = view.findViewById(R.id.profile_sign_in);
        signOutBtn = view.findViewById(R.id.profile_sign_out);
        progressBar = view.findViewById(R.id.profile_progress);
        adminPanelBtn = view.findViewById(R.id.profile_admin_panel); // Initialize

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("175702750781-5qmoavcapf4srkdbt85738v3lual40m7.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        signInBtn.setOnClickListener(v -> signIn());
        signOutBtn.setOnClickListener(v -> signOut());
        adminPanelBtn.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, new AdminUserManagementFragment())
                    .commit();
        });

        updateUI(auth.getCurrentUser());

        return view;
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        auth.signOut();
        googleSignInClient.signOut();
        Fragment adminFragment = getChildFragmentManager().findFragmentById(R.id.admin_fragment_container);
        if (adminFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(adminFragment)
                    .commit();
        }
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);
        if (user == null) {
            nameText.setText("");
            emailText.setText("");
            adminText.setVisibility(View.GONE);
            signInBtn.setVisibility(View.VISIBLE);
            signOutBtn.setVisibility(View.GONE);
            adminPanelBtn.setVisibility(View.GONE); // Hide for non-logged-in users
            errorText.setText("");
        } else {
            nameText.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
            emailText.setText(user.getEmail() != null ? user.getEmail() : "");
            signInBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.VISIBLE);
            checkAdmin(user);
        }
    }

    private void checkAdmin(FirebaseUser user) {
        DocumentReference userDoc = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        userDoc.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                userDoc.set(new User(user.getEmail(), false)).addOnSuccessListener(aVoid -> {
                    adminText.setVisibility(View.GONE);
                    adminPanelBtn.setVisibility(View.GONE);
                    errorText.setText("");
                }).addOnFailureListener(e -> {
                    adminText.setVisibility(View.GONE);
                    adminPanelBtn.setVisibility(View.GONE);
                    errorText.setText("Nem sikerült létrehozni a felhasználót.");
                });
            } else {
                Boolean isAdmin = doc.getBoolean("isAdmin");
                if (isAdmin == null) {
                    adminText.setVisibility(View.GONE);
                    adminPanelBtn.setVisibility(View.GONE);
                    errorText.setText("");
                } else if (isAdmin) {
                    adminText.setVisibility(View.VISIBLE);
                    adminPanelBtn.setVisibility(View.VISIBLE);
                    errorText.setText("Admin vagy.");
                } else {
                    adminText.setVisibility(View.GONE);
                    adminPanelBtn.setVisibility(View.GONE);
                    errorText.setText("");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        updateUI(auth.getCurrentUser());
                        errorText.setText("");
                    } else {
                        errorText.setText("Nem sikerült bejelentkezni.");
                    }
                });
            } catch (Exception e) {
                progressBar.setVisibility(View.GONE);
                errorText.setText("Nem sikerült bejelentkezni.");
            }
        }
    }

    // Helper class for Firestore user document
    public static class User {
        public String email;
        public boolean isAdmin;
        public User() {}
        public User(String email, boolean isAdmin) {
            this.email = email;
            this.isAdmin = isAdmin;
        }
    }
}