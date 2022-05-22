package com.example.eatitv2client.Callback;

import com.example.eatitv2client.Database.CartItem;
import com.example.eatitv2client.Model.CategoryModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotfound(String message);
}
