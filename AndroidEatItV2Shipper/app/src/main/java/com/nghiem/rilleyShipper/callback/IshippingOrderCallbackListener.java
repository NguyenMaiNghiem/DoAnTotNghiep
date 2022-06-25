package com.nghiem.rilleyShipper.callback;

import com.nghiem.rilleyShipper.model.ShippingOrderModel;

import java.util.List;

public interface IshippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList);
    void onShippingOrderLoadFailed(String message);
}
