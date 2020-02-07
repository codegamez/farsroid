package com.codegames.farsroid

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.adapter.AppAdapter
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.content_history.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class HistoryActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private var adapter: AppAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        mJob = Job()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.last_seen)

        ah_progressBar.show()

        launch(Dispatchers.IO) {

            adapter = AppAdapter(mutableListOf(), R.layout.item_app_horizontal, this@HistoryActivity)
            adapter?.isPageable = false

            adapter?.onClick = { app, holder ->
                Intent(this@HistoryActivity, AppPageActivity::class.java).apply {
                    putExtra("url", app.link)
                    putExtra("name", app.name)
                    putExtra("imageUrl", app.imageUrl)
                    startActivity(
                        this, ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@HistoryActivity,
                            holder.iconContainer!!, "app_icon"
                        ).toBundle()
                    )
                }
            }

            ah_recyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {
                ah_recyclerView.addItemDecoration(
                    DividerItemDecoration(
                        this@HistoryActivity, DividerItemDecoration.VERTICAL
                    )
                )
                ah_recyclerView.adapter = adapter
                ah_recyclerView.layoutManager =
                    LinearLayoutManager(this@HistoryActivity, RecyclerView.VERTICAL, false)

                ah_progressBar.hide()

            }

        }.let {
            SyncJob.add(JOB_HISTORY_SETUP, it)
        }

        ah_tryAgain.setOnClickListener {
            it.visibility = View.GONE
            adapter?.fetchData()
        }

    }

    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            findViewById<View>(R.id.search_plate)?.let {
                it.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            findViewById<View>(R.id.search_mag_icon)?.let {
                it.layoutParams.width = 0
            }

        }

        return true
    }

    override fun onResume() {
        super.onResume()

        SyncJob.get(JOB_HISTORY_SETUP)?.invokeOnCompletion {
            launch(Dispatchers.IO) {
                adapter?.apply {
                    appList.clear()
                    appList.addAll(database.appDao().getAll())
                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (searchView?.isIconified == false) {
            searchView?.onActionViewCollapsed()
        } else
            super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

    companion object {
        private const val JOB_HISTORY_SETUP = "job-history-setup"
    }

}
