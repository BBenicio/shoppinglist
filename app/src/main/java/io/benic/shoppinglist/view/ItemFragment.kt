package io.benic.shoppinglist.view

import android.os.Bundle
import android.os.Handler
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.benic.shoppinglist.R
import io.benic.shoppinglist.model.Item
import io.benic.shoppinglist.model.ShoppingCart
import io.benic.shoppinglist.utils.CurrencyHelper
import io.benic.shoppinglist.viewmodel.ItemViewModel
import kotlinx.android.synthetic.main.fragment_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class ItemFragment : Fragment() {

    private var cart: ShoppingCart = ShoppingCart()


    private lateinit var checkAll: MenuItem
    private lateinit var uncheckAll: MenuItem
    private lateinit var searchItem: MenuItem
    private lateinit var search: SearchView

    private lateinit var handler: Handler

    private lateinit var itemListAdapter: ItemRecycleAdapter

    private val viewModel: ItemViewModel by viewModels()

    private var unsavedChanges: Int = 0
    private var itemsInitialized: Boolean = false
    private var isInitializing: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        handler = Handler(requireActivity().mainLooper)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item, container, false)
    }

    private fun initializeItems() {
        for (i in 0 until cart.items.size) {
            if (cart.items[i].position < 0) {
                cart.items[i].position = i
                ++unsavedChanges
            }
        }

        cart.items.sortWith { a, b -> if (a.position > b.position) 1 else -1 }
        for (i in 0 until cart.items.size) {
            if (cart.items[i].position != i) {
                cart.items[i].position = i
                ++unsavedChanges
            }
        }

        totalItems.text =
            resources.getQuantityString(R.plurals.item, cart.items.size).format(cart.items.size)

        itemListAdapter.changeData(cart.items)

        isInitializing = false
        updateProgress()
    }

    private fun initializeCart() {
        if (!itemsInitialized) {
            itemsInitialized = true
            viewModel.fetchItems(cart.id).observe(viewLifecycleOwner, { items ->
                if (cart.items.isEmpty()) {
                    Log.i(TAG, "showing items")
                    cart.items.addAll(items)
                    initializeItems()
                } else {
                    Log.i(TAG, "won't reload items")
                }
            })
        }

        if (cart.maxCost > 0) {
            maximumCost.setText(CurrencyHelper.getString(cart.maxCost))
        }

        cartTitleEdit.setText(cart.name)
        totalValue.text = CurrencyHelper.getString(cart.cost)
    }

    private fun createAdapter() {
        itemListAdapter = ItemRecycleAdapter(cart.items, object : ItemRecycleListener {
            override fun onQuantityChanged(position: Int, text: String) {
                val item = itemListAdapter.getItem(position)
                val q = if (text.isEmpty()) 0 else text.toInt()
                if (item.quantity != q) {
                    item.quantity = q

                    updateProgress()
                }
            }

            override fun onNameChanged(position: Int, text: String) {
                val item = itemListAdapter.getItem(position)
                if (item.name != text) {
                    item.name = text

                    updateProgress()
                }
            }

            override fun onPriceChanged(
                position: Int,
                text: String,
                textWatcher: TextWatcher,
                editText: EditText
            ) {
                val item = itemListAdapter.getItem(position)
                editText.removeTextChangedListener(textWatcher)

                item.price = CurrencyHelper.getValue(text)
                val formatted = CurrencyHelper.getString(item.price)
                editText.setText(formatted)

                editText.setSelection(formatted.length)

                editText.addTextChangedListener(textWatcher)

                updateProgress()
            }

            override fun onChecked(position: Int, checked: Boolean) {
                val item = itemListAdapter.getItem(position)
                item.checked = checked

                updateProgress()
            }

            override fun onItemMoved(from: Int, to: Int) {
                updateProgress()
            }
        })

        itemList.adapter = itemListAdapter
    }

    private fun initView() {
        itemList.layoutManager = LinearLayoutManager(requireContext())

        createAdapter()
    }

    private fun initListeners() {
        maximumCost.addTextChangedListener { t, w ->
            val cost = CurrencyHelper.getValue(t)

            maximumCost.removeTextChangedListener(w)

            cart.maxCost = cost
            if (cost == 0) {
                maximumCost.setText("")
                updateProgress()
            } else {
                val formatted = CurrencyHelper.getString(cart.maxCost)
                maximumCost.setText(formatted)
                maximumCost.setSelection(formatted.length)

                updateProgress()
            }

            maximumCost.addTextChangedListener(w)
        }

        cartTitleEdit.addTextChangedListener { t, _ ->
            if (cart.name != t) {
                cart.name = t
            }
        }

        progressCard.setOnClickListener {
            findNavController().popBackStack()
        }

        fab.setOnClickListener {
            addItem()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initListeners()

        val cartId = arguments?.getLong("cartId") ?: 0L

        if (cartId >= 0) {
            viewModel.fetchCart(cartId).observe(viewLifecycleOwner, { cart ->
                Log.i(TAG, "loaded cart ${cart.name}")
                this.cart = cart
                viewModel.cart.removeObservers(viewLifecycleOwner)
                initializeCart()
            })
        } else {
            Log.i(TAG, "creating cart")
            cart = ShoppingCart()
            viewModel.createCart(cart).observe(viewLifecycleOwner, { id ->
                cart.id = id
                initializeCart()
            })
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val cost = if (cart.items.size > 0)
                cart.items.map { item -> item.price }
                    .reduce { acc, price -> acc + price }
            else 0
            withContext(Dispatchers.Main) {
                totalValue.text = CurrencyHelper.getString(cost)
            }
        }

        val swipeHelper = SwipeHelper(lifecycleScope) { remove, i ->
            val item = itemListAdapter.getItem(i)

            if (remove) {
                viewModel.deleteItem(item)

                cart.items.remove(item)
                updateProgress()

                itemListAdapter.notifyItemRemoved(i)
            } else {
                itemListAdapter.notifyItemChanged(i)
            }
        }
        swipeHelper.setAdapter(itemListAdapter)

        swipeHelper.setUpAnimationDecoratorHelper(itemList)
        val itemTouchHelper = ItemTouchHelper(swipeHelper)
        itemTouchHelper.attachToRecyclerView(itemList)

        setItemsChecked(cart.items.none { item -> !item.checked })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_item, menu)

        checkAll = menu.findItem(R.id.check_all)
        uncheckAll = menu.findItem(R.id.uncheck_all)
        searchItem = menu.findItem(R.id.app_bar_search)

        search = searchItem.actionView as SearchView
        search.setOnQueryTextListener(itemList.adapter as ItemRecycleAdapter)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.check_all -> {
                setItemsChecked(true)
                setShowCheckAllActionInMenu(false)
                true
            }
            R.id.uncheck_all -> {
                setItemsChecked(false)
                setShowCheckAllActionInMenu(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setShowCheckAllActionInMenu(showChecked: Boolean) {
        if (this::checkAll.isInitialized) {
            checkAll.isVisible = showChecked
            uncheckAll.isVisible = !showChecked
        }
    }

    private fun setItemsChecked(checked: Boolean) {
        (itemList.adapter as ItemRecycleAdapter).setItemsChecked(checked)
    }

    private fun updateProgress() {
        Log.d(TAG, "updateProgress()")

        if (isInitializing) {
            Log.i(TAG, "still initializing so won't update progress")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val checked = cart.items.filter { item -> item.checked }

            val progress = checked.size
            val progressMax = cart.items.size

            val totalCostVal =
                if (checked.isNotEmpty()) checked.map { item -> item.price * item.quantity }
                    .reduce { acc, price -> acc + price } else 0

            val totalValueVal =
                if (cart.items.isNotEmpty()) cart.items.map { item -> item.price * item.quantity }
                    .reduce { acc, price -> acc + price } else 0

            cart.cost = totalValueVal
            cart.description =
                cart.items.subList(0, if (cart.items.size > 3) 3 else cart.items.size)
                    .joinToString("\n") { item -> item.name }

            if (++unsavedChanges > MAX_UNSAVED_CHANGES) {
                save()
            }

            withContext(Dispatchers.Main) {
                if (cart.maxCost in 1 until totalCostVal) {
                    totalValue.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.colorOverSpent
                        )
                    )
                } else {
                    totalValue.setTextColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.textDarkSecondary
                        )
                    )
                }

                setShowCheckAllActionInMenu(checked.size < cart.items.size)

                itemsSelected.text =
                    resources.getQuantityString(R.plurals.item, checked.size).format(checked.size)
                totalCost.text = CurrencyHelper.getString(totalCostVal)
                totalValue.text = CurrencyHelper.getString(totalValueVal)

                progressBar.progress = progress
                progressBar.max = progressMax
            }
        }
    }

    private fun addItem() {
        Log.i(TAG, "add item to cart ${cart.id}")

        val item = Item(cartId = cart.id)
        item.position = cart.items.size
        cart.items.add(item)

        viewModel.addItem(item).observe(viewLifecycleOwner, { id -> item.id = id })

        totalItems.text =
            resources.getQuantityString(R.plurals.item, cart.items.size, cart.items.size)

        updateProgress()


        itemListAdapter.notifyItemInserted(cart.items.size - 1)
    }

    private fun save() {
        Log.d(TAG, "save ${cart.name} (${cart.items.size} items)")
        unsavedChanges = 0
        viewModel.updateCart(cart)
        for (item in cart.items) {
            viewModel.updateItem(item)
        }
    }

    override fun onDestroyView() {
        save()

        super.onDestroyView()
    }

    companion object {
        private const val TAG = "ItemFragment"
        private const val MAX_UNSAVED_CHANGES = 0
    }
}

