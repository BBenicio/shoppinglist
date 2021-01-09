package io.benic.shoppinglist.utils

import java.text.NumberFormat

object CurrencyHelper {
    fun getString(value: Int): String = NumberFormat.getCurrencyInstance().format(value / 100.0)

    fun getValue(str: String): Int {
        val clean = str.filter { c -> c.isDigit() }

        return clean.toIntOrNull() ?: 0
    }
}