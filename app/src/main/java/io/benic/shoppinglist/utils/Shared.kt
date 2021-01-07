package io.benic.shoppinglist.utils

import androidx.fragment.app.Fragment
import io.benic.shoppinglist.model.ShoppingCart

object Shared {
    var current: Int = 0
    var frag: Fragment? = null
    var cart: ShoppingCart? = null
}