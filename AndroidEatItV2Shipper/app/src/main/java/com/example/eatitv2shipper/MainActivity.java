package com.example.eatitv2shipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import com.example.eatitv2shipper.common.Common;
import com.example.eatitv2shipper.model.ShipperUserModel;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        //Delete data offline
//        Paper.init(this);
//        Paper.book().delete(Common.TRIP_START);
//        Paper.book().delete(Common.SHIPPING_ORDER_DATA);
    }
    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),new AuthUI.IdpConfig.EmailBuilder().build());

        serverRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {

            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if(user != null)
            {
                checkServerUserFromFirebase(user);
//                Paper.init(this);
//                String jsonEncode = Paper.book().read(Common.RESTAURANT_SAVE);
//                RestaurantModel restaurantModel = new Gson().fromJson(jsonEncode,
//                        new TypeToken<RestaurantModel>(){}.getType());
//                if(restaurantModel != null) // If already save, just go to
//                    checkServerUserFromFirebase(user,restaurantModel); // Copy from RestaurantListActivity
//                else
//                {
//                    startActivity(new Intent(MainActivity.this,RestaurantListActivity.class));
//                    finish();
//                }
            }
            else
            {
                phoneLogin();
            }
        };
    }

//    private void checkServerUserFromFirebase(FirebaseUser user, RestaurantModel restaurantModel) {
//        dialog.show();
//
//        serverRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
//                .child(restaurantModel.getUid())
//                .child(Common.SHIPPER_REF);
//        serverRef.child(user.getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists())
//                        {
//                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
//                            if(userModel.isActive())
//                            {
//                                goToHomeActivity(userModel,restaurantModel);
//                            }
//                            else
//                            {
//                                dialog.dismiss();
//                                Toast.makeText(MainActivity.this, "You must be allowed from Server app", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//    }

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();

//        serverRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
//                .child(restaurantModel.getUid())
//                .child(Common.SHIPPER_REF);
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
                            if(userModel.isActive())
                            {
                                goToHomeActivity(userModel);
                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "You must be allowed from Server app", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            //Ask for Registration in case User Record does not exist
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information \nAdmin will accept your account late");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        //Set
        edt_phone.setText(user.getPhoneNumber());


        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("REGISTER", (dialog, which) -> {
            if (TextUtils.isEmpty(edt_name.getText().toString())) {
                Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            ShipperUserModel shipperUserModel = new ShipperUserModel();
            shipperUserModel.setUid(user.getUid());
            shipperUserModel.setName(edt_name.getText().toString());
            shipperUserModel.setPhone(edt_phone.getText().toString());
            shipperUserModel.setActive(false); //Default false, We must activate user manual in Firebase


            serverRef.child(shipperUserModel.getUid())
                    .setValue(shipperUserModel)
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        Toast.makeText(MainActivity.this, "Congratulation! Register Success! Admin will check and active you soon", Toast.LENGTH_SHORT).show();
//                                goToHomeActivity(shipperUserModel);

                    });
        });

        builder.setView(itemView);

        //Show Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

//    private void goToHomeActivity(ShipperUserModel userModel,RestaurantModel restaurantModel) {
//        dialog.dismiss();
//        Common.currentRestaurant = restaurantModel; // Fix crash
//        Common.currentShipperUser = userModel; // Important, if you don't do this line, when you access Common.currentShipperUser, this is null
//        startActivity(new Intent(this,HomeActivity.class));
//        finish();
//    }

    private void goToHomeActivity(ShipperUserModel userModel) {
        dialog.dismiss();
        Common.currentShipperUser = userModel; // Important, if you don't do this line, when you access Common.currentShipperUser, this is null
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
                ,APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == APP_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
