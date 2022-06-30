package com.nghiem.rilleyShipper.service;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.nghiem.rilleyShipper.MainActivity;
import com.nghiem.rilleyShipper.common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nghiem.rilleyShipper.eventbus.UpdateShippingOrderEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null)
        {
            if (dataRecv.get(Common.NOTI_TITLE).equals("You have new order need ship"))
            {

                ///Here we need call main activity because we have to assign value for Common.currentUser
                //So we must call mainactivity. to do that, if you directly call home activity it will crash
                //As it can be assigned to mainactivyt after Login.
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, true); //Use extra to detect is app from notification.

                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        intent);
                EventBus.getDefault().postSticky(new UpdateShippingOrderEvent());   //Update order list when have new order need ship

            } else
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
        }
    }

    @Override
    public void onNewToken(@NonNull String newToken) {
        super.onNewToken(newToken);
        Common.updateNewToken(this, newToken, true, false); //Becasue we are in server app, isServer=true
    }
}
