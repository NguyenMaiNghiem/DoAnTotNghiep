package com.nghiem.rilleyServer.Callback;

import com.nghiem.rilleyServer.Model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallbackListener {
    void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
