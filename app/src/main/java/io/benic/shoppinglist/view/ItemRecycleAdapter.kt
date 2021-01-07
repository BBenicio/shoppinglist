package io.benic.shoppinglist.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import io.benic.shoppinglist.R
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.utils.CurrencyHelper
import java.util.*

class ItemRecycleAdapter(
    private var data: ArrayList<Item>,
    private val listener: ItemRecycleListener
) : RecyclerView.Adapter<ItemRecycleAdapter.ViewHolder>(), SearchView.OnQueryTextListener {

    private var filtered: List<Item> = data
    private var filterText: String = ""

    class ViewHolder(
        view: View,
        listener: ItemRecycleListener
    ) : RecyclerView.ViewHolder(view) {

        val quantity: EditText
        val name: EditText
        val price: EditText
        val checkBox: CheckBox

        init {
            quantity = view.findViewById(R.id.quantity_edit)
            name = view.findViewById(R.id.item_name_edit)
            price = view.findViewById(R.id.item_price_edit)
            checkBox = view.findViewById(R.id.item_check)

            quantity.addTextChangedListener { s, _ ->
                listener.onQuantityChanged(
                    adapterPosition,
                    s
                )
            }
            name.addTextChangedListener { s, _ -> listener.onNameChanged(adapterPosition, s) }
            price.addTextChangedListener { s, t ->
                listener.onPriceChanged(
                    adapterPosition,
                    s,
                    t,
                    price
                )
            }
            checkBox.setOnCheckedChangeListener { _, b -> listener.onChecked(adapterPosition, b) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_list_item, parent, false)

        return ViewHolder(v, listener)
    }

    override fun getItemCount(): Int = if (isFiltered()) filtered.size else data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ds = if (isFiltered()) filtered else data

        holder.name.setText(ds[position].name)
        holder.quantity.setText(ds[position].quantity.toString())
        holder.checkBox.isChecked = ds[position].checked

        holder.price.setText(CurrencyHelper.getString(ds[position].price))
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.i(TAG, "query text submit '$query'")
        filterText = query ?: ""
        filtered = if (filterText.isEmpty()) {
            data
        } else {
            data.filter { item -> item.name.contains(filterText, true) }
        }
        notifyDataSetChanged()
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        Log.i(TAG, "query text '$query'")
        filterText = query ?: ""
        filtered = if (filterText.isEmpty()) {
            data
        } else {
            data.filter { item -> item.name.contains(filterText, true) }
        }
        notifyDataSetChanged()
        return true
    }

    fun getItem(position: Int): Item {
        return if (isFiltered()) filtered[position] else data[position]
    }

    fun setItemsChecked(checked: Boolean) {
        for (i in 0 until data.size) {
            data[i].checked = checked
            notifyItemChanged(i)
        }
    }

    fun moveItem(from: Int, to: Int) {
        if (isFiltered()) {
            Log.i(TAG, "list is filtered, no moving allowed")
        }
        Log.d(TAG, "move item from $from to $to")

        if (from < to) {
            for (i in from until to) {
                val swap = data[i].position
                data[i].position = data[i + 1].position
                data[i + 1].position = swap

                Collections.swap(data, i, i + 1)
            }
        } else if (from > to) {
            for (i in from downTo to + 1) {
                val swap = data[i].position
                data[i].position = data[i - 1].position
                data[i - 1].position = swap

                Collections.swap(data, i, i - 1)
            }
        }

        for (item in data) {
            Log.d(TAG, "'${item.name}' at ${item.position}")
        }

        listener.onItemMoved(from, to)
        notifyItemMoved(from, to)
    }

    fun isFiltered(): Boolean = filterText.isNotEmpty()

    fun changeData(data: ArrayList<Item>) {
        Log.i(TAG, "changing all data")
        this.data = data
        onQueryTextSubmit(filterText)
    }

    companion object {
        private const val TAG = "ItemRecycleAdapter"
    }
}