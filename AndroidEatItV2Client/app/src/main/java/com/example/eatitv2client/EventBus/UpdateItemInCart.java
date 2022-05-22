package com.example.eatitv2client.EventBus;

import com.example.eatitv2client.Database.CartItem;

public class UpdateItemInCart {
    private CartItem cartItem;

    public UpdateItemInCart(CartItem cartItem)
    {
        this.cartItem = cartItem;
    }

    public CartItem getCartItem() {
        return cartItem;
    }

    public void setCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
    }
}
