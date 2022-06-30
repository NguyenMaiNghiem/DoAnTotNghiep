package com.nghiem.rilleyClient.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {
    Flowable<List<CartItem>> getAllCart(String uid, String milkteaId);

    Single<Integer> countItemInCart(String uid, String milkteaId);

    Single<Double> sumPriceInCart(String uid, String milkteaId);

    Single<CartItem> getItemInCart(String foodId, String uid, String milkteaId);

    Completable insertOrReplaceAll(CartItem... cartItems);

    Single<Integer> updateCartItem(CartItem cartItem);

    Single<Integer> deleteCartItem(CartItem cartItem);

    Single<Integer> cleanCart(String uid, String milkteaId);

    Single<CartItem> getItemWithAllOptionsInCart(String uid, String categoryId, String foodId, String foodSize, String foodAddon, String milkteaId);
}
