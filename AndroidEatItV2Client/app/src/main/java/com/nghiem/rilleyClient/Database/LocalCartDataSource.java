package com.nghiem.rilleyClient.Database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSource {

    private CartDAO cartDAO;

    public LocalCartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<CartItem>> getAllCart(String uid, String milkteaId) {
        return cartDAO.getAllCart(uid,milkteaId);
    }

    @Override
    public Single<Integer> countItemInCart(String uid, String milkteaId) {
        return cartDAO.countItemInCart(uid, milkteaId);
    }

    @Override
    public Single<Double> sumPriceInCart(String uid, String milkteaId) {
        return cartDAO.sumPriceInCart(uid, milkteaId);
    }

    @Override
    public Single<CartItem> getItemInCart(String foodId, String uid, String milkteaId) {
        return cartDAO.getItemInCart(foodId, uid, milkteaId);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAO.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCartItem(CartItem cartItem) {
        return cartDAO.updateCartItem(cartItem);
    }

    @Override
    public Single<Integer> deleteCartItem(CartItem cartItem) {
        return cartDAO.deleteCartItem(cartItem);
    }

    @Override
    public Single<Integer> cleanCart(String uid, String milkteaId) {
        return cartDAO.cleanCart(uid, milkteaId);
    }

    @Override
    public Single<CartItem> getItemWithAllOptionsInCart(String uid, String categoryId, String foodId, String foodSize, String foodAddon, String milkteaId) {
        return cartDAO.getItemWithAllOptionsInCart(uid,categoryId,foodId,foodSize,foodAddon,milkteaId);
    }
}
