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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.adapter.AppAdapter
import com.codegames.farsroid.model.App
import com.codegames.farsroid.util.Parser
import com.codegames.farsroid.util.document
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.content_app_list.*
import kotlinx.coroutines.*
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

class AppListActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        mJob = Job()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        url = intent?.extras?.getString("url") ?: throw Throwable("Set url")
        title = intent?.extras?.getString("title") ?: ""

        aal_progressBar.show()

        var adapter: AppAdapter? = null

        launch(Dispatchers.IO) {

            adapter = AppAdapter(mutableListOf(), R.layout.item_app_horizontal, this@AppListActivity)
            adapter?.isPageable = true

            adapter?.fetchData = fetchData@{ page ->
                runOnUiThread {
                    aal_progressBar.show()
                }
                val data = try {
                    val html = service.appList(url, page).execute().document()
                    Parser.appList(html)
                } catch (t: NullPointerException) {
                    t.printStackTrace()
                    arrayOf<App>()
                } catch (t: UnknownHostException) {
                    t.printStackTrace()
                    runOnUiThread {
                        aal_tryAgain.visibility = View.VISIBLE
                    }
                    null
                }

                runOnUiThread {
                    aal_progressBar.hide()
                }

                return@fetchData data
            }

            adapter?.onClick = {
                Intent(this@AppListActivity, AppPageActivity::class.java).apply {
                    putExtra("url", it.link)
                    putExtra("name", it.name)
                    startActivity(this)
                }
            }

            aal_recyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {
                aal_recyclerView.addItemDecoration(
                    DividerItemDecoration(
                        this@AppListActivity, DividerItemDecoration.VERTICAL
                    )
                )
                aal_recyclerView.adapter = adapter
                aal_recyclerView.layoutManager =
                    LinearLayoutManager(this@AppListActivity, RecyclerView.VERTICAL, false)

                adapter?.fetchData()

            }

        }

        aal_tryAgain.setOnClickListener {
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

}
