package com.nghiem.rilleyClient.Remote;

import com.nghiem.rilleyClient.Model.FCMResponse;
import com.nghiem.rilleyClient.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAukSDBxk:APA91bE6LOXG21H1vNoytCD0QMbamg3lK5jmyxcqL2gMNIpyj0PDZOYHxOaZP8VaOhclGikbXeEAmEtqjLq762jIwPIhSrLKy-WCtNhiwD6SQ-icrbk2WJ4AcN0pvWoWQ23eupeUfApz"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification (@Body FCMSendData body);

}