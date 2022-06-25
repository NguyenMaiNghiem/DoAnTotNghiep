package com.nghiem.rilleyServer.Callback;

import com.nghiem.rilleyServer.Model.ShippingOrderModel;

public interface ISingleShippingOrderCallbackListener {
    void onSingleShippingOrderLoadSuccess(ShippingOrderModel shippingOrderModel);

}
