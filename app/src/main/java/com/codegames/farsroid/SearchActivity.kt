package com.codegames.farsroid

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
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
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.content_search.*
import kotlinx.coroutines.*
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext
import android.provider.SearchRecentSuggestions
import androidx.core.app.ActivityOptionsCompat


class SearchActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        mJob = Job()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleIntent(intent)

    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    private var rawQuery = ""
    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {

            rawQuery = intent.getStringExtra(SearchManager.QUERY)

            val suggestions = SearchRecentSuggestions(
                this,
                MySearchSuggestionProvider.AUTHORITY, MySearchSuggestionProvider.MODE
            )
            suggestions.saveRecentQuery(rawQuery, null)

            as_progressBar.show()

            searchView?.findViewById<EditText>(R.id.search_src_text)?.setText(rawQuery)

            val query = rawQuery.trim()
                .replace("  ", " ")
                .replace(" ", "+")
                .replace("/", "")

            SyncJob.cancel(JOB_SEARCH)

            var adapter: AppAdapter? = null

            launch(Dispatchers.IO) {

                adapter = AppAdapter(mutableListOf(), R.layout.item_app_horizontal, this@SearchActivity)
                adapter?.isPageable = true

                adapter?.fetchData = fetchData@{ page ->
                    runOnUiThread {
                        as_progressBar.show()
                    }

                    val data = try {
                        val html = service.search(query, page).execute().document()
                        Parser.appList(html)
                    } catch (t: NullPointerException) {
                        t.printStackTrace()
                        arrayOf<App>()
                    } catch (t: UnknownHostException) {
                        t.printStackTrace()
                        runOnUiThread {

                            as_tryAgain.visibility = View.VISIBLE
                        }
                        null
                    }

                    runOnUiThread {
                        as_progressBar.hide()
                    }

                    return@fetchData data
                }

                adapter?.onClick = { app, holder ->
                    Intent(this@SearchActivity, AppPageActivity::class.java).apply {
                        putExtra("url", app.link)
                        putExtra("name", app.name)
                        putExtra("imageUrl", app.imageUrl)
                        startActivity(
                            this, ActivityOptionsCompat.makeSceneTransitionAnimation(
                                this@SearchActivity,
                                holder.iconContainer!!, "app_icon"
                            ).toBundle()
                        )
                    }
                }

                as_recyclerView.itemAnimator = DefaultItemAnimator()

                withContext(Dispatchers.Main) {
                    as_recyclerView.addItemDecoration(
                        DividerItemDecoration(
                            this@SearchActivity, DividerItemDecoration.VERTICAL
                        )
                    )
                    as_recyclerView.adapter = adapter
                    as_recyclerView.layoutManager =
                        LinearLayoutManager(this@SearchActivity, RecyclerView.VERTICAL, false)

                    adapter?.fetchData()

                }
            }.let {
                SyncJob.add(JOB_SEARCH, it)
            }

            as_tryAgain.setOnClickListener {
                it.visibility = View.GONE
                adapter?.fetchData()
            }

        }

    }

    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            setIconifiedByDefault(false)

            findViewById<View>(R.id.search_plate)?.let {
                it.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }

            findViewById<View>(R.id.search_mag_icon)?.let {
                it.layoutParams.width = 0
            }

            findViewById<EditText>(R.id.search_src_text)?.setText(rawQuery)

        }


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}
