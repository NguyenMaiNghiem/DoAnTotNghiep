package com.example.eatitv2server.Callback;

import com.example.eatitv2server.Model.ShippingOrder;

public interface ISingleShippingOrderCallbackListener {
    void onSingleShippingOrderLoadSuccess(ShippingOrder shippingOrderModel);

}
