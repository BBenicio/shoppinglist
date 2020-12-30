package io.benic.shoppinglist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.benic.shoppinglist.model.ShoppingCart
import java.text.NumberFormat
import java.util.function.IntFunction

class CartRecycleAdapter(private val data:ArrayList<ShoppingCart>,
                         private val editListener:(Int) -> Unit,
                         private val deleteListener:(Int) -> Unit,
                         private val duplicateListener: (Int) -> Unit)
    : RecyclerView.Adapter<CartRecycleAdapter.ViewHolder>() {

    class ViewHolder(view: View, editListener: (Int) -> Unit,
                     deleteListener: (Int) -> Unit,
                     duplicateListener: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val cartTitle:TextView
        val shortList:Array<TextView>
        val totalCost:TextView

        init {
            cartTitle = view.findViewById(R.id.cart_title)
//            shortList = view.findViewById(R.id.short_list)
            shortList = Array(3) { i->
                when (i) {
                    0 -> view.findViewById<TextView>(R.id.item1)
                    1 -> view.findViewById<TextView>(R.id.item2)
                    2 -> view.findViewById<TextView>(R.id.item3)
                    else ->  view.findViewById<TextView>(R.id.item1)
                }
            }
            totalCost = view.findViewById(R.id.total_cost)

            view.setOnClickListener{ _ -> editListener.invoke(adapterPosition) }

            view.findViewById<Button>(R.id.delete_button).setOnClickListener{ _ -> deleteListener.invoke(adapterPosition) }
            view.findViewById<Button>(R.id.duplicate_button).setOnClickListener{ _ -> duplicateListener.invoke(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_cart_item, parent, false)

        return ViewHolder(v, editListener, deleteListener, duplicateListener)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cartTitle.text = data[position].name
        for (i in 0 until 3) {
            holder.shortList[i].text = ""
        }
        var size = data[position].items.size
        if (size > 3) size = 3

        val cost = if (data[position].items.isNotEmpty())
            data[position].items
//            .filter { item -> item.checked } // filter to show only selected items (total spent)
            .map { item -> item.price * item.quantity }
            .reduce{ acc, price -> acc + price }
        else 0

        if (data[position].maxCost in 1 until cost) {
            holder.totalCost.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorOverSpent))
        }

        holder.totalCost.text = CurrencyHelper.getString(cost)

        for (i in 0 until size) {
            holder.shortList[i].text = data[position].items[i].name
        }
    }
}