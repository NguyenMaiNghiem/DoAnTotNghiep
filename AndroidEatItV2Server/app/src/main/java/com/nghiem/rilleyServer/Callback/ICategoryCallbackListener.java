package com.nghiem.rilleyServer.Callback;

import com.nghiem.rilleyServer.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModels);
    void onCategoryLoadFailed(String message);
}
