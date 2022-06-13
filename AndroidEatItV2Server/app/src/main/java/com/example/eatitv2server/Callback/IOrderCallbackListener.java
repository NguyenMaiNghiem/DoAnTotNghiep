package com.example.eatitv2server.Callback;

import com.example.eatitv2server.Model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderList);
    void onOrderLoadFailed(String message);
}
