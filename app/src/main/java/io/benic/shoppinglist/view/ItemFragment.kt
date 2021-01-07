package io.benic.shoppinglist.view

import android.os.Bundle
import android.os.Handler
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private lateinit var itemList: RecyclerView
    private lateinit var itemCount: TextView
    private lateinit var totalCost: TextView
    private lateinit var itemsSelected: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressCard: CardView
    private lateinit var totalValue: TextView
    private lateinit var maxCost: EditText
    private lateinit var cartTitle: EditText

    private lateinit var checkAll: MenuItem
    private lateinit var uncheckAll: MenuItem
    private lateinit var searchItem: MenuItem
    private lateinit var search: SearchView

    private lateinit var handler: Handler

    private lateinit var itemListAdapter: ItemRecycleAdapter

    private val viewModel: ItemViewModel by viewModels()

    private var unsavedChanges: Int = 0
    private var itemsInitialized: Boolean = false

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

        itemCount.text =
            resources.getQuantityString(R.plurals.item, cart.items.size).format(cart.items.size)

        Log.d(TAG, "init items ${cart.items.size}")
        itemListAdapter.changeData(cart.items)
    }

    private fun initializeCart() {
        if (!itemsInitialized) {
            itemsInitialized = true
            viewModel.fetchItems(cart.id).observe(viewLifecycleOwner, { items ->
                if (items.containsAll(cart.items) && cart.items.containsAll(items)) {
                    Log.i(TAG, "items loaded are the same as the ones in memory, skip.")
                } else {
                    cart.items.clear()
                    cart.items.addAll(items)
                    initializeItems()
                }
            })
        }

        if (cart.maxCost > 0) {
            maxCost.setText(CurrencyHelper.getString(cart.maxCost))
        }

        cartTitle.setText(cart.name)
        totalCost.text = CurrencyHelper.getString(cart.cost)
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

    private fun initView(view: View) {
        itemList = view.findViewById(R.id.item_list)

        itemList.layoutManager = LinearLayoutManager(requireContext())

        totalValue = view.findViewById(R.id.total_value)
        progressBar = view.findViewById(R.id.progress_bar)
        progressCard = view.findViewById(R.id.progress_card)
        itemsSelected = view.findViewById(R.id.items_selected)
        totalCost = view.findViewById(R.id.total_cost)
        maxCost = view.findViewById(R.id.maximum_cost)
        cartTitle = view.findViewById(R.id.cart_title_edit)
        itemCount = view.findViewById(R.id.total_items)

        createAdapter()
    }

    private fun initListeners() {
        maxCost.addTextChangedListener { t, w ->
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

        cartTitle.addTextChangedListener { t, _ ->
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

        initView(view)
        initListeners()

        val cartId = arguments?.getLong("cartId")!!

        if (cartId >= 0) {
            viewModel.fetchCart(cartId).observe(viewLifecycleOwner, { cart ->
                this.cart = cart
                viewModel.cart.removeObservers(viewLifecycleOwner)
                initializeCart()
            })
        } else {
            Log.i(TAG, "creating cart")
            cart = ShoppingCart()
            viewModel.createCart(cart).observe(viewLifecycleOwner, { id ->
                cart.id = id
                Log.d(TAG, "cart created with id ${cart.id}")
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

        val swipeHelper = SwipeHelper(handler) { remove, i ->
            val item = itemListAdapter.getItem(i)

            if (remove) {
                viewModel.deleteItem(item)

                cart.items.remove(item)
                updateProgress()

                (itemList.adapter as ItemRecycleAdapter).notifyItemRemoved(i)
            } else {
                (itemList.adapter as ItemRecycleAdapter).notifyItemChanged(i)
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
                true
            }
            R.id.uncheck_all -> {
                setItemsChecked(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setItemsChecked(checked: Boolean) {
        if (this::checkAll.isInitialized) {
            checkAll.isVisible = !checked
            uncheckAll.isVisible = checked
        }
        (itemList.adapter as ItemRecycleAdapter).setItemsChecked(checked)
    }

    private fun updateProgress() {
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

                setItemsChecked(checked.size == cart.items.size)

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

        itemCount.text =
            resources.getQuantityString(R.plurals.item, cart.items.size, cart.items.size)

        updateProgress()


        itemListAdapter.notifyItemInserted(cart.items.size - 1)
    }

    private fun save() {
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
        private const val TAG = "SecondFragment"
        private const val MAX_UNSAVED_CHANGES = 1
    }
}

