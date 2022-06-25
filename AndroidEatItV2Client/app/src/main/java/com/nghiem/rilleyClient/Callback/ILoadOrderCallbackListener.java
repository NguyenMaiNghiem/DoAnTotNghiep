package com.nghiem.rilleyClient.Callback;

import com.nghiem.rilleyClient.Model.OrderModel;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<OrderModel> orderList);
    void onLoadOrderFailed(String message);
}
