package com.nghiem.rilleyClient.ui.foodlist;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Model.FoodModel;

import java.util.List;

public class FoodListViewModel extends ViewModel {

    private MutableLiveData<List<FoodModel>> mutableLiveDataFoodList;
    private MutableLiveData<String> messageError = new MutableLiveData<>();


    public FoodListViewModel() {

    }

    public MutableLiveData<List<FoodModel>> getMutableLiveDataFoodList() {
        if(mutableLiveDataFoodList == null)
        {
            mutableLiveDataFoodList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            mutableLiveDataFoodList.setValue(Common.categorySelected.getFoods());

        }

        return mutableLiveDataFoodList;
    }

}