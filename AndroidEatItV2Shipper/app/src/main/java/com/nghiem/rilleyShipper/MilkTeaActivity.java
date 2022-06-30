package com.nghiem.rilleyShipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nghiem.rilleyShipper.adapter.MyMilkTeaAdapter;
import com.nghiem.rilleyShipper.callback.IMilkteaCallbackListener;
import com.nghiem.rilleyShipper.common.Common;
import com.nghiem.rilleyShipper.eventbus.MilkTeaSelectEvent;
import com.nghiem.rilleyShipper.model.MilkTeaModel;
import com.nghiem.rilleyShipper.model.ShipperUserModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MilkTeaActivity extends AppCompatActivity implements IMilkteaCallbackListener {

    RecyclerView recycler_milktea;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMilkTeaAdapter adapter;

    DatabaseReference serverRef;
    IMilkteaCallbackListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milk_tea);

        initViews();
        loadAllMilkTea();
    }

    private void loadAllMilkTea() {
        dialog.show();
        List<MilkTeaModel> milkTeaModels = new ArrayList<>();
        DatabaseReference milkteaRef = FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF);
        milkteaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot milkSnapShot : dataSnapshot.getChildren()) {
                        MilkTeaModel milkTeaModel = milkSnapShot.getValue(MilkTeaModel.class);
                        milkTeaModel.setUid(milkSnapShot.getKey());
                        milkTeaModels.add(milkTeaModel);
                    }
                    if (milkTeaModels.size() > 0)
                        listener.onMilkteaLoadSuccess(milkTeaModels);
                    else
                        listener.onMilkteaLoadFailed("Danh sách cửa hàng trống");
                } else
                    listener.onMilkteaLoadFailed("Danh sách cửa hàng không tồn tại");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onMilkteaLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void initViews() {
        recycler_milktea = findViewById(R.id.recycler_milktea);
        listener = this;
        
        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Vui lòng đợi ....").create();

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this,R.anim.layout_item_from_left);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recycler_milktea.setLayoutManager(linearLayoutManager);
        recycler_milktea.addItemDecoration(new DividerItemDecoration(this,linearLayoutManager.getOrientation()));
    }

    @Override
    public void onMilkteaLoadSuccess(List<MilkTeaModel> milkTeaModelList) {
        dialog.dismiss();
        adapter = new MyMilkTeaAdapter(this,milkTeaModelList);
        recycler_milktea.setAdapter(adapter);
        recycler_milktea.setLayoutAnimation(layoutAnimationController);
    }

    @Override
    public void onMilkteaLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //EventBus


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMilkteaSelectedEvent(MilkTeaSelectEvent event)
    {
        if (event != null)
        {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null)
            {
                checkServerUserFromFirebase(user,event.getMilkTeaModel());
            }
        }
    }

    private void checkServerUserFromFirebase(FirebaseUser user, MilkTeaModel milkTeaModel) {
        dialog.show();

        serverRef = FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(milkTeaModel.getUid())
                .child(Common.SHIPPER_REF);
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        {
                            ShipperUserModel userModel = dataSnapshot.getValue(ShipperUserModel.class);
                            if (userModel.isActive())
                            {
                                gotoHomeActivity(userModel,milkTeaModel);
                            }
                            else 
                            {
                                dialog.dismiss();
                                Toast.makeText(MilkTeaActivity.this, "Bạn cần được chấp nhận từ Admin", Toast.LENGTH_SHORT).show();

                            }
                        }
                        else
                        {
                            dialog.dismiss();
                            showRegisterDialog(user, milkTeaModel.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user, String uid) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information \nAdmin will accept your account late");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        //Set Data
        if (user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber()))
        {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }else
            edt_phone.setText(user.getPhoneNumber());


        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.setPositiveButton("REGISTER", (dialogInterface, which) -> {
            if (TextUtils.isEmpty(edt_name.getText().toString())) {
                Toast.makeText(MilkTeaActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            ShipperUserModel shipperUserModel = new ShipperUserModel();
            shipperUserModel.setUid(user.getUid());
            shipperUserModel.setName(edt_name.getText().toString());
            shipperUserModel.setPhone(edt_phone.getText().toString());
            shipperUserModel.setActive(false); //Default false, We must activate user manual in Firebase

            dialog.show();

            //Init ServerRef
            serverRef = FirebaseDatabase.getInstance(Common.URL).getReference(Common.MILKTEA_REF)
                    .child(uid)
                    .child(Common.SHIPPER_REF);

            serverRef.child(shipperUserModel.getUid())
                    .setValue(shipperUserModel)
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(MilkTeaActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show(); })
                    .addOnCompleteListener(task -> {
                        dialog.dismiss();
                        Toast.makeText(MilkTeaActivity.this, "Congratulation! Register Success! Admin will check and active you soon", Toast.LENGTH_SHORT).show();
//                                goToHomeActivity(shipperUserModel);

                    });
        });

        builder.setView(itemView);

        //Show Dialog
        androidx.appcompat.app.AlertDialog registerDialog = builder.create();
        registerDialog.show();
    }

    private void gotoHomeActivity(ShipperUserModel userModel, MilkTeaModel milkTeaModel) {
        dialog.dismiss();

        String jsonEncode = new Gson().toJson(milkTeaModel);
        Paper.init(this);
        Paper.book().write(Common.MILKTEA_SAVE,jsonEncode);

        Common.currentShipperUser = userModel;  //Important , If you don't do this line, when u access Common.currentShipperUser,this is null
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }
}