package com.example.eatitv2client.Callback;

import com.example.eatitv2client.Model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
