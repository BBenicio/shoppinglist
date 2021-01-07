package io.benic.shoppinglist.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items", indices = [Index("cartId")],
    foreignKeys = [ForeignKey(
        entity = ShoppingCart::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("cartId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Item(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var price: Int = 0,
    var quantity: Int = 1,
    var checked: Boolean = false,
    var position: Int = 0,
    var cartId: Long = 0
) {
    constructor(item: Item) : this(
        0,
        item.name.substring(0),
        item.price,
        item.quantity,
        item.checked,
        item.position,
        item.cartId
    )

    override fun equals(other: Any?): Boolean {
        if (other is Item) {
            return id == other.id && cartId == other.cartId && name == other.name && price == other.price && quantity == other.quantity && checked == other.checked && position == other.position
        }

        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + price
        result = 31 * result + quantity
        result = 31 * result + checked.hashCode()
        result = 31 * result + position
        result = 31 * result + cartId.hashCode()
        return result
    }
}