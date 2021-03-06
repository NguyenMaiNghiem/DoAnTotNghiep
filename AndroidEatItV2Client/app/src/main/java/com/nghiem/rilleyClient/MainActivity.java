package com.nghiem.rilleyClient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Model.UserModel;
import com.nghiem.rilleyClient.Remote.ICloudFunctions;
import com.nghiem.rilleyClient.Remote.RetroFitCloudClient;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171; //Random Number
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ICloudFunctions cloudFunctions;
    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    @Override
    protected void onStart() {
        //This is called after onCreate
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        //This is called when HomeActivity will Start
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start of Application
        init();
    }

    private void init() {

        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build()   //Phone Authentication (Sign In Method)
                ,new AuthUI.IdpConfig.EmailBuilder().build());                  //Email Authentication (Sign In Method)

        userRef = FirebaseDatabase.getInstance(Common.URL).getReference(Common.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        cloudFunctions = RetroFitCloudClient.getInstance().create(ICloudFunctions.class);
        listener = firebaseAuth -> {

            Dexter.withActivity(this)
                    .withPermissions(
                            Arrays.asList(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA)
                    )
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted())
                            {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    //Firebase Authentication Successful
                                    checkUserFromFirebase(user);
                                } else {
                                    phoneLogin();
                                }
                            }
                            else
                                Toast.makeText(MainActivity.this, "B???n c???n ph???i ch???p nh???n to??n b??? quy???n y??u c???u ????? s??? d???ng ???ng d???ng", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                        }
                    })
                    .check();


        };
    }

    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            //Record found in Firebase Database "Users"
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);

                        } else {
                            //Ask for Registration in case User Record does not exist
                            showRegisterDialog(user);
                        }

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //In case any Error,
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_address = (EditText) itemView.findViewById(R.id.edt_address);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                edt_address.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Set Data
        if (user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber()))
        {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }else
            edt_phone.setText(user.getPhoneNumber());

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("REGISTER", (dialog, which) -> {

//            if (placeSelected != null)
//            {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserModel userModel = new UserModel();
                userModel.setUid(user.getUid());
                userModel.setName(edt_name.getText().toString());
                userModel.setAddress(edt_address.getText().toString());
                userModel.setPhone(edt_phone.getText().toString());
//                userModel.setLat(placeSelected.getLatLng().latitude);
//                userModel.setLng(placeSelected.getLatLng().longitude);


                userRef.child(user.getUid()).setValue(userModel)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                //Successful Update of User Detail to Firebase Database
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Congratulation! Register Success", Toast.LENGTH_SHORT).show();
                                goToHomeActivity(userModel);

//                                    FirebaseAuth.getInstance().getCurrentUser()
//                                            .getIdToken(true)
//                                            .addOnFailureListener(e -> {
//                                                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show(); })
//                                            .addOnCompleteListener(task1 -> {
//                                                Common.authorizeKey = token;
//                                                Map<String,String> header;
//                                                header.put("AuthorizeKey");
//                                                compositeDisposable.add()
//                                            })

                            }
                        });
//            }
//            else
//            {
//                Toast.makeText(this, "Please select Address!", Toast.LENGTH_SHORT).show();
//                return;
//            }
        });

        builder.setView(itemView);

        //Show Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        dialog.show();
    }

    private void goToHomeActivity(UserModel userModel) {

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel; //Important, You always needs to assign this value before going to Home.
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();

                })
                .addOnCompleteListener(task -> {
                    Common.currentUser = userModel; //Important, You always needs to assign this value before going to Home.
                    Common.updateNewToken(MainActivity.this, task.getResult().getToken());
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false));
                    startActivity(intent);
                    finish();
                });

//        FirebaseInstallations.getInstance()
//                .getId()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        Common.currentUser = userModel; //Important, You always needs to assign this value before going to Home.
//                        Common.updateNewToken(MainActivity.this, task.getResult());
//                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
//                        finish();
//                    }
//                }).addOnFailureListener(e -> {
//                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    Common.currentUser = userModel; //Important, You always needs to assign this value before going to Home.
//                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
//                    finish();
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //When Sign-In Flow Complete, Result will be received in onActivityResult
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                //Successfully Signed In
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                //Sign In Failed: Response is null, User Cancelled, can be checked response.getError().getErrorCode();
                Toast.makeText(this, "Failed to Sign In due to " + response.getError().getErrorCode(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void phoneLogin() {

        // Create and launch sign-in intent
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setLogo(R.drawable.anhdaidien)
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .build()
                ,APP_REQUEST_CODE);

    }
}
