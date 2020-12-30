package io.benic.shoppinglist.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "carts")
data class ShoppingCart (
    @PrimaryKey(autoGenerate = true) var id:Long = 0,
    var name:String = "",
    var maxCost:Int = 0,
    @Ignore var items: ArrayList<Item> = ArrayList()
) {
    constructor(cart: ShoppingCart) : this(cart.id, cart.name.substring(0), cart.maxCost) {
        for (it in cart.items) {
            items.add(Item(it))
        }
    }

    object Carts {
        var carts: ArrayList<ShoppingCart> = ArrayList()

        fun get(id: Long) : ShoppingCart? {
            return carts.find { cart -> cart.id == id }
        }
    }
}