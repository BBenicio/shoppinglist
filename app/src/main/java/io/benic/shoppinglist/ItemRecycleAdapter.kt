package io.benic.shoppinglist

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.math.MathUtils
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import java.util.*
import java.util.function.IntFunction
import kotlin.collections.ArrayList

class ItemRecycleAdapter(private val data:ArrayList<Item>,
                         private val quantityListener: (Int, String) -> Unit,
                         private val nameListener: (Int, String) -> Unit,
                         private val priceListener: (Int, String, TextWatcher, EditText) -> Unit,
                         private val checkListener: (Int, Boolean) -> Unit)
    : RecyclerView.Adapter<ItemRecycleAdapter.ViewHolder>(), SearchView.OnQueryTextListener {

    private var filtered:List<Item> = data
    private var filterText:String = ""

    class ViewHolder(view: View,
                     private val quantityListener: (Int, String) -> Unit,
                     private val nameListener: (Int, String) -> Unit,
                     private val priceListener: (Int, String, TextWatcher, EditText) -> Unit,
                     private val checkListener: (Int, Boolean) -> Unit)
        : RecyclerView.ViewHolder(view) {

        val quantity:EditText
        val name:EditText
        val price:EditText
        val checkBox:CheckBox

        init {
            quantity = view.findViewById(R.id.quantity_edit)
            name = view.findViewById(R.id.item_name_edit)
            price = view.findViewById(R.id.item_price_edit)
            checkBox = view.findViewById(R.id.item_check)

            quantity.addTextChangedListener{ s, _ -> quantityListener.invoke(adapterPosition, s) }
            name.addTextChangedListener{ s, _ -> nameListener.invoke(adapterPosition, s) }
            price.addTextChangedListener{ s, t -> priceListener.invoke(adapterPosition, s, t, price) }
            checkBox.setOnCheckedChangeListener{ _, b -> checkListener.invoke(adapterPosition, b) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_list_item, parent, false)

        return ViewHolder(v, quantityListener, nameListener, priceListener, checkListener)
    }

    override fun getItemCount(): Int = filtered.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.setText(filtered[position].name)
        holder.quantity.setText(filtered[position].quantity.toString())
        holder.checkBox.isChecked = filtered[position].checked

        holder.price.setText(CurrencyHelper.getString(filtered[position].price))
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

    fun getItem(position: Int):Item {
        return filtered[position]
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
            for (i in from downTo to+1) {
                val swap = data[i].position
                data[i].position = data[i - 1].position
                data[i - 1].position = swap

                Collections.swap(data, i, i - 1)
            }
        }

        for (item in data) {
            Log.d(TAG, "'${item.name}' at ${item.position}")
        }

        notifyItemMoved(from, to)

//        onQueryTextChange(filterText)
    }

    fun isFiltered(): Boolean {
        return filterText.isNotEmpty()
    }

    companion object {
        private const val TAG = "ItemRecycleAdapter"
    }
}

fun EditText.addTextChangedListener(afterTextChanged: (String, TextWatcher) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            afterTextChanged.invoke(p0.toString(), this)
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    })
}