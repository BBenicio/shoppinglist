package io.benic.shoppinglist.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.repository.ShoppingCartRepository

class ShoppingCartViewModel @ViewModelInject constructor(
    private val shoppingCartRepository: ShoppingCartRepository,
) :
    ViewModel() {
    val carts: LiveData<List<ShoppingCart>>
        get() = shoppingCartRepository.getCarts()

    fun remove(cart: ShoppingCart) = shoppingCartRepository.removeCart(cart)

    fun add(cart: ShoppingCart) = shoppingCartRepository.addCart(cart)
}