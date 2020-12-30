package io.benic.shoppinglist.model

import androidx.room.*

@Dao
interface ItemDao {

    @Query("select * from items order by position asc")
    fun getAll() : List<Item>

    @Query("select * from items where cartId = :cartId order by position asc")
    fun getFromCart(cartId:Long) : List<Item>

    @Insert
    fun insert(vararg item: Item) : List<Long>

    @Delete
    fun delete(item: Item)

    @Update
    fun update(vararg item: Item)
}