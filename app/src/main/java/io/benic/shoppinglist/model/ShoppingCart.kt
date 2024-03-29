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
    var description: String = "",
    var createdAt: Long = System.currentTimeMillis()
) {
    constructor(cart: ShoppingCart) : this(
        id = cart.id,
        name = cart.name.substring(0),
        maxCost = cart.maxCost,
        cost = cart.cost,
        description = cart.description.substring(0)
    ) {
        items.addAll(cart.items)

    }

}