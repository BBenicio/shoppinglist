package io.benic.shoppinglist.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.benic.shoppinglist.model.AppDatabase
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.testing.getOrAwaitValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = 28)
class ItemRepositoryTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private lateinit var itemRepository: ItemRepository
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val cartDao = db.shoppingCartDao()
        cartDao.insert(ShoppingCart(1))

        val itemDao = db.itemDao()
        itemDao.insert(Item(id = 1, name = "item", cartId = 1))

        itemRepository = ItemRepository(itemDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getItems() {
        val items = itemRepository.getItems(1).getOrAwaitValue()
        assertThat(items).containsExactly(Item(id = 1, name = "item", cartId = 1))
    }

    @Test
    fun addItem() {
        val id = itemRepository.addItem(Item(name = "item2", cartId = 1)).getOrAwaitValue()
        assertThat(id).isEqualTo(2)
    }

    @Test
    fun updateItem() {
        val item = Item(id = 1, name = "item1", cartId = 1)
        itemRepository.updateItem(item)

        val items = itemRepository.getItems(1).getOrAwaitValue()
        assertThat(items).containsExactly(item)
    }

    @Test
    fun deleteItem() {
        val item = Item(id = 1, name = "item", cartId = 1)
        itemRepository.deleteItem(item)

        val items = itemRepository.getItems(1).getOrAwaitValue()
        assertThat(items).isEmpty()
    }
}