package io.benic.shoppinglist.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.benic.shoppinglist.R
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.utils.CurrencyHelper

class ShoppingCartRecycleAdapter(
    val data: MutableList<ShoppingCart>,
    private val listener: ShoppingCartRecycleListener
) : RecyclerView.Adapter<ShoppingCartRecycleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cartTitle: TextView = view.findViewById(R.id.cartTitle)
        val shortList: Array<TextView> = arrayOf(
            view.findViewById(R.id.item1),
            view.findViewById(R.id.item2),
            view.findViewById(R.id.item3)
        )
        val totalCost: TextView = view.findViewById(R.id.totalCost)
    }

    var selected: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_cart_item, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { listener.onEdit(data[position]) }

        holder.itemView.findViewById<Button>(R.id.duplicateButton).setOnClickListener {
            listener.onDuplicate(data[position])
        }

        holder.cartTitle.text = data[position].name
        for (i in 0 until 3) {
            holder.shortList[i].text = ""
        }

        if (data[position].maxCost in 1 until data[position].cost) {
            holder.totalCost.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.colorOverSpent
                )
            )
        }

        holder.totalCost.text = CurrencyHelper.getString(data[position].cost)

        val shortList = data[position].description.split("\n")
        for (i in shortList.indices) {
            holder.shortList[i].text = shortList[i]
        }
    }

    fun addCart(cart: ShoppingCart) {
        Log.i(TAG, "add cart ${cart.name}")
        data.add(cart)
        notifyItemInserted(data.size)
    }

    fun removeCartAt(position: Int) {
        Log.i(TAG, "remove cart at $position")
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addData(carts: List<ShoppingCart>) {
        Log.i(TAG, "add ${carts.size} carts")
        data.addAll(carts)
        notifyDataSetChanged()
    }

    companion object {
        private const val TAG = "ShoppingCartAdapter"
    }
}