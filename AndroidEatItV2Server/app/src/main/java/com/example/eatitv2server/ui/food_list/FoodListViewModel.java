package com.example.eatitv2server.ui.food_list;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eatitv2server.Common.Common;
import com.example.eatitv2server.Model.FoodModel;

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
        }
        mutableLiveDataFoodList.setValue(Common.categorySelected.getFoods());
        return mutableLiveDataFoodList;
    }
}