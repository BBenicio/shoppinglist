package io.benic.shoppinglist.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import getOrAwaitValue
import io.benic.shoppinglist.model.AppDatabase
import io.benic.shoppinglist.model.ShoppingCart
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ShoppingCartRepositoryTest {
    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private lateinit var cartRepository: ShoppingCartRepository
    private lateinit var db: AppDatabase

    private val defaultCart = ShoppingCart(1, "name")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val cartDao = db.shoppingCartDao()
        cartDao.insert(defaultCart)

        cartRepository = ShoppingCartRepository(cartDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getCarts() {
        val carts = cartRepository.getCarts().getOrAwaitValue()
        assertThat(carts).containsExactly(defaultCart)
    }

    @Test
    fun getCart() {
        val cart = cartRepository.getCart(1).getOrAwaitValue()
        assertThat(cart).isEqualTo(defaultCart)
    }

    @Test
    fun removeCart() {
        cartRepository.removeCart(defaultCart)

        val carts = cartRepository.getCarts().getOrAwaitValue()
        assertThat(carts).isEmpty()
    }

    @Test
    fun addCart() {
        val cart = ShoppingCart(2, "cart2")
        cartRepository.addCart(cart)

        val carts = cartRepository.getCarts().getOrAwaitValue()
        assertThat(carts).contains(cart)
    }

    @Test
    fun updateCart() {
        val cart = ShoppingCart(defaultCart)
        cart.name = "cart3"

        cartRepository.updateCart(cart)

        val carts = cartRepository.getCarts().getOrAwaitValue()
        assertThat(carts).containsExactly(cart)
    }
}