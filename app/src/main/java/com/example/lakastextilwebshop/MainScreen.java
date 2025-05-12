package com.example.lakastextilwebshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainScreen extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_screen, container, false);

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_navigation);

        // Set default fragment
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeScreen())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeScreen())
                        .commit();
                return true;
            } else if (id == R.id.nav_products) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProductsScreen())
                        .commit();
                return true;
            } else if (id == R.id.nav_cart) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new CartScreen())
                        .commit();
                return true;
            } else if (id == R.id.nav_profile) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileScreen())
                        .commit();
                return true;
            }
            return false;
        });

        return view;
    }
}