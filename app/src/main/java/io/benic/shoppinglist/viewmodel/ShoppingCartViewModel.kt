package io.benic.shoppinglist.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.repository.ItemRepository
import io.benic.shoppinglist.repository.ShoppingCartRepository

class ShoppingCartViewModel @ViewModelInject constructor(
    private val shoppingCartRepository: ShoppingCartRepository,
    private val itemRepository: ItemRepository
) :
    ViewModel() {
    val carts: LiveData<List<ShoppingCart>>
        get() = shoppingCartRepository.getCarts()

    fun remove(cart: ShoppingCart) = shoppingCartRepository.removeCart(cart)

    fun add(cart: ShoppingCart) = shoppingCartRepository.addCart(cart)

    fun getItemsFromCart(cart: ShoppingCart) = itemRepository.getItems(cart.id)

    fun addItemsToCart(cart: ShoppingCart, items: List<Item>) {
        for (item in items) {
            item.id = 0
            item.cartId = cart.id
            itemRepository.addItem(item)
        }
    }
}