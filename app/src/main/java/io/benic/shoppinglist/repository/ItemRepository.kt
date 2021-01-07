package io.benic.shoppinglist.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ItemRepository @Inject constructor(private val itemDao: ItemDao) {
    fun getItems(cartId: Long): LiveData<List<Item>> {
        return itemDao.getFromCart(cartId)
    }

    fun addItem(item: Item): LiveData<Long> {
        val id = MutableLiveData<Long>()
        GlobalScope.launch(Dispatchers.IO) {
            id.postValue(itemDao.insert(item).first())
        }

        return id
    }

    fun updateItem(item: Item) {
        GlobalScope.launch(Dispatchers.IO) {
            itemDao.update(item)
        }
    }

    fun deleteItem(item: Item) {
        GlobalScope.launch(Dispatchers.IO) {
            itemDao.delete(item)
        }
    }
}