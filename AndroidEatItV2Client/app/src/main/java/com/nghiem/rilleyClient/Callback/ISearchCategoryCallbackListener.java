package com.nghiem.rilleyClient.Callback;

import com.nghiem.rilleyClient.Database.CartItem;
import com.nghiem.rilleyClient.Model.CategoryModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotfound(String message);
}
