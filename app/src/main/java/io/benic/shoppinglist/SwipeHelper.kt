package io.benic.shoppinglist

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Runnable


class SwipeHelper(private val mainLooperHandler: Handler, private val callback : (Boolean, Int)->Unit) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

    companion object {
        private const val TAG = "SwipeHelper"
        private const val UNDO_TIME = 1500L
    }

    private var itemAdapter: ItemRecycleAdapter? = null
    private var cartAdapter: CartRecycleAdapter? = null

    private val deleteBackground:Drawable = ColorDrawable(Color.RED)

    private var pending: Runnable? = null
    private var isSwiping: Boolean = false

    fun setAdapter(adapter: ItemRecycleAdapter) {
        itemAdapter = adapter
    }

    fun setAdapter(adapter: CartRecycleAdapter) {
        cartAdapter = adapter
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (itemAdapter != null) {
            if (!(itemAdapter!!.isFiltered())) {
                itemAdapter!!.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }
        }
        return cartAdapter != null
    }



    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val i = viewHolder.adapterPosition
        Log.i(TAG, "swiped on item $i")

        setDefaultSwipeDirs(0)

        pending = Runnable {
            callback.invoke(true, i)
            pending = null

            setDefaultSwipeDirs(ItemTouchHelper.LEFT)
        }
        mainLooperHandler.postDelayed(pending, UNDO_TIME)

        Snackbar.make(viewHolder.itemView, R.string.item_deleted, Snackbar.LENGTH_INDEFINITE).setAction(R.string.undo) { _ ->
            Log.i(TAG, "undoing")

            mainLooperHandler.removeCallbacks(pending)
            pending = null

            callback.invoke(false, i)

            setDefaultSwipeDirs(ItemTouchHelper.LEFT)
        }.setDuration(UNDO_TIME.toInt()).show()
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     * https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete
     */
    public fun setUpAnimationDecoratorHelper(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(object : ItemDecoration() {
            // we want to cache this and not allocate anything repeatedly in the onDraw method
            val background: Drawable = deleteBackground

            override fun onDraw(
                c: Canvas,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                // only if animation is in progress
                if (parent.itemAnimator!!.isRunning && isSwiping) {
                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    var lastViewComingDown: View? = null
                    var firstViewComingUp: View? = null

                    // this is fixed
                    val left = 0
                    val right = parent.width

                    // this we need to find out
                    var top = 0
                    var bottom = 0

                    // find relevant translating views
                    val childCount = parent.layoutManager!!.childCount
                    for (i in 0 until childCount) {
                        val child: View = parent.layoutManager!!.getChildAt(i)!!

                        if (child.translationY < 0) { // view is coming down
                            lastViewComingDown = child
                        } else if (child.translationY > 0) { // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) { // views are coming down AND going up to fill the void
                        top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    } else if (lastViewComingDown != null) { // views are going down to fill the void
                        top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = lastViewComingDown.bottom
                    } else if (firstViewComingUp != null) { // views are coming up to fill the void
                        top = firstViewComingUp.top
                        bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    }

                    background.setBounds(left, top, right, bottom)
                    background.draw(c)
                }

                super.onDraw(c, parent, state)
            }
        })
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val view = viewHolder.itemView

        isSwiping = actionState == ItemTouchHelper.ACTION_STATE_SWIPE

        deleteBackground.setBounds(view.right + dX.toInt(), view.top, view.right, view.bottom)
        deleteBackground.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}