package com.example.eatitv2client.Callback;

import com.example.eatitv2client.Model.BestDealModel;
import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
