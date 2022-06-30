package com.nghiem.rilleyShipper.callback;

import com.nghiem.rilleyShipper.model.MilkTeaModel;

import java.util.List;

public interface IMilkteaCallbackListener {
    void onMilkteaLoadSuccess(List<MilkTeaModel> milkTeaModelList);
    void onMilkteaLoadFailed(String message);
}
