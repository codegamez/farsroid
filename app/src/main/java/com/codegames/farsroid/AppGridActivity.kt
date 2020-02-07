package com.codegames.farsroid

import android.app.ActivityOptions
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
import androidx.recyclerview.widget.GridLayoutManager
import com.codegames.farsroid.adapter.AppAdapter
import com.codegames.farsroid.api.FarsroidApi
import com.codegames.farsroid.database.getIfUpdated
import com.codegames.farsroid.database.put
import com.codegames.farsroid.model.App
import com.codegames.farsroid.model.Page
import com.codegames.farsroid.util.Parser
import com.codegames.farsroid.util.SpacesItemDecoration
import com.codegames.farsroid.util.document
import com.codegames.farsroid.util.now
import kotlinx.android.synthetic.main.activity_app_grid.*
import kotlinx.android.synthetic.main.content_app_grid.*
import kotlinx.android.synthetic.main.content_app_page.*
import kotlinx.coroutines.*
import lib.codegames.debug.LogCG
import org.jsoup.Jsoup
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext
import kotlin.math.round

const val TYPE_TOP_APPS = 1
const val TYPE_TOP_GAMES = 2
const val TYPE_MOST_DOWNLOADED_APPS = 3
const val TYPE_MOST_DOWNLOADED_GAMES = 4

class AppGridActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private var type: Int = 0
    private var url = ""

    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_grid)
        mJob = Job()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        type = intent?.extras?.getInt("type", 0) ?: 0

        url = intent?.extras?.getString("url") ?: ""

        title = when (type) {
            TYPE_TOP_APPS -> getString(R.string.best_apps)
            TYPE_TOP_GAMES -> getString(R.string.best_games)
            TYPE_MOST_DOWNLOADED_APPS -> getString(R.string.most_downloaded_apps)
            TYPE_MOST_DOWNLOADED_GAMES -> getString(R.string.most_downloaded_games)
            else -> intent?.extras?.getString("title") ?: ""
        }

        aag_progressBar.show()

        getAppList()
        aag_tryAgain.setOnClickListener {
            it.visibility = View.GONE
            if (type == 0)
                adapter.fetchData()
            else
                getAppList()
        }

    }

    private fun getAppList() {

        launch(Dispatchers.IO) {

            val appList = if (type != 0) {

                val html = try {
                    when (type) {

                        TYPE_TOP_APPS -> database.pageDao().getIfUpdated(FarsroidApi.TOP_APPS)?.let {
                            Jsoup.parse(it.data)
                        } ?: service.topApps().execute().document().apply {
                            database.pageDao().put(
                                Page(
                                    url = FarsroidApi.TOP_APPS,
                                    data = this.toString(),
                                    updated = now()
                                )
                            )
                        }

                        TYPE_TOP_GAMES -> database.pageDao().getIfUpdated(FarsroidApi.TOP_GAMES)?.let {
                            Jsoup.parse(it.data)
                        } ?: service.topGames().execute().document().apply {
                            database.pageDao().put(
                                Page(
                                    url = FarsroidApi.TOP_GAMES,
                                    data = this.toString(),
                                    updated = now()
                                )
                            )
                        }

                        TYPE_MOST_DOWNLOADED_APPS -> database.pageDao().getIfUpdated(FarsroidApi.MOST_DOWNLOADED_APPS)?.let {
                            Jsoup.parse(it.data)
                        } ?: service.mostDownloadedApps().execute().document().apply {
                            database.pageDao().put(
                                Page(
                                    url = FarsroidApi.MOST_DOWNLOADED_APPS,
                                    data = this.toString(),
                                    updated = now()
                                )
                            )
                        }

                        TYPE_MOST_DOWNLOADED_GAMES -> database.pageDao().getIfUpdated(FarsroidApi.MOST_DOWNLOADED_GAMES)?.let {
                            Jsoup.parse(it.data)
                        } ?: service.mostDownloadedGames().execute().document().apply {
                            database.pageDao().put(
                                Page(
                                    url = FarsroidApi.MOST_DOWNLOADED_GAMES,
                                    data = this.toString(),
                                    updated = now()
                                )
                            )
                        }

                        else -> throw Throwable("Wrong Type")
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    done(true)
                    cancel()
                    return@launch
                }

                when (type) {
                    TYPE_TOP_APPS -> Parser.bestApps(html).toMutableList()
                    TYPE_TOP_GAMES -> Parser.bestApps(html).toMutableList()
                    TYPE_MOST_DOWNLOADED_APPS -> Parser.mostDownloaded(html).toMutableList()
                    TYPE_MOST_DOWNLOADED_GAMES -> Parser.mostDownloaded(html).toMutableList()
                    else -> Parser.appList(html).toMutableList()
                }

            } else
                mutableListOf()

            adapter = AppAdapter(
                appList, if (type != 0)
                    R.layout.item_app_3
                else
                    R.layout.item_app_4,
                this@AppGridActivity
            )

            adapter.isPageable = type == 0

            adapter.fetchData = fetchData@{ page ->
                runOnUiThread {
                    aag_progressBar.show()
                }
                val data = try {
                    val html = service.appList(url, page).execute().document()
                    Parser.appList(html)
                } catch (t: NullPointerException) {
                    t.printStackTrace()
                    LogCG.d(t.javaClass.simpleName)
                    arrayOf<App>()
                } catch (t: UnknownHostException) {
                    runOnUiThread {
                        aag_tryAgain.visibility = View.VISIBLE
                    }
                    null
                }

                runOnUiThread {
                    aag_progressBar.hide()
                }
                return@fetchData data
            }

            adapter.onClick = { app, holder ->
                Intent(this@AppGridActivity, AppPageActivity::class.java).apply {
                    putExtra("url", app.link)
                    putExtra("name", app.name)
                    putExtra("imageUrl", app.imageUrl)
                    startActivity(
                        this, ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@AppGridActivity,
                            holder.iconContainer!!, "app_icon"
                        ).toBundle()
                    )
                }
            }

            aag_recyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                val padding = resources.getDimensionPixelSize(R.dimen.space_md)
                val decoration = SpacesItemDecoration(
                    padding,
                    SpacesItemDecoration.GRID
                )
                aag_recyclerView.addItemDecoration(decoration)

                aag_recyclerView.adapter = adapter
                aag_recyclerView.layoutManager =
                    GridLayoutManager(this@AppGridActivity, calculateSpanCount(padding))

                if (type == 0)
                    adapter.fetchData()
                else
                    done()

            }

        }.let {
            SyncJob.add(it)
        }

    }

    private var searchView: SearchView? = null

    private fun calculateSpanCount(padding: Int): Int {

        var screenWidth = window.decorView.width.toFloat()
        val itemWidth = resources.getDimensionPixelSize(R.dimen.app_item_size)

        var num = screenWidth / itemWidth

        LogCG.d(num)

        screenWidth -= (round(num).toInt() + 1) * padding

        num = screenWidth / itemWidth

        LogCG.d(num)

//        val spanCount = round(
//
//        ).toInt()

        return round(num).toInt()
    }

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

    private fun done(isCanceled: Boolean = false) {
        runOnUiThread {
            aag_progressBar.hide()
            if (isCanceled)
                aag_tryAgain.visibility = View.VISIBLE
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

}
