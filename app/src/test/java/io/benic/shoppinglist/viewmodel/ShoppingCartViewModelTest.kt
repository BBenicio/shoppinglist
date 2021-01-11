package io.benic.shoppinglist.viewmodel

import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.repository.ItemRepository
import io.benic.shoppinglist.repository.ShoppingCartRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations

class ShoppingCartViewModelTest {

    @Mock
    private lateinit var itemRepository: ItemRepository

    @Mock
    private lateinit var cartRepository: ShoppingCartRepository

    private lateinit var cartViewModel: ShoppingCartViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        cartViewModel = ShoppingCartViewModel(cartRepository, itemRepository)
    }

    @Test
    fun remove() {
        val cart = ShoppingCart(name = "cart")

        cartViewModel.remove(cart)

        verify(cartRepository).removeCart(cart)
        verifyNoMoreInteractions(cartRepository)
    }

    @Test
    fun add() {
        val cart = ShoppingCart(name = "cart")

        cartViewModel.add(cart)

        verify(cartRepository).addCart(cart)
        verifyNoMoreInteractions(cartRepository)
    }

    @Test
    fun getItemsFromCart() {
        val cart = ShoppingCart(id = 1, name = "cart")

        cartViewModel.getItemsFromCart(cart)

        verify(itemRepository).getItems(cart.id)
        verifyNoMoreInteractions(itemRepository)
    }

    @Test
    fun addItemsToCart() {
        val cart = ShoppingCart(id = 1, name = "cart")
        val items = listOf(Item(id = 0, name = "a"), Item(id = 2, name = "c"))

        cartViewModel.addItemsToCart(cart, items)

        verify(itemRepository).addItem(items[0])
        verify(itemRepository).addItem(items[1])
        verifyNoMoreInteractions(itemRepository)
    }
}