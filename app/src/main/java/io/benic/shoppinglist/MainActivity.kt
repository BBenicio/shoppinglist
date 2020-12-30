package io.benic.shoppinglist

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var dark:Boolean = false
    private lateinit var themeSwitch: MenuItem
    private lateinit var checkAll: MenuItem
    private lateinit var uncheckAll: MenuItem
    private lateinit var searchItem: MenuItem
    lateinit var search: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getPreferences(Context.MODE_PRIVATE)
        dark = pref.getBoolean("dark", false)
//        dark = Preferences.userRoot().getBoolean("dark", false)

        setTheme(if (dark) R.style.AppTheme_Dark else R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Shared.startDB(this)

        fab.setOnClickListener { view ->
            if (Shared.frag != null) {
                when (Shared.current) {
                    0 -> {
                        (Shared.frag as FirstFragment).addCart()
                    }
                    1 -> {
                        (Shared.frag as SecondFragment).addItem()
                    }
                }
            } else {
                Snackbar.make(view, "Something unexpected happened", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        Shared.database.close()
        Log.i(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        themeSwitch = menu.findItem(R.id.theme_switch)
        checkAll = menu.findItem(R.id.check_all)
        uncheckAll = menu.findItem(R.id.uncheck_all)
        searchItem = menu.findItem(R.id.app_bar_search)
        search = searchItem.actionView as SearchView

        themeSwitch.isChecked = dark

        return true
    }

    fun setMenuItemsVisible(visible: Boolean) {
        if (this::checkAll.isInitialized) {
            searchItem.isVisible = visible

            checkAll.isVisible = visible
            uncheckAll.isVisible = visible
        }
    }

    fun setItemsChecked(checked: Boolean) {
        if (this::checkAll.isInitialized) {
            checkAll.isVisible = !checked
            uncheckAll.isVisible = checked
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.check_all -> {
                (Shared.frag as SecondFragment).setItemsChecked(true)
                true
            }
            R.id.uncheck_all -> {
                (Shared.frag as SecondFragment).setItemsChecked(false)
                true
            }
            R.id.theme_switch -> {
                dark = !dark
                Log.i(TAG, "Switching theme to ${if (dark) "dark" else "light"}")
//                setTheme(if (dark) R.style.AppTheme_Dark else R.style.AppTheme)
                themeSwitch.isChecked = dark
//                Preferences.userRoot().putBoolean("dark", dark)
                val pref = getPreferences(Context.MODE_PRIVATE)
                with (pref.edit()) {
                    putBoolean("dark", dark)
                    commit()
                }

                recreate()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
