package io.benic.shoppinglist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var dark: Boolean = false
    private lateinit var themeSwitch: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getPreferences(Context.MODE_PRIVATE)
        dark = pref.getBoolean("dark", false)

        setTheme(if (dark) R.style.AppTheme_Dark else R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        themeSwitch = menu.findItem(R.id.theme_switch)

        themeSwitch.isChecked = dark

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.theme_switch -> {
                dark = !dark
                Log.i(TAG, "Switching theme to ${if (dark) "dark" else "light"}")
                themeSwitch.isChecked = dark
                val pref = getPreferences(Context.MODE_PRIVATE)
                with(pref.edit()) {
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
