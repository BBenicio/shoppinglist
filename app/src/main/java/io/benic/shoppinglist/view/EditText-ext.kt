package io.benic.shoppinglist.view

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.addTextChangedListener(afterTextChanged: (String, TextWatcher) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged(s.toString(), this)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}