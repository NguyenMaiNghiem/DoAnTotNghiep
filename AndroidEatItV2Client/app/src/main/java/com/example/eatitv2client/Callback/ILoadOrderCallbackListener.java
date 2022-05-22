package com.example.eatitv2client.Callback;

import com.example.eatitv2client.Model.OrderModel;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<OrderModel> orderList);
    void onLoadOrderFailed(String message);
}
