package io.benic.shoppinglist.view

import io.benic.shoppinglist.model.ShoppingCart

interface ShoppingCartRecycleListener {
    fun onEdit(cart: ShoppingCart)
    fun onDuplicate(cart: ShoppingCart)
}