package com.nghiem.rilleyServer.Callback;

import com.nghiem.rilleyServer.Model.DiscountModel;

import java.util.List;

public interface IDiscountCallbackListener {
    void onListDiscountLoadSuccess(List<DiscountModel> discountModelList);
    void onListDiscountLoadFailed(String message);
}
