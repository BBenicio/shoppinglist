package io.benic.shoppinglist.viewmodel

import androidx.lifecycle.MutableLiveData
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.repository.ItemRepository
import io.benic.shoppinglist.repository.ShoppingCartRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ItemViewModelTest {

    @Mock
    private lateinit var itemRepository: ItemRepository

    @Mock
    private lateinit var cartRepository: ShoppingCartRepository

    private lateinit var itemViewModel: ItemViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        itemViewModel = ItemViewModel(itemRepository, cartRepository)

        `when`(itemRepository.getItems(anyLong())).thenReturn(MutableLiveData())
        `when`(cartRepository.getCart(anyInt())).thenReturn(MutableLiveData())
    }

    @Test
    fun fetchItems() {
        itemViewModel.fetchItems(0)

        verify(itemRepository).getItems(0)
        verifyNoMoreInteractions(itemRepository)
    }

    @Test
    fun fetchCart() {
        itemViewModel.fetchCart(0)

        verify(cartRepository).getCart(0)
        verifyNoMoreInteractions(cartRepository)
    }

    @Test
    fun createCart() {
        val cart = ShoppingCart(name = "cart")
        itemViewModel.createCart(cart)

        verify(cartRepository).addCart(cart)
        verifyNoMoreInteractions(cartRepository)
    }

    @Test
    fun addItem() {
        val item = Item(name = "item")
        itemViewModel.addItem(item)

        verify(itemRepository).addItem(item)
        verifyNoMoreInteractions(itemRepository)
    }

    @Test
    fun updateCart() {
        val cart = ShoppingCart(name = "cart")
        itemViewModel.updateCart(cart)

        verify(cartRepository).updateCart(cart)
        verifyNoMoreInteractions(cartRepository)
    }

    @Test
    fun updateItem() {
        val item = Item(name = "item")
        itemViewModel.updateItem(item)

        verify(itemRepository).updateItem(item)
        verifyNoMoreInteractions(itemRepository)
    }

    @Test
    fun deleteItem() {
        val item = Item(name = "item")
        itemViewModel.deleteItem(item)

        verify(itemRepository).deleteItem(item)
        verifyNoMoreInteractions(itemRepository)
    }
}