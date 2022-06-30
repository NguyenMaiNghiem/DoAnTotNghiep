package com.nghiem.rilleyClient.Callback;

import com.nghiem.rilleyClient.Model.CategoryModel;
import com.nghiem.rilleyClient.Model.MilkTeaModel;

import java.util.List;

public interface IMilkteaCallbackListener {
    void onMilkteaLoadSuccess(List<MilkTeaModel> milkTeaModelList);
    void onMilkteaLoadFailed(String message);
}
