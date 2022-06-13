package com.example.eatitv2server.ui.most_popular;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eatitv2server.Callback.IMostPopularCallbackListener;
import com.example.eatitv2server.Common.Common;
import com.example.eatitv2server.Model.MostPopularModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MostPopularViewModel extends ViewModel implements IMostPopularCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<MostPopularModel>> mostPopularListtMutable;
    private IMostPopularCallbackListener iMostPopularCallbackListener;

    public MostPopularViewModel() {
        iMostPopularCallbackListener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<MostPopularModel>> getMostPopularListtMutable() {
        if(mostPopularListtMutable==null)
            mostPopularListtMutable = new MutableLiveData<>();
        loadMostPopular();
        return mostPopularListtMutable;
    }

    public void loadMostPopular() {
        List<MostPopularModel> tempList = new ArrayList<>();
        DatabaseReference mostPopularRef = FirebaseDatabase.getInstance().getReference(Common.MOST_POPULAR);

        mostPopularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot mostPopularSnapshot: dataSnapshot.getChildren())
                {
                    MostPopularModel mostPopularModel = mostPopularSnapshot.getValue(MostPopularModel.class);
                    mostPopularModel.setKey(mostPopularSnapshot.getKey());
                    tempList.add(mostPopularModel);
                }

                iMostPopularCallbackListener.onListMostPopularLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                iMostPopularCallbackListener.onListMostPopularLoadFailed(databaseError.getMessage());
            }
        });
    }

    @Override
    public void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels) {
        mostPopularListtMutable.setValue(mostPopularModels);
    }

    @Override
    public void onListMostPopularLoadFailed(String message) {
        messageError.setValue(message);
    }
}
