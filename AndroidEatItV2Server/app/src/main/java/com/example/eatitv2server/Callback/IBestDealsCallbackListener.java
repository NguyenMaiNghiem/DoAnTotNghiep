package com.example.eatitv2server.Callback;

import com.example.eatitv2server.Model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallbackListener {
    void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
