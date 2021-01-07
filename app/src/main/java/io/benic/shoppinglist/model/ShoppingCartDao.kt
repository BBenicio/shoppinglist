package io.benic.shoppinglist.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShoppingCartDao {
    @Query("select * from carts")
    fun getAll(): LiveData<List<ShoppingCart>>

    @Query("select * from carts where id = :id")
    fun getFromId(id: Int): LiveData<ShoppingCart>

    @Insert
    fun insert(vararg cart: ShoppingCart): List<Long>

    @Delete
    fun delete(cart: ShoppingCart)

    @Update
    fun update(vararg cart: ShoppingCart)
}