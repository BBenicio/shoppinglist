package io.benic.shoppinglist.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.model.ShoppingCartDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShoppingCartRepository @Inject constructor(private val shoppingCartDao: ShoppingCartDao) {

    fun getCarts(): LiveData<List<ShoppingCart>> {
        return shoppingCartDao.getAll()
    }

    fun getCart(cartId: Int): LiveData<ShoppingCart> {
        return shoppingCartDao.getFromId(cartId)
    }

    fun removeCart(cart: ShoppingCart) {
        GlobalScope.launch(Dispatchers.IO) {
            shoppingCartDao.delete(cart)
        }
    }

    fun addCart(cart: ShoppingCart): LiveData<Long> {
        val id = MutableLiveData<Long>()
        GlobalScope.launch(Dispatchers.IO) {
            id.postValue(shoppingCartDao.insert(cart).first())
        }

        return id
    }

    fun updateCart(cart: ShoppingCart) {
        GlobalScope.launch(Dispatchers.IO) {
            shoppingCartDao.update(cart)
        }
    }
}