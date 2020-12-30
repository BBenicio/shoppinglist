package io.benic.shoppinglist

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ComplexColorCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import java.text.NumberFormat
import java.util.*
import kotlin.Comparator
import kotlin.concurrent.thread
import kotlin.text.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private lateinit var cart: ShoppingCart
    private lateinit var itemList: RecyclerView
    private lateinit var itemCount: TextView
    private lateinit var totalCost: TextView
    private lateinit var itemsSelected: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressCard: CardView
    private lateinit var totalValue: TextView
    private lateinit var maxCost: EditText

    private lateinit var handler: Handler

    private lateinit var itemListAdapter: ItemRecycleAdapter

    private var unsavedChanges: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        handler = Handler(requireActivity().mainLooper)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cartId = arguments?.getLong("cartId")!!

        if (cartId >= 0)
            cart = ShoppingCart.Carts.get(cartId)!!
        else {
            if (Shared.cart != null) {
                Log.i(TAG, "recovering cart")
                cart = Shared.cart!!
            } else {
                Log.i(TAG, "creating cart")
                cart = ShoppingCart()
                ShoppingCart.Carts.carts.add(cart)
                Shared.insert(cart)
                Log.d(TAG, "cart created with id ${cart.id}")
                Shared.cart = cart
            }
        }

        for (i in 0 until cart.items.size) {
            if (cart.items[i].position < 0) {
                cart.items[i].position = i
                ++unsavedChanges
            }
        }

        cart.items.sortWith(Comparator { a, b -> if (a.position > b.position) 1 else -1 })
        for (i in 0 until cart.items.size) {
            if (cart.items[i].position != i) {
                cart.items[i].position = i
                ++unsavedChanges
            }
        }

        itemList = view.findViewById(R.id.item_list)

        itemList.layoutManager = LinearLayoutManager(activity)

        totalValue = view.findViewById(R.id.total_value)
        progressBar = view.findViewById(R.id.progress_bar)
        progressCard = view.findViewById(R.id.progress_card)
        itemsSelected = view.findViewById(R.id.items_selected)
        totalCost = view.findViewById(R.id.total_cost)
        maxCost = view.findViewById(R.id.maximum_cost)

        if (cart.maxCost > 0) {
            maxCost.setText(CurrencyHelper.getString(cart.maxCost))
        }

        maxCost.addTextChangedListener{ t, w ->
            val cost = CurrencyHelper.getValue(t)

            maxCost.removeTextChangedListener(w)

            cart.maxCost = cost
            if (cost == 0) {
                maxCost.setText("")
                updateProgress()
            } else {
                val formatted = CurrencyHelper.getString(cart.maxCost)
                maxCost.setText(formatted)
                maxCost.setSelection(formatted.length)

                updateProgress()
            }

            maxCost.addTextChangedListener(w)
        }

        val cartTitle = view.findViewById<EditText>(R.id.cart_title_edit)
        cartTitle.setText(cart.name)
        cartTitle.addTextChangedListener { t,_ ->
            if (cart.name != t) {
                cart.name = t
//                Shared.update(cart)
            }
        }

        progressCard.setOnClickListener { _ ->
            findNavController().popBackStack()
        }

        itemCount = view.findViewById(R.id.total_items)
        itemCount.text = resources.getQuantityString(R.plurals.item, cart.items.size).format(cart.items.size)

        thread {
            val cost = if (cart.items.size > 0)
                cart.items.map { item -> item.price}
                    .reduce { acc, price -> acc + price }
            else 0
            handler.post { view.findViewById<TextView>(R.id.total_value).text = CurrencyHelper.getString(cost) }
        }

        itemListAdapter = ItemRecycleAdapter(cart.items,
            { i, text ->
                val item = itemListAdapter.getItem(i)
                val q = if (text.isEmpty()) 0 else text.toInt()
                if (item.quantity != q) {
                    item.quantity = q

                    updateProgress()

//                    Shared.update(cart.items[i])
                }
            },
            { i, text ->
                val item = itemListAdapter.getItem(i)
                if (item.name != text) {
                    item.name = text

//                    Shared.update(cart.items[i])
                }
            },
            { i, text, w, priceEdit ->
                val item = itemListAdapter.getItem(i)
                priceEdit.removeTextChangedListener(w)

                item.price = CurrencyHelper.getValue(text)
                val formatted = CurrencyHelper.getString(item.price)
                priceEdit.setText(formatted)

                priceEdit.setSelection(formatted.length)

                priceEdit.addTextChangedListener(w)

                updateProgress()

//                Shared.update(cart.items[i])
            },
            { i, check ->
                val item = itemListAdapter.getItem(i)
                item.checked = check
                updateProgress()

//                Shared.update(cart.items[i])
            })

        itemList.adapter = itemListAdapter

        val swipeHelper = SwipeHelper(handler) { remove, i ->
            val item = itemListAdapter.getItem(i)
//            val item = cart.items[i]

            if (remove) {
                Shared.delete(item)

                cart.items.remove(item)
                updateProgress()

//                (itemList.adapter as ItemRecycleAdapter).notifyDataSetChanged()
                (itemList.adapter as ItemRecycleAdapter).notifyItemRemoved(i)
            } else {
                (itemList.adapter as ItemRecycleAdapter).notifyItemChanged(i)
            }
        }
        swipeHelper.setAdapter(itemListAdapter)

        swipeHelper.setUpAnimationDecoratorHelper(itemList)
        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(itemList)

        (activity as MainActivity).setMenuItemsVisible(true)
        (activity as MainActivity).setItemsChecked(cart.items.filter { item -> !item.checked }.isEmpty())
        (activity as MainActivity).search.setOnQueryTextListener(itemList.adapter as ItemRecycleAdapter)

        Shared.current = 1
        Shared.frag = this
    }

    fun setItemsChecked(checked: Boolean) {
        (itemList.adapter as ItemRecycleAdapter).setItemsChecked(checked)
    }

    private fun updateProgress() {
        thread {
            val checked = cart.items.filter { item -> item.checked }

            val progress = checked.size
            val progressMax = cart.items.size

            val itemSelectedText = resources.getQuantityString(R.plurals.item, checked.size).format(checked.size)

            val totalCostVal = if (checked.isNotEmpty()) checked.map { item -> item.price * item.quantity }.reduce { acc, price -> acc + price } else 0

            val totalValueVal = if (cart.items.isNotEmpty()) cart.items.map { item -> item.price * item.quantity }.reduce { acc, price -> acc + price } else 0

            if (cart.maxCost in 1 until totalCostVal) {
                totalValue.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorOverSpent))
            } else {
                totalValue.setTextColor(ContextCompat.getColor(requireActivity(), R.color.textDarkSecondary))
            }

            Handler(Looper.getMainLooper()).post {
                (activity as MainActivity).setItemsChecked(checked.size == cart.items.size)

                itemsSelected.text = itemSelectedText
                totalCost.text = CurrencyHelper.getString(totalCostVal)
                totalValue.text = CurrencyHelper.getString(totalValueVal)

                progressBar.progress = progress
                progressBar.max = progressMax

                if (++unsavedChanges > MAX_UNSAVED_CHANGES) {
                    save()
                }

            }
        }
    }

    fun addItem() {
        Log.i(TAG, "add item to cart ${cart.id}")

        val item = Item(cartId = cart.id)
        item.position = cart.items.size
        cart.items.add(item)

        Shared.insert(item)

        itemCount.text = resources.getQuantityString(R.plurals.item, cart.items.size, cart.items.size)

        updateProgress()

        itemList.adapter!!.notifyItemInserted(cart.items.size - 1)
//        itemList.adapter!!.notifyDataSetChanged()
    }

    private fun save() {
        unsavedChanges = 0
        Shared.update(cart)
        for (item in cart.items) {
            Shared.update(item)
        }
    }

    override fun onDestroyView() {
        save()

        super.onDestroyView()
    }

    companion object {
        private const val TAG = "SecondFragment"
        private const val MAX_UNSAVED_CHANGES = 10
    }
}

