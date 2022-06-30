package com.nghiem.rilleyClient.ui.milktea;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nghiem.rilleyClient.Callback.IMilkteaCallbackListener;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Model.MilkTeaModel;

import java.util.ArrayList;
import java.util.List;

public class MilkTeaViewModel extends ViewModel implements IMilkteaCallbackListener {
    private MutableLiveData<List<MilkTeaModel>> milkteaListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private IMilkteaCallbackListener listener;

    public MilkTeaViewModel() {
        listener = this;
    }

    public MutableLiveData<List<MilkTeaModel>> getMilkteaListMutable() {
        if (milkteaListMutable == null) {
            milkteaListMutable = new MutableLiveData<>();
            loadMilkteaFromFirebase();
        }
        return milkteaListMutable;
    }

    private void loadMilkteaFromFirebase() {
        List<MilkTeaModel> milkTeaModels = new ArrayList<>();
        DatabaseReference milkteaRef = FirebaseDatabase.getInstance()
                .getReference(Common.MILKTEA_REF);
        milkteaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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

    @Override
    public void onMilkteaLoadSuccess(List<MilkTeaModel> milkTeaModelList) {
        milkteaListMutable.setValue(milkTeaModelList);
    }

    @Override
    public void onMilkteaLoadFailed(String message) {
        messageError.setValue(message);
    }
}