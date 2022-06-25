package com.nghiem.rilleyShipper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.nghiem.rilleyShipper.R;
import com.nghiem.rilleyShipper.common.Common;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.nghiem.rilleyShipper.databinding.ActivityHomeBinding;
import com.google.firebase.iid.FirebaseInstanceId;

import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        updateToken();

        checkStartTrip();

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void checkStartTrip() {
        Paper.init(this);
        if (!TextUtils.isEmpty(Paper.book().read(Common.TRIP_START)))
            startActivity(new Intent(this,ShippingActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(instanceIdResult -> {
                    Common.updateNewToken(HomeActivity.this, instanceIdResult.getToken(),
                            false,
                            true);

                    Log.d("MYTOKEN", instanceIdResult.getToken());
                });


    }
}