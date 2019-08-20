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
import kotlinx.android.synthetic.main.fragment_apps.*
import kotlinx.android.synthetic.main.fragment_apps.view.*
import kotlinx.coroutines.*
import lib.codegames.extension.runOnUiThread
import org.jsoup.Jsoup
import kotlin.math.max

class AppsFragment : Fragment(), ToolbarListener {

    private val coroutineScope
        get() = context as? CoroutineScope

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_apps, container, false)

        view.fa_newGameProgressBar.show()

        getMainPage(view)
        getSuggestedApps(view)
        getMostDownloadedApps(view)
        getTopApps(view)

        view.fa_tryAgain.setOnClickListener {
            it.visibility = View.GONE
            view.fa_newGameProgressBar.visibility = View.VISIBLE

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

            if (SyncJob.get(JOB_GET_SUGGESTED_APPS)?.isCancelled != false) {
                getSuggestedApps(view)
            }

            if (SyncJob.get(JOB_GET_MOST_DOWNLOADED_APPS)?.isCancelled != false) {
                getMostDownloadedApps(view)
            }

            if (SyncJob.get(JOB_GET_TOP_APPS)?.isCancelled != false) {
                getTopApps(view)
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

                val programList = Parser.mainpagePrograms(Store.mainpageDocument).toMutableList()

                val adapter = AppAdapter(programList, R.layout.item_app, coroutineScope)
                adapter.isPageable = false

                adapter.onClick = {
                    Intent(context, AppPageActivity::class.java).apply {
                        putExtra("url", it.link)
                        putExtra("name", it.name)
                        startActivity(this)
                    }
                }

                view.fa_newGameRecyclerView.itemAnimator = DefaultItemAnimator()

                withContext(Dispatchers.Main) {

                    view.fa_newGameMore.setOnClickListener {
                        Intent(context, AppListActivity::class.java).apply {
                            putExtra("url", "cat/application")
                            putExtra("title", "برنامه ها")
                            startActivity(this)
                        }
                    }


                    fa_categories.setOnClickListener {
                        Intent(context, CategoryListActivity::class.java).apply {
                            putExtra("type", "program")
                            putExtra("title", "برنامه ها")
                            startActivity(this)
                        }
                    }

                    context?.apply {
                        val decoration = SpacesItemDecoration(
                            resources.getDimensionPixelSize(R.dimen.space_md),
                            LinearLayoutManager.HORIZONTAL
                        )
                        view.fa_newGameRecyclerView.addItemDecoration(decoration)
                    }
                    view.fa_newGameRecyclerView.adapter = adapter
                    view.fa_newGameRecyclerView.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                    done(JOB_GET_MAIN_PAGE)
                }

            }?.run {
                SyncJob.add(this)
            }

        }

    }

    private fun getSuggestedApps(view: View) {

        coroutineScope?.launch(Dispatchers.IO) {

            val html = database.pageDao().getIfUpdated(FarsroidApi.SUGGESTED_APPS)?.let {
                Jsoup.parse(it.data)
            } ?:try {
                service.suggestedApps().execute().document().apply {
                    database.pageDao().put(
                        Page(
                            url = FarsroidApi.SUGGESTED_APPS,
                            data = this.toString(),
                            updated = now()
                        )
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                done(JOB_GET_SUGGESTED_APPS, true)
                cancel()
                return@launch
            }

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

            view.fa_suggestionRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                view.fa_suggestionMore.setOnClickListener {
                    Intent(context, AppGridActivity::class.java).apply {
                        putExtra("url", "cat/app/suggested-apps")
                        putExtra("title", getString(R.string.suggestion))
                        startActivity(this)
                    }
                }

                context?.let {
                    val decoration = SpacesItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.HORIZONTAL
                    )
                    view.fa_suggestionRecyclerView.addItemDecoration(decoration)
                }

                view.fa_suggestionRecyclerView.adapter = adapter
                view.fa_suggestionRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                done(JOB_GET_SUGGESTED_APPS)
            }

        }?.apply {
            SyncJob.add(JOB_GET_SUGGESTED_APPS, this)
        }

    }

    private fun getMostDownloadedApps(view: View) {

        coroutineScope?.launch(Dispatchers.IO) {

            val html = database.pageDao().getIfUpdated(FarsroidApi.MOST_DOWNLOADED_APPS)?.let {
                Jsoup.parse(it.data)
            } ?:try {
                service.mostDownloadedApps().execute().document().apply {
                    database.pageDao().put(
                        Page(
                            url = FarsroidApi.MOST_DOWNLOADED_APPS,
                            data = this.toString(),
                            updated = now()
                        )
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                done(JOB_GET_MOST_DOWNLOADED_APPS, true)
                cancel()
                return@launch
            }

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

            view.fa_mostDownloadedRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                view.fa_mostDownloadedMore.setOnClickListener {
                    Intent(context, AppGridActivity::class.java).apply {
                        putExtra("type", TYPE_MOST_DOWNLOADED_APPS)
                        startActivity(this)
                    }
                }

                context?.let {
                    val decoration = SpacesItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.HORIZONTAL
                    )
                    view.fa_mostDownloadedRecyclerView.addItemDecoration(decoration)
                }

                view.fa_mostDownloadedRecyclerView.adapter = adapter
                view.fa_mostDownloadedRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                done(JOB_GET_MOST_DOWNLOADED_APPS)
            }

        }?.apply {
            SyncJob.add(JOB_GET_MOST_DOWNLOADED_APPS, this)
        }

    }

    private fun getTopApps(view: View) {

        coroutineScope?.launch(Dispatchers.IO) {

            val html = database.pageDao().getIfUpdated(FarsroidApi.TOP_APPS)?.let {
                Jsoup.parse(it.data)
            } ?:try {
                service.topApps().execute().document().apply {
                    database.pageDao().put(
                        Page(
                            url = FarsroidApi.TOP_APPS,
                            data = this.toString(),
                            updated = now()
                        )
                    )
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                done(JOB_GET_TOP_APPS, true)
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

            view.fa_topAppsRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                view.fa_topAppsMore.setOnClickListener {
                    Intent(context, AppGridActivity::class.java).apply {
                        putExtra("type", TYPE_TOP_APPS)
                        startActivity(this)
                    }
                }

                context?.let {
                    val decoration = SpacesItemDecoration(
                        it.resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.HORIZONTAL
                    )
                    view.fa_topAppsRecyclerView.addItemDecoration(decoration)
                }

                view.fa_topAppsRecyclerView.adapter = adapter
                view.fa_topAppsRecyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                done(JOB_GET_TOP_APPS)
            }

        }?.apply {
            SyncJob.add(JOB_GET_TOP_APPS, this)
        }

    }

    private val doneJobs = mutableMapOf(
        JOB_GET_MAIN_PAGE to false,
        JOB_GET_SUGGESTED_APPS to false,
        JOB_GET_MOST_DOWNLOADED_APPS to false,
        JOB_GET_TOP_APPS to false
    )

    private fun done(jobName: String, isCanceled: Boolean = false) {
        if (!isCanceled) {
            doneJobs[jobName] = true
            if (!doneJobs.containsValue(false)) {
                context?.runOnUiThread {
                    view?.apply {
                        fa_newGameProgressBar.hide()
                        fa_content_Container.alpha = 0F
                        fa_content_Container.visibility = View.VISIBLE
                        fa_content_Container.animate().alpha(1F).start()
                    }
                }
            }
        } else {
            context?.runOnUiThread {
                view?.apply {
                    fa_newGameProgressBar.hide()
                    fa_tryAgain.visibility = View.VISIBLE
                }
            }
        }

    }

    companion object {
        fun newInstance() = AppsFragment()
    }

    override fun onOffsetChange(toolbarHeight: Int, offset: Int) {
        val bottom = max(toolbarHeight + offset, 0)
        view?.faTopContainer?.setPadding(0, 0, 0, bottom)
    }

}
