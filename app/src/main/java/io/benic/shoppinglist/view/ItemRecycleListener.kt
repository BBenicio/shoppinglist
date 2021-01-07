package io.benic.shoppinglist.view

import android.text.TextWatcher
import android.widget.EditText

interface ItemRecycleListener {
    fun onQuantityChanged(position: Int, text: String)

    fun onNameChanged(position: Int, text: String)

    fun onPriceChanged(position: Int, text: String, textWatcher: TextWatcher, editText: EditText)

    fun onChecked(position: Int, checked: Boolean)

    fun onItemMoved(from: Int, to: Int)
}