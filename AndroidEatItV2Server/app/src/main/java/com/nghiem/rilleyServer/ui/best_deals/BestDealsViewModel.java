package com.nghiem.rilleyServer.ui.best_deals;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nghiem.rilleyServer.Callback.IBestDealsCallbackListener;
import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.Model.BestDealsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<BestDealsModel>> bestDealsListtMutable;
    private IBestDealsCallbackListener bestDealsCallbackListener;

    public BestDealsViewModel() {
        bestDealsCallbackListener = this;
    }

    public MutableLiveData<List<BestDealsModel>> getBestDealsListtMutable() {
        if(bestDealsListtMutable==null)
            bestDealsListtMutable = new MutableLiveData<>();
        loadBestDeals();
        return bestDealsListtMutable;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public void loadBestDeals() {
        List<BestDealsModel> tempList = new ArrayList<>();
        DatabaseReference bestDealsRef = FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentServerUser.getMilktea())
                .child(Common.BEST_DEALS);

        bestDealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot bestdealsSnapshot: dataSnapshot.getChildren())
                {
                    BestDealsModel bestDealsModel = bestdealsSnapshot.getValue(BestDealsModel.class);
                    bestDealsModel.setKey(bestdealsSnapshot.getKey());
                    tempList.add(bestDealsModel);
                }

                bestDealsCallbackListener.onListBestDealsLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                bestDealsCallbackListener.onListBestDealsLoadFailed(databaseError.getMessage());
            }
        });
    }

    @Override
    public void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels) {
        bestDealsListtMutable.setValue(bestDealsModels);
    }

    @Override
    public void onListBestDealsLoadFailed(String message) {
        messageError.setValue(message);
    }
}
