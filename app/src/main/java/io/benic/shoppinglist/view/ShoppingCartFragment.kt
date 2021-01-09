package io.benic.shoppinglist.view

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.benic.shoppinglist.R
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.viewmodel.ShoppingCartViewModel
import kotlinx.android.synthetic.main.fragment_shopping_cart.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class ShoppingCartFragment : Fragment() {
    private val viewModel: ShoppingCartViewModel by viewModels()

    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.carts.observe(viewLifecycleOwner, { carts: List<ShoppingCart> ->
            Log.i(TAG, "carts changed from ${cartsList.adapter?.itemCount} to ${carts.size}")

            cartsList.adapter = cartsList.adapter ?: createCartRecycleAdapter(carts)
            val adapter = cartsList.adapter as ShoppingCartRecycleAdapter
            when {
                adapter.itemCount == 0 && carts.size > 1 -> {
                    adapter.addData(carts)
                }
                carts.size > adapter.itemCount -> {
                    adapter.addCart(carts.last())
                }
                carts.size < adapter.itemCount -> {
                    adapter.removeCartAt(adapter.selected)
                }
                else -> {
                    Log.i(TAG, "carts change requires dataset change")
                    adapter.data.clear()
                    adapter.addData(carts)
                }
            }
        })

        handler = Handler(requireActivity().mainLooper)

        cartsList.adapter = createCartRecycleAdapter(listOf())

        cartsList.layoutManager = LinearLayoutManager(activity)

        val swipeHelper = SwipeHelper(lifecycleScope) { remove, i ->
            val adapter = (cartsList.adapter as ShoppingCartRecycleAdapter)
            val cart = adapter.data[i]
            adapter.selected = i

            if (remove) {
                viewModel.remove(cart)
            } else {
                adapter.notifyItemChanged(i)
            }
        }

        swipeHelper.setUpAnimationDecoratorHelper(cartsList)
        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(cartsList)

        fab.setOnClickListener {
            addCart()
        }
    }

    private fun createCartRecycleAdapter(carts: List<ShoppingCart>): ShoppingCartRecycleAdapter {
        Log.i(TAG, "creating cart adapter")
        val mut = mutableListOf<ShoppingCart>().apply {
            addAll(carts)
        }
        return ShoppingCartRecycleAdapter(mut, object : ShoppingCartRecycleListener {
            override fun onEdit(cart: ShoppingCart) {
                val bundle = bundleOf("cartId" to cart.id)
                findNavController().navigate(
                    R.id.action_ShoppingCartFragment_to_ItemFragment,
                    bundle
                )
            }

            override fun onDuplicate(cart: ShoppingCart) {
                Log.i(TAG, "creating cart ${cart.name}")
                val c = ShoppingCart(cart)
                c.id = 0

                viewModel.add(c).observe(viewLifecycleOwner, { id ->
                    c.id = id
                    val itemsFromCart = viewModel.getItemsFromCart(cart)
                    itemsFromCart.observe(viewLifecycleOwner, { items ->
                        Log.i(TAG, "adding ${items.size} items to cart ${c.name}")
                        itemsFromCart.removeObservers(viewLifecycleOwner)
                        viewModel.addItemsToCart(c, items)
                    })
                })
            }
        })
    }

    private fun addCart() {
        findNavController().navigate(R.id.action_ShoppingCartFragment_to_ItemFragment)
    }

    companion object {
        private const val TAG = "ShoppingCartFragment"
    }
}
