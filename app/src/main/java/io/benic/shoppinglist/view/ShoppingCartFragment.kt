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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var cartsList: RecyclerView

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
                carts.isEmpty() -> {
                }
                else -> {
                    adapter.changeItemAt(adapter.selected, carts[adapter.selected])
                }
            }
        })

        handler = Handler(requireActivity().mainLooper)

        cartsList = view.findViewById(R.id.carts_list)
        cartsList.adapter = createCartRecycleAdapter(listOf())

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        cartsList.layoutManager = LinearLayoutManager(activity)

        val swipeHelper = SwipeHelper(handler) { remove, i ->
            val adapter = (cartsList.adapter as ShoppingCartRecycleAdapter)
            val cart = adapter.data[i]
            adapter.selected = i

            if (remove) {
                viewModel.remove(cart)
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
        return ShoppingCartRecycleAdapter(mut,
            { c ->
                val bundle = bundleOf("cartId" to c.id)
                findNavController().navigate(
                    R.id.action_ShoppingCartFragment_to_ItemFragment,
                    bundle
                )
            },
            { c ->
                Log.i(TAG, "creating cart ${c.name}")

                c.id = 0
                viewModel.add(c)
            }
        )
    }

    private fun addCart() {
        findNavController().navigate(R.id.action_ShoppingCartFragment_to_ItemFragment)
    }

    companion object {
        private const val TAG = "FirstFragment"
    }
}
