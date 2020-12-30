package io.benic.shoppinglist.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.benic.shoppinglist.CurrencyHelper
import java.text.NumberFormat

@Entity(tableName = "items", indices = [Index("cartId")],
    foreignKeys = [ForeignKey(entity = ShoppingCart::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("cartId"),
        onDelete = ForeignKey.CASCADE)]
)
data class Item (
    @PrimaryKey(autoGenerate = true) var id:Long = 0,
    var name:String = "",
    var price:Int = 0,
    var quantity:Int = 1,
    var checked:Boolean = false,
    var position:Int = 0,
    var cartId:Long = 0
) {
    constructor(item: Item) : this(0, item.name.substring(0), item.price, item.quantity, item.checked, item.position, item.cartId)
}