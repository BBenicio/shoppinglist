package io.benic.shoppinglist.utils

import java.text.NumberFormat

object CurrencyHelper {
    fun getString(value: Int): String = NumberFormat.getCurrencyInstance().format(value / 100.0)

    fun getValue(str: String): Int {
        val clean = str.replace("[$.,]".toRegex(), "")
        var value = 0
        try {
            value = if (clean.isNotEmpty()) clean.toInt() else 0
        } catch (numberFormatException: NumberFormatException) {
            numberFormatException.printStackTrace()
        }

        return value
    }
}