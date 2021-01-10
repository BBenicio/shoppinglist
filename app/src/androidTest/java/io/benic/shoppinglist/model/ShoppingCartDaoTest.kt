package io.benic.shoppinglist.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import getOrAwaitValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ShoppingCartDaoTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var cartDao: ShoppingCartDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        cartDao = db.shoppingCartDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert() {
        val cart = ShoppingCart(1, "cart", 1, arrayListOf(), 30, "a\nb")
        cartDao.insert(cart)

        val result = cartDao.getAll().getOrAwaitValue()
        assertThat(result).contains(cart)
    }

    @Test
    fun delete() {
        val cart = ShoppingCart(1, "cart", 1, arrayListOf(), 30, "a\nb")
        cartDao.insert(cart)

        cartDao.delete(cart)

        val result = cartDao.getAll().getOrAwaitValue()
        assertThat(result).isEmpty()
    }

    @Test
    fun update() {
        val cart = ShoppingCart(1, "cart", 1, arrayListOf(), 30, "a\nb")
        cartDao.insert(cart)

        cart.cost = 510
        cartDao.update(cart)

        val result = cartDao.getFromId(1).getOrAwaitValue()
        assertThat(result).isEqualTo(cart)
    }

    @Test
    fun getAll() {
        val cart1 = ShoppingCart(1, "cart", 1, arrayListOf(), 30, "a\nb")
        cartDao.insert(cart1)
        val cart2 = ShoppingCart(2, "cartB", 5, arrayListOf(), 40, "a\nb")
        cartDao.insert(cart2)

        val result = cartDao.getAll().getOrAwaitValue()
        assertThat(result).containsExactly(cart1, cart2)
    }

    @Test
    fun getFromId() {
        val cart = ShoppingCart(1, "cart", 1, arrayListOf(), 30, "a\nb")
        cartDao.insert(cart)

        val result = cartDao.getFromId(1).getOrAwaitValue()
        assertThat(result).isEqualTo(cart)
    }
}