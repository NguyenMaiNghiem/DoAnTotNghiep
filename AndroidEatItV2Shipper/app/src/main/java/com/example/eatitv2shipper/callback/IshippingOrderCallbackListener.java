package com.example.eatitv2shipper.callback;

import com.example.eatitv2shipper.model.ShippingOrderModel;

import java.util.List;

public interface IshippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList);
    void onShippingOrderLoadFailed(String message);
}
