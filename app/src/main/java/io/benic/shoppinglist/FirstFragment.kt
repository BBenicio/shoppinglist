package io.benic.shoppinglist

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.model.Item
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    // https://github.com/android/views-widgets-samples/blob/master/RecyclerViewKotlin/app/src/main/java/com/example/android/recyclerview/RecyclerViewFragment.kt
//    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var handler:Handler
    private lateinit var cartsList:RecyclerView

    private fun whenDatabaseReady(cb: ()->Unit) {
        thread {
            while (!Shared.databaseReady) {
                Thread.sleep(100)
            }

            handler.post(cb)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Shared.cart = null
        handler = Handler(requireActivity().mainLooper)

        cartsList = view.findViewById<RecyclerView>(R.id.carts_list)

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        cartsList.layoutManager = LinearLayoutManager(activity)

        cartsList.adapter = CartRecycleAdapter(ShoppingCart.Carts.carts,
            { i ->
                val bundle = bundleOf("cartId" to ShoppingCart.Carts.carts[i].id)
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
            },
            { i ->
                val id = ShoppingCart.Carts.carts[i].id
                val cart = ShoppingCart.Carts.get(id)!!
                Log.i(TAG, "deleting cart $id")

                ShoppingCart.Carts.carts.remove(cart)

                Shared.delete(cart)

                cartsList.adapter!!.notifyItemRemoved(i)
//                cartsList.adapter!!.notifyDataSetChanged()
            },
            { i ->
                Log.i(TAG, "creating cart ${ShoppingCart.Carts.carts[i].name}")

                val cart = ShoppingCart(ShoppingCart.Carts.carts[i])
                cart.id = 0
                ShoppingCart.Carts.carts.add(cart)
                Shared.insert(cart)
                cartsList.adapter!!.notifyItemInserted(ShoppingCart.Carts.carts.size)
            }
        )

        val swipeHelper = SwipeHelper(handler) { remove, i ->
            val cart = ShoppingCart.Carts.carts[i]

            if (remove) {
                Shared.delete(cart)

                ShoppingCart.Carts.carts.remove(cart)

                (cartsList.adapter as CartRecycleAdapter).notifyItemRemoved(i)
            } else {
                (cartsList.adapter as CartRecycleAdapter).notifyItemChanged(i)
            }
        }

        swipeHelper.setUpAnimationDecoratorHelper(cartsList)
        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(cartsList)

        whenDatabaseReady {
            Log.i(TAG, "database ready, notify dataset changed")
            (cartsList.adapter as CartRecycleAdapter).notifyDataSetChanged()

            if (cartsList.isEmpty() && ShoppingCart.Carts.carts.isNotEmpty()) {
                requireActivity().recreate()
            }
        }

        (activity as MainActivity).setMenuItemsVisible(false)

        Shared.current = 0
        Shared.frag = this
    }

    fun addCart() {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
    }

    companion object {
        private const val TAG = "FirstFragment"
    }
}
