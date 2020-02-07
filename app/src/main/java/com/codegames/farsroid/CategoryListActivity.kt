package com.codegames.farsroid

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codegames.farsroid.adapter.CategoryAdapter
import com.codegames.farsroid.util.Parser
import com.codegames.farsroid.util.SpacesItemDecoration

import kotlinx.android.synthetic.main.activity_category_list.*
import kotlinx.android.synthetic.main.content_category_list.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class CategoryListActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)
        mJob = Job()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent?.extras?.getString("type") ?: throw Throwable("Set Type")
        title = intent?.extras?.getString("title") ?: throw Throwable("Set Title")

        acl_progressBar.show()
        (SyncJob.get(JOB_GET_MAIN_PAGE) ?: Store.getMainpageAsync(this))?.invokeOnCompletion {

            if (it != null) {
                showDisconnected()
                return@invokeOnCompletion
            }

            launch(Dispatchers.IO) {

                val categoryList = if (type == "game") {
                    Parser.gameCategoryList(Store.mainpageDocument)
                } else {
                    Parser.programCategoryList(Store.mainpageDocument)
                }

                val adapter = CategoryAdapter(categoryList, type)

                adapter.onClick = {
                    Intent(this@CategoryListActivity, AppListActivity::class.java).apply {
                        putExtra("url", it.second)
                        putExtra("title", it.first)
                        startActivity(this)
                    }
                }

                acl_recyclerView.itemAnimator = DefaultItemAnimator()

                withContext(Dispatchers.Main) {

                    val decoration = SpacesItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.space_md),
                        LinearLayoutManager.VERTICAL
                    )
                    acl_recyclerView.addItemDecoration(decoration)
                    acl_recyclerView.adapter = adapter
                    acl_recyclerView.layoutManager =
                        LinearLayoutManager(this@CategoryListActivity, RecyclerView.VERTICAL, false)
                    acl_progressBar.hide()
                }

                val myData = mutableListOf(
                    "data 1",
                    "data 2",
                    "data 3"
                )

            }

        }

    }

    private fun showDisconnected() {
        acl_progressBar.hide()
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

    override fun onDestroy() {
        super.onDestroy()
        mJob.cancel()
    }

}

