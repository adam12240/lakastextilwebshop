package com.example.lakastextilwebshop;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.List;

public class ProfileScreen extends Fragment {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private TextView nameText, emailText, adminText, errorText;

    private ActivityResultLauncher<Intent> signInLauncher;

    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private Button signInBtn, signOutBtn;
    private Button adminPanelBtn;
    private ProgressBar progressBar;
    private Button reminderBtn;

    private Button locationBtn;

    private TextView locationText;

    @SuppressLint("SetTextI18n")
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
        locationBtn = view.findViewById(R.id.profile_location_btn);
        reminderBtn = view.findViewById(R.id.profile_reminder_btn);
        reminderBtn.setOnClickListener(v -> setReminder());
        locationBtn.setOnClickListener(v -> requestLocation());
        locationText = view.findViewById(R.id.profile_location_text);
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("175702750781-5qmoavcapf4srkdbt85738v3lual40m7.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        signInBtn.setOnClickListener(v -> signIn());
        signOutBtn.setOnClickListener(v -> signOut());
        adminPanelBtn.setOnClickListener(v -> getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_fragment_container, new AdminUserManagementFragment())
                .commit());

        updateUI(auth.getCurrentUser());

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    progressBar.setVisibility(View.GONE);
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        try {
                            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                            auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    updateUI(auth.getCurrentUser());
                                    askNotificationPermission();
                                    errorText.setText("");
                                } else {
                                    errorText.setText("Nem sikerült bejelentkezni.");
                                }
                            });
                        } catch (Exception e) {
                            errorText.setText("Nem sikerült bejelentkezni.");
                        }
                    } else {
                        errorText.setText("Nem sikerült bejelentkezni.");
                    }
                }
        );

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                        getLocation();
                    } else {
                        errorText.setText("Helymeghatározási engedély megtagadva.");
                    }
                }
        );

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                }
        );

        return view;
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
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

    private void setReminder() {
    askNotificationPermission();
    Context context = requireContext();
    Intent intent = new Intent(context, ReminderReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    long triggerAtMillis = System.currentTimeMillis() + 60 * 1000; // 1 perc múlva
    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    
    Toast.makeText(context, "Emlékeztető beállítva 1 percre!", Toast.LENGTH_SHORT).show();
}


    @SuppressLint("SetTextI18n")
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

    private void askNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            getLocation();
        }
    }

    @SuppressLint("SetTextI18n")
    private void getLocation() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            errorText.setText("Nincs megadva a helymeghatározási engedély.");
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            String city = "";
            Geocoder geocoder = new Geocoder(requireContext());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    city = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                city = "Ismeretlen";
            }
            String formattedLocation = String.format("Jelenleg ebben a városban vagy: %s \nAz Ön városába is ingyenesen szállítunk.", city != null ? city : "Ismeretlen");
            errorText.setText("");
            locationText.setText(formattedLocation);
        } else {
            locationText.setText("");
            errorText.setText("Ismeretlen hely.");
        }
    }


    public static class User {
        public String email;
        public boolean isAdmin;
        public User(String email, boolean isAdmin) {
            this.email = email;
            this.isAdmin = isAdmin;
        }
    }
}