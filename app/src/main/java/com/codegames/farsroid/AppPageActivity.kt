package com.codegames.farsroid

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.adapter.DownloadAdapter
import com.codegames.farsroid.adapter.SliderAdapter
import com.codegames.farsroid.database.getIfUpdated
import com.codegames.farsroid.util.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_app_page.*
import kotlinx.android.synthetic.main.content_app_page.*
import kotlinx.coroutines.*
import lib.codegames.extension.fanum
import lib.codegames.extension.hide
import lib.codegames.extension.show
import kotlin.coroutines.CoroutineContext


class AppPageActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_page)
        mJob = Job()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = ""

        val url = intent?.extras?.getString("url") ?: throw Throwable("set url")
        val name = intent?.extras?.getString("name") ?: throw Throwable("set name")

        aap_tryAgain.setOnClickListener {
            aap_tryAgain.visibility = View.GONE
            getAppPage(url, name)
        }

        getAppPage(url, name)

    }

    private fun getAppPage(url: String, name: String) {

        aap_progressBar.show()

        launch(Dispatchers.IO) {

            val app = database.appDao().getIfUpdated(url)?.apply {
                seen = now()
            } ?: try {
                Parser.appPage(service.appPage(url).execute().document()).apply {
                    updated = now()
                    seen = now()
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                withContext(Dispatchers.Main) {
                    aap_progressBar.hide()
                    aap_tryAgain.visibility = View.VISIBLE
                }
                cancel()
                return@launch
            }

            with(database.appDao()) {
                if (get(app.link) == null) { // insert
                    insertAll(app)
                    deleteOlds(100)
                } else { // update
                    updateAll(app)
                }
            }

            val adapter = SliderAdapter(app.screenshotList)

            adapter.onClick = { _, position ->
                Intent(this@AppPageActivity, ImageActivity::class.java).apply {
                    putExtra("images", adapter.images)
                    putExtra("position", position)
                    startActivity(this)
                }
            }

            aap_slider.itemAnimator = DefaultItemAnimator()

            val downloadAdapter = DownloadAdapter(app.downloadLinks)

//            downloadAdapter.onClick = onCLick@ {
//
//                getPermissionIfNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)?.let { permission ->
//                    ActivityCompat.requestPermissions(this@AppPageActivity, arrayOf(permission), 1)
//                    return@onCLick
//                }
//
//                DownloadManager.Request(it.second.toUri()).apply {
//                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, it.second.getName())
//                    allowScanningByMediaScanner()
//                    setTitle(app.name)
//                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                    dm.enqueue(this)
//                }
//
//            }

            downloadAdapter.onClick = {

                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.second))
                startActivity(browserIntent)

            }

            downloadAdapter.onLongClick = {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.primaryClip = ClipData.newRawUri(app.name, it.second.toUri())
                Snackbar.make(aap_coordinatorLayout, R.string.file_link_copied, Snackbar.LENGTH_SHORT).show()
            }

            aap_downloadRecyclerView.itemAnimator = DefaultItemAnimator()

            withContext(Dispatchers.Main) {

                Picasso.get()
                    .load(app.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(aap_icon)

                Picasso.get()
                    .load(app.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(aap_content_icon)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    aap_content_less.text = Html.fromHtml(app.contentLess, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    aap_content_less.text = Html.fromHtml(app.contentLess)
                }

                aap_rate.rating = (app.rate.toIntOrNull() ?: 0).toFloat()
                aap_rate.setIsIndicator(true)

                aap_content_name.text = name.trim()

                aap_name.text = app.title.trim().fanum()

                aap_category.text = app.categoryName

                aap_category.setOnClickListener {
                    Intent(this@AppPageActivity, AppListActivity::class.java).apply {
                        putExtra("url", app.categoryLink)
                        putExtra("title", app.categoryName)
                        startActivity(this)
                    }
                }

                aap_install.setOnClickListener {
                    aap_scrollView.smoothScrollTo(0, aap_downloadBox.y.toInt())
                }

                aap_content.isClickable = false
                aap_content.isLongClickable = false
                aap_content.loadData(
                    """
                    <html>
                        <head>
                            <style>
                                @font-face {
                                    font-family: 'Samim';
                                    src: url('file:///android_asset/font/samim_normal.ttf');
                                    font-weight: normal;
                                }
                                @font-face {
                                    font-family: 'Samim';
                                    src: url('file:///android_asset/font/samim_bold.ttf');
                                    font-weight: bold;
                                }

                                * {
                                    text-decoration: none;
                                    font-family: Samim, Arial, serif !important;
                                }

                                body {
                                    background-color: #ffffff;
                                    -webkit-touch-callout: none;
                                    -webkit-user-select: none;
                                    -khtml-user-select: none;
                                    -moz-user-select: none;
                                    -ms-user-select: none;
                                    user-select: none;
                                }
                                a {
                                    color: inherit;
                                    pointer-events: none;
                                }
                                img.aligncenter {
                                    display: block;
                                    margin: 5px auto;
                                }
                                img {
                                    height: auto;
                                    max-width: 100% !important;
                                    border: 0;
                                    vertical-align: middle;
                                }
                            </style>
                        </head>
                        <body dir="rtl">${app.content.replace(Regex("width:\\s?\\d+px;?"), "")}</body>
                    </html>
                """.trimIndent(), "text/html", "UTF-8"
                )

                aap_comment.isSelected = true

                aap_more.setOnClickListener {
                    aap_contentContainer.alpha = 0F
                    aap_contentContainer.scaleX = 0F
                    aap_contentContainer.scaleY = 0F
                    aap_contentContainer.show(300)
                }

                aap_content_back.setOnClickListener {
                    aap_contentContainer.hide(300)
                }

                aap_comment.text = app.commentCount.toPrettyNumber().fanum()
                aap_version.text = app.version.trim().fanum()
                aap_download.text = app.downloadCount.toPrettyNumber().fanum()
                aap_lastUpdate.text = app.lastUpdate.trim().toGoodDate().fanum()
                aap_likeCount.text = app.likeCount.toPrettyNumber().fanum()

                var decoration = SpacesItemDecoration(
                    resources.getDimensionPixelSize(R.dimen.space_sm),
                    LinearLayoutManager.HORIZONTAL
                )
                aap_slider.addItemDecoration(decoration)
                aap_slider.adapter = adapter
                aap_slider.layoutManager =
                    LinearLayoutManager(this@AppPageActivity, LinearLayoutManager.HORIZONTAL, false)

                decoration = SpacesItemDecoration(
                    resources.getDimensionPixelSize(R.dimen.space_md),
                    LinearLayoutManager.VERTICAL
                )
                aap_downloadRecyclerView.addItemDecoration(decoration)
                aap_downloadRecyclerView.adapter = downloadAdapter
                aap_downloadRecyclerView.layoutManager =
                    LinearLayoutManager(this@AppPageActivity, RecyclerView.VERTICAL, false)

                aap_downloadHelp.setOnClickListener {
                    Intent(this@AppPageActivity, DownloadHelpDialog::class.java).apply {
                        startActivity(this)
                    }
                }

                aap_scrollView.visibility = View.VISIBLE
                aap_progressBar.hide()
            }

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
        when {
            aap_contentContainer.isVisible -> aap_contentContainer.hide(300)
            searchView?.isIconified == false -> searchView?.onActionViewCollapsed()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}
