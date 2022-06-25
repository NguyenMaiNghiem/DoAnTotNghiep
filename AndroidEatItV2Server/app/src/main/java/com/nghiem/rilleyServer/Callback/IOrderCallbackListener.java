package com.nghiem.rilleyServer.Callback;

import com.nghiem.rilleyServer.Model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderList);
    void onOrderLoadFailed(String message);
}
