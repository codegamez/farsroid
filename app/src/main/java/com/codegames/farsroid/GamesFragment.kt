package com.codegames.farsroid

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.codegames.farsroid.adapter.AppAdapter
import com.codegames.farsroid.api.FarsroidApi
import com.codegames.farsroid.database.getIfUpdated
import com.codegames.farsroid.database.put
import com.codegames.farsroid.model.Page
import com.codegames.farsroid.util.*
import kotlinx.android.synthetic.main.fragment_games.view.*
import kotlinx.coroutines.*
import lib.codegames.extension.runOnUiThread
import org.jsoup.Jsoup
import kotlin.math.max


class GamesFragment : Fragment(), ToolbarListener {

    private val coroutineScope
        get() = context as? CoroutineScope

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_games, container, false)

        view.fg_newGameProgressBar.show()

        getMainPage(view)
        getSuggestedGames(view)
        getMostDownloadedGames(view)
        getTopGames(view)

        view.fg_tryAgain.setOnClickListener {
            it.visibility = View.GONE
            view.fg_newGameProgressBar.visibility = View.VISIBLE

            if (SyncJob.get(JOB_GET_MAIN_PAGE)?.isCancelled != false) {
                SyncJob.cancel(JOB_GET_MAIN_PAGE)
                getMainPage(view)
            } else if (SyncJob.get(JOB_GET_MAIN_PAGE)?.isCompleted == true
                && doneJobs[JOB_GET_MAIN_PAGE] == false
            ) {
                getMainPage(view)
            } else if (SyncJob.get(JOB_GET_MAIN_PAGE)?.isActive == true
                && doneJobs[JOB_GET_MAIN_PAGE] == false
            ) {
                getMainPage(view)
            }

            if (SyncJob.get(JOB_GET_SUGGESTED_GAMES)?.isCancelled != false) {
                getSuggestedGames(view)
            }

            if (SyncJob.get(JOB_GET_MOST_DOWNLOADED_GAMES)?.isCancelled != false) {
                getMostDownloadedGames(view)
            }

            if (SyncJob.get(JOB_GET_TOP_GAMES)?.isCancelled != false) {
                getTopGames(view)
            }

        }

        return view
    }

    private fun getMainPage(view: View) {

        (SyncJob.get(JOB_GET_MAIN_PAGE) ?: Store.getMainpageAsync(coroutineScope))?.invokeOnCompletion {

            if (it != null) {
                done(JOB_GET_MAIN_PAGE, true)
                return@invokeOnCompletion
            }

            coroutineScope?.launch(Dispatchers.IO) {

                val gameList = Parser.mainpageGames(Store.mainpageDocument).toMutableList()

                val adapter = AppAdapter(gameList, R.layout.item_app, coroutineScope)
                adapter.isPageable = false

                adapter.onClick = {
                    Intent(context, AppPageActivity::class.java).apply {
                        putExtra("url", it.link)
                        putExtra("name", it.name)
                        startActivity(this)
                    }
                }

                view.fg_newGameRecyclerView.itemAnimator = DefaultItemAnimator()

                withContext(Dispatchers.Main) {

                    view.fg_newGameMore.setOnClickListener {
                        Intent(context, AppListActivity::class.java).apply {
                            putExtra("url", "cat/game")
                            putExtra("title", "بازی ها")
                            startActivity(this)
                        }
                    }

                    view.fg_categories.setOnClickListener {
                        Intent(context, CategoryListActivity::class.java).apply {
                            putExtra("type", "game")
                            putExtra("title", "بازی ها")
                            startActivity(this)
                        }
                    }

                    context?.apply {
                        val decoration = SpacesItemDecoration(
                            resources.getDimensionPixelSize(R.dimen.space_md),
                            LinearLayoutManager.HORIZONTAL
                        )
                        view.fg_newGameRecyclerView.addItemDecoration(decoration)
                    }
                    view.fg_newGameRecyclerView.adapter = adapter
                    view.fg_newGameRecyclerView.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                    done(JOB_GET_MAIN_PAGE)
                }

            }

        }

    }

    private fun getSuggestedGames(view: View) {
        coroutineScope?.launch(Dispatchers.IO) {

            val page = database.pageDao().get(FarsroidApi.SUGGESTED_GAMES)
            val html = if (page == null) {
                try {
                    service.suggestedGames().execute().document().apply {
                        database.pageDao().put(
                            Page(
                                url = FarsroidApi.SUGGESTED_GAMES,
                                data = this.toString(),
                                updated = now()
                            )
                        )
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    done(JOB_GET_SUGGESTED_GAMES, true)
                    cancel()
                    return@launch
                }
            } else
                Jsoup.parse(page.data)

            val apps = Parser.appList(html).toMutableList()

            val adapter = AppAdapter(apps, R.layout.item_app_5, coroutineScope)
            adapter.isPageable = false

            adapter.onClick = {
                Intent(context, AppPageActivity::class.java).apply {
                    putExtra("url", it.link)
                    putExtra("name", it.name)
                    startActivity(this)
                }
            }

            view.fg_suggestionRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                view.fg_suggestionMore.setOnClickListener {
                    Intent(context, AppGridActivity::class.java).apply {
                        putExtra("url", "cat/game/suggested-games")
                        putExtra("title", getString(R.string.suggestion))
                        startActivity(this)
                    }
                }

                context?.let {
                    val decoration = SpacesItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.HORIZONTAL
                    )
                    view.fg_suggestionRecyclerView.addItemDecoration(decoration)
                }

                view.fg_suggestionRecyclerView.adapter = adapter
                view.fg_suggestionRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                done(JOB_GET_SUGGESTED_GAMES)
            }

        }?.apply {
            SyncJob.add(JOB_GET_SUGGESTED_GAMES, this)
        }
    }

    private fun getMostDownloadedGames(view: View) {

        coroutineScope?.launch(Dispatchers.IO) {

            val page = database.pageDao().getIfUpdated(FarsroidApi.MOST_DOWNLOADED_GAMES)
            val html = if (page == null) {
                try {
                    service.mostDownloadedGames().execute().document().apply {
                        database.pageDao().put(
                            Page(
                                url = FarsroidApi.MOST_DOWNLOADED_GAMES,
                                data = this.toString(),
                                updated = now()
                            )
                        )
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    done(JOB_GET_MOST_DOWNLOADED_GAMES, true)
                    cancel()
                    return@launch
                }
            } else
                Jsoup.parse(page.data)

            val apps = Parser.mostDownloaded(html).toMutableList()

            val adapter = AppAdapter(apps, R.layout.item_app_2, coroutineScope)
            adapter.isPageable = false

            adapter.onClick = {
                Intent(context, AppPageActivity::class.java).apply {
                    putExtra("url", it.link)
                    putExtra("name", it.name)
                    startActivity(this)
                }
            }

            view.fg_mostDownloadedRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                view.fg_mostDownloadedMore.setOnClickListener {
                    Intent(context, AppGridActivity::class.java).apply {
                        putExtra("type", TYPE_MOST_DOWNLOADED_GAMES)
                        startActivity(this)
                    }
                }

                context?.let {
                    val decoration = SpacesItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.HORIZONTAL
                    )
                    view.fg_mostDownloadedRecyclerView.addItemDecoration(decoration)
                }

                view.fg_mostDownloadedRecyclerView.adapter = adapter
                view.fg_mostDownloadedRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                done(JOB_GET_MOST_DOWNLOADED_GAMES)
            }

        }?.apply {
            SyncJob.add(JOB_GET_MOST_DOWNLOADED_GAMES, this)
        }

    }

    private fun getTopGames(view: View) {

        coroutineScope?.launch(Dispatchers.IO) {

            val html = database.pageDao().getIfUpdated(FarsroidApi.TOP_GAMES)?.let {
                Jsoup.parse(it.data)
            } ?: try {
                service.topGames().execute().document().apply {
                    database.pageDao().put(
                        Page(
                            url = FarsroidApi.TOP_GAMES,
                            data = this.toString(),
                            updated = now()
                        )
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                done(JOB_GET_TOP_GAMES, true)
                cancel()
                return@launch
            }

            val apps = Parser.bestApps(html).toMutableList()

            val adapter = AppAdapter(apps, R.layout.item_app_2, coroutineScope)
            adapter.isPageable = false

            adapter.onClick = {
                Intent(context, AppPageActivity::class.java).apply {
                    putExtra("url", it.link)
                    putExtra("name", it.name)
                    startActivity(this)
                }
            }

            view.fg_topAppsRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                view.fg_topAppsMore.setOnClickListener {
                    Intent(context, AppGridActivity::class.java).apply {
                        putExtra("type", TYPE_TOP_GAMES)
                        startActivity(this)
                    }
                }

                context?.let {
                    val decoration = SpacesItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.HORIZONTAL
                    )
                    view.fg_topAppsRecyclerView.addItemDecoration(decoration)
                }

                view.fg_topAppsRecyclerView.adapter = adapter
                view.fg_topAppsRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                done(JOB_GET_TOP_GAMES)
            }

        }?.apply {
            SyncJob.add(JOB_GET_TOP_GAMES, this)
        }

    }

    private val doneJobs = mutableMapOf(
        JOB_GET_MAIN_PAGE to false,
        JOB_GET_SUGGESTED_GAMES to false,
        JOB_GET_MOST_DOWNLOADED_GAMES to false,
        JOB_GET_TOP_GAMES to false
    )

    private fun done(jobName: String, isCanceled: Boolean = false) {
        if (!isCanceled) {
            doneJobs[jobName] = true
            if (!doneJobs.containsValue(false)) {
                context?.runOnUiThread {
                    view?.apply {
                        fg_newGameProgressBar.hide()
                        fg_contentContainer.alpha = 0F
                        fg_contentContainer.visibility = View.VISIBLE
                        fg_contentContainer.animate().alpha(1F).start()
                    }
                }
            }
        } else {
            context?.runOnUiThread {
                view?.apply {
                    fg_newGameProgressBar.hide()
                    fg_tryAgain.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        fun newInstance() = GamesFragment()
    }

    override fun onOffsetChange(toolbarHeight: Int, offset: Int) {
        val bottom = max(toolbarHeight + offset, 0)
        view?.fgTopContainer?.setPadding(0, 0, 0, bottom)
    }

}
