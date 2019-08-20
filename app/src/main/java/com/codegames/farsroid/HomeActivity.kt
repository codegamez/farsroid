package com.codegames.farsroid

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import com.codegames.farsroid.util.ToolbarListener
import com.codegames.farsroid.util.ViewPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import lib.codegames.debug.LogCG
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import kotlin.coroutines.CoroutineContext

class HomeActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mJob = Job()
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        toolbar.setOnClickListener {
            if (searchView?.isIconified == true) {
                searchView?.isIconified = false
            }
        }

        Store.getMainpageAsync(this)

        val adapter = ViewPagerAdapter(supportFragmentManager).apply {
            addFragment(GamesFragment.newInstance(), getString(R.string.games))
            addFragment(AppsFragment.newInstance(), getString(R.string.apps))
        }

        am_viewPager.adapter = adapter
        am_tabLayout.setupWithViewPager(am_viewPager)

        am_appBar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                adapter.mFragmentList.forEach {
                    (it as? ToolbarListener)?.onOffsetChange(toolbar.height, verticalOffset)
                }
            }
        )

        launch(Dispatchers.IO) {
            database.appDao().getAll().forEach {
                LogCG.d(it)
            }
        }

    }

    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            findViewById<View>(R.id.search_plate)?.let {
                it.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            KeyboardVisibilityEvent.setEventListener(this@HomeActivity) {
                if (!it) searchView?.onActionViewCollapsed()
            }

        }


        return true
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) ->
                drawer_layout.closeDrawer(GravityCompat.START)
            searchView?.isIconified == false -> searchView?.onActionViewCollapsed()
            else -> super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_history -> {
                Intent(this, HistoryActivity::class.java).let {
                    startActivity(it)
                }
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
