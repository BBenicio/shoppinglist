package io.benic.shoppinglist.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.repository.ItemRepository
import io.benic.shoppinglist.repository.ShoppingCartRepository

class ItemViewModel @ViewModelInject constructor(
    private val itemRepository: ItemRepository,
    private val cartRepository: ShoppingCartRepository
) : ViewModel() {
    lateinit var items: LiveData<List<Item>>
    lateinit var cart: LiveData<ShoppingCart>

    fun fetchItems(cartId: Long): LiveData<List<Item>> {
        items = itemRepository.getItems(cartId)
        return items
    }

    fun fetchCart(cartId: Long): LiveData<ShoppingCart> {
        cart = cartRepository.getCart(cartId.toInt())
        return cart
    }

    fun createCart(cart: ShoppingCart): LiveData<Long> {
        return cartRepository.addCart(cart)
    }

    fun addItem(item: Item): LiveData<Long> {
        return itemRepository.addItem(item)
    }

    fun updateCart(cart: ShoppingCart) {
        cartRepository.updateCart(cart)
    }

    fun updateItem(item: Item) {
        itemRepository.updateItem(item)
    }

    fun deleteItem(item: Item) {
        itemRepository.deleteItem(item)
    }
}