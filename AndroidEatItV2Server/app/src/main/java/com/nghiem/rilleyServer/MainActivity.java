package com.nghiem.rilleyServer;

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

import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.Model.ServerUserModel;
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

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171; //Random Number
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build()); //Phone Authentication (Sign In Method)
        serverRef = FirebaseDatabase.getInstance(Common.URL).getReference(Common.SERVER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        listener = firebaseAuth -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                //Firebase Authentication Successful
                checkServerUserFromFirebase(user);
            } else {
                phoneLogin();
            }
        };
    }

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
        super.onStop();
    }

    private void phoneLogin() {

        // Create and launch sign-in intent
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build()
                        , APP_REQUEST_CODE);

    }

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

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            //Record found in Firebase Database "Users"
                            ServerUserModel userModel = dataSnapshot.getValue(ServerUserModel.class);
                            if(userModel.isActive())
                                goToHomeActivity(userModel);
                            else {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "You must be allowed from Admin to Access this App", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            //Ask for Registration in case User Record does not exist
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
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

            ServerUserModel serverUserModel = new ServerUserModel();
            serverUserModel.setUid(user.getUid());
            serverUserModel.setName(edt_name.getText().toString());
            serverUserModel.setPhone(edt_phone.getText().toString());
            serverUserModel.setActive(false); //Default false, We must activate user manual in Firebase


            serverRef.child(serverUserModel.getUid()).setValue(serverUserModel)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "Congratulation! Register Success! Admin will check and active you soon", Toast.LENGTH_SHORT).show();
//                                goToHomeActivity(serverUserModel);

                        }
                    });
        });

        builder.setView(itemView);

        //Show Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void goToHomeActivity(ServerUserModel serverUserModel) {
        dialog.dismiss();
        Common.currentServerUser = serverUserModel;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false));
        startActivity(intent);
        finish();
    }
}
