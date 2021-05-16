package io.benic.shoppinglist.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ShoppingCart::class, Item::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingCartDao(): ShoppingCartDao

    abstract fun itemDao(): ItemDao
}