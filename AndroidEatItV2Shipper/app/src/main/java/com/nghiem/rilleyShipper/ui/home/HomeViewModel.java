package com.nghiem.rilleyShipper.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nghiem.rilleyShipper.callback.IshippingOrderCallbackListener;
import com.nghiem.rilleyShipper.common.Common;
import com.nghiem.rilleyShipper.model.ShippingOrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel implements IshippingOrderCallbackListener {

    private MutableLiveData<List<ShippingOrderModel>> shippingOrderMutableData;
    private MutableLiveData<String> messageError;
    private IshippingOrderCallbackListener listener;

    public HomeViewModel() {
        shippingOrderMutableData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<ShippingOrderModel>> getShippingOrderMutableData(String shipperPhone) {
        if (shippingOrderMutableData == null)
        {
            shippingOrderMutableData = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadOrderByShipper(shipperPhone);
        }
        return shippingOrderMutableData;
    }

    public void loadOrderByShipper(String shipperPhone){
        List<ShippingOrderModel> tempList = new ArrayList<>();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance(Common.URL).getReference(Common.SHIPPER_ORDER_REF);
        Query orderRef = shipperRef.orderByChild("shipperPhone").equalTo(Common.currentShipperUser.getPhone());

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnapshot : snapshot.getChildren())
                {
                    ShippingOrderModel shippingOrderModel = orderSnapshot.getValue(ShippingOrderModel.class);
                    Log.i("data","co du lieu loadOrderByShipper:"+shippingOrderModel.toString());
                    shippingOrderModel.setKey(orderSnapshot.getKey());
                    tempList.add(shippingOrderModel);
                }
                listener.onShippingOrderLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onShippingOrderLoadFailed(error.getMessage());
            }
        });
    }

    @Override
    public void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList) {
        shippingOrderMutableData.setValue(shippingOrderModelList);
    }

    @Override
    public void onShippingOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}