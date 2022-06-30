package com.nghiem.rilleyServer.ui.discount;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nghiem.rilleyServer.Callback.IDiscountCallbackListener;
import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.Model.DiscountModel;

import java.util.ArrayList;
import java.util.List;

public class DiscountViewModel extends ViewModel implements IDiscountCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<DiscountModel>> discountMutableLiveData;
    private IDiscountCallbackListener discountCallbackListener;

    public DiscountViewModel() {
        discountCallbackListener = this;
    }

    public MutableLiveData<List<DiscountModel>> getDiscountMutableLiveData() {
        if (discountMutableLiveData == null) discountMutableLiveData = new MutableLiveData<>();
        loadDiscount();
        return discountMutableLiveData;
    }

    public void loadDiscount() {
        List<DiscountModel> temp = new ArrayList<>();
        DatabaseReference discountRef = FirebaseDatabase.getInstance()
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentServerUser.getMilktea())
                .child(Common.DISCOUNT);
        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildren().iterator().hasNext())
                {
                    for (DataSnapshot discountSnapshot : snapshot.getChildren())
                    {
                        DiscountModel discountModel = discountSnapshot.getValue(DiscountModel.class);
                        discountModel.setKey(discountSnapshot.getKey());
                        temp.add(discountModel);
                    }
                    discountCallbackListener.onListDiscountLoadSuccess(temp);
                }
                else
                    discountCallbackListener.onListDiscountLoadFailed("Empty Data");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                discountCallbackListener.onListDiscountLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListDiscountLoadSuccess(List<DiscountModel> discountModelList) {
        discountMutableLiveData.setValue(discountModelList);
    }

    @Override
    public void onListDiscountLoadFailed(String message) {
        if (message.equals("Empty Data"))
            discountMutableLiveData.setValue(null);
        messageError.setValue(message);
    }
}