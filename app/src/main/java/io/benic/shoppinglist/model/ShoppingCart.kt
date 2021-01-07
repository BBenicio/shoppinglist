package io.benic.shoppinglist.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "carts")
data class ShoppingCart(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var maxCost: Int = 0,
    @Ignore var items: ArrayList<Item> = ArrayList(),
    var cost: Int = 0,
    var description: String = ""
) {
    constructor(cart: ShoppingCart) : this(cart.id, cart.name.substring(0), cart.maxCost) {
        for (it in cart.items) {
            items.add(Item(it))
        }
    }

}