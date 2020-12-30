package io.benic.shoppinglist

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.benic.shoppinglist.model.AppDatabase
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import kotlin.concurrent.thread

object Shared {
    private val TAG = "Shared"

    var current:Int = 0
    var frag: Fragment? = null
    var cart: ShoppingCart? = null

    lateinit var database: AppDatabase
    var databaseReady:Boolean = false

    fun startDB(context:Context) {
        databaseReady = false
//        if (!this::database.isInitialized) {

            database = Room.databaseBuilder(context, AppDatabase::class.java, "shopping_cart")
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        Log.i(TAG, "Executing database migration from 1 to 2")
                        database.execSQL("alter table carts add column maxCost INTEGER default 0")
                    }
                })
                .addMigrations(object : Migration(2, 3) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        Log.i(TAG, "Executing database migration from 2 to 3")
                        database.execSQL("create index index_items_cartId on items(cartId)")
                    }
                })
                .addMigrations(object : Migration(3, 4) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        Log.i(TAG, "Executing database migration from 3 to 4")
                        database.execSQL("alter table items add column position integer default -1 not null")
                    }
                })
                .build()

//        }

        thread {
            Log.i(TAG, "loading database")
            Log.d(TAG, "loading cart data from database")
            ShoppingCart.Carts.carts = database.shoppingCartDao().getAll() as ArrayList<ShoppingCart>

            for (c in ShoppingCart.Carts.carts) {
                Log.d(TAG, "cart #${c.id}: '${c.name}'")
            }

            Log.d(TAG, "loading item data from database")
            for (cart in ShoppingCart.Carts.carts) {
                cart.items = database.itemDao().getFromCart(cart.id) as ArrayList<Item>
            }

            Log.i(TAG, "database ready")
            databaseReady = true
        }
    }

    fun insert(vararg item: Item) {
        thread {
            Log.i(TAG, "inserting item")
            val ids = database.itemDao().insert(*item)
            for (i in ids.indices) {
                Log.d(TAG, "item ${item[i].id} -> ${ids[i]}")
                item[i].id = ids[i]
            }
        }
    }

    fun update(vararg item: Item) {
        thread {
            Log.d(TAG, "updating item")
            database.itemDao().update(*item)
        }
    }

    fun delete(item:Item) {
        thread {
            Log.i(TAG, "deleting item")
            database.itemDao().delete(item)
        }
    }

    fun insert(vararg cart: ShoppingCart) {
        thread {
            Log.i(TAG, "inserting cart")
            val ids = database.shoppingCartDao().insert(*cart)
            for (i in ids.indices) {
                Log.d(TAG, "cart ${cart[i].id} -> ${ids[i]}")
                cart[i].id = ids[i]
                for (it in cart[i].items) {
                    if (it.cartId != cart[i].id) {
                        it.cartId = cart[i].id
                        Shared.insert(it)
                    }
                }
            }
        }
    }

    fun update(vararg cart: ShoppingCart) {
        thread {
            Log.d(TAG, "updating cart")
            database.shoppingCartDao().update(*cart)
        }
    }

    fun delete(cart: ShoppingCart) {
        thread {
            Log.i(TAG, "deleting cart")
            database.shoppingCartDao().delete(cart)
        }
    }
}