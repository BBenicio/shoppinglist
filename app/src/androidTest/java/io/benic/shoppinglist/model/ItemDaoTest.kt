package io.benic.shoppinglist.model

import android.database.sqlite.SQLiteConstraintException
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
class ItemDaoTest {

    @get:Rule
    val executorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var itemDao: ItemDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        itemDao = db.itemDao()

        val cartDao = db.shoppingCartDao()
        cartDao.insert(ShoppingCart(1))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert() {
        val item = Item(1, "item", 10, 2, true, 0, 1)
        itemDao.insert(item)

        val result = itemDao.getAll().getOrAwaitValue()
        assertThat(result).contains(item)
    }

    @Test
    fun insert_invalidCartId() {
        val item = Item(1, "item", 10, 2, true, 0, 0)
        try {
            itemDao.insert(item)
        } catch (constraintException: SQLiteConstraintException) {
        }

        val result = itemDao.getAll().getOrAwaitValue()
        assertThat(result).isEmpty()
    }

    @Test
    fun delete() {
        val item = Item(1, "item", 10, 2, true, 0, 1)
        itemDao.insert(item)

        itemDao.delete(item)
        val result = itemDao.getAll().getOrAwaitValue()
        assertThat(result).isEmpty()
    }

    @Test
    fun update() {
        val item = Item(1, "item", 10, 2, true, 0, 1)
        itemDao.insert(item)

        item.position = 2
        itemDao.update(item)

        val result = itemDao.getAll().getOrAwaitValue()
        assertThat(result).contains(item)
    }

    @Test
    fun getAll() {
        val item1 = Item(1, "item", 10, 2, true, 0, 1)
        itemDao.insert(item1)

        val item2 = Item(2, "itemB", 50, 1, false, 1, 1)
        itemDao.insert(item2)

        val result = itemDao.getAll().getOrAwaitValue()
        assertThat(result).containsExactly(item1, item2)
    }

    @Test
    fun getFromCart() {
        val item1 = Item(1, "item", 10, 2, true, 0, 1)
        itemDao.insert(item1)

        val result = itemDao.getFromCart(1).getOrAwaitValue()
        assertThat(result).contains(item1)
    }

    @Test
    fun getFromCart_invalidCartId() {
        val item1 = Item(1, "item", 10, 2, true, 0, 1)
        itemDao.insert(item1)

        val result = itemDao.getFromCart(0).getOrAwaitValue()
        assertThat(result).isEmpty()
    }
}