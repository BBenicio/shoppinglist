package io.benic.shoppinglist.utils

import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase

class CurrencyHelperTest : TestCase() {
    fun testGetValue() {
        val value = CurrencyHelper.getValue("$1.23")
        assertThat(value).isEqualTo(123)
    }
}