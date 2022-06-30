package com.nghiem.rilleyClient.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {

    @Query("SELECT * FROM Cart WHERE uid=:uid AND milkteaId=:milkteaId")
    Flowable<List<CartItem>> getAllCart(String uid,String milkteaId);

    @Query("SELECT SUM(foodQuantity) FROM Cart WHERE uid=:uid AND milkteaId=:milkteaId")
    Single<Integer> countItemInCart(String uid,String milkteaId);

    @Query("SELECT SUM((foodPrice+foodExtraPrice) * foodQuantity) FROM Cart WHERE uid=:uid AND milkteaId=:milkteaId")
    Single<Double> sumPriceInCart(String uid,String milkteaId);

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid AND milkteaId=:milkteaId")
    Single<CartItem> getItemInCart(String foodId, String uid, String milkteaId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCartItem(CartItem cartItem);

    @Delete
    Single<Integer> deleteCartItem(CartItem cartItem);

    @Query("DELETE FROM Cart WHERE uid=:uid AND milkteaId=:milkteaId")
    Single<Integer> cleanCart(String uid, String milkteaId);

    @Query("SELECT * FROM Cart WHERE categoryId=:categoryId AND foodId=:foodId AND uid=:uid AND foodSize=:foodSize AND foodAddon=:foodAddon AND milkteaId=:milkteaId")
    Single<CartItem> getItemWithAllOptionsInCart(String uid, String categoryId, String foodId, String foodSize, String foodAddon, String milkteaId);


}
