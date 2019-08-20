package com.codegames.farsroid

import com.codegames.farsroid.api.FarsroidApi
import com.codegames.farsroid.database.AppDatabase
import com.codegames.farsroid.database.getIfUpdated
import com.codegames.farsroid.database.put
import com.codegames.farsroid.model.Page
import com.codegames.farsroid.util.document
import com.codegames.farsroid.util.now
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


const val BASEURL = "https://www.farsroid.com"

lateinit var service: FarsroidApi
lateinit var database: AppDatabase
val gson = Gson()

object Store {

    lateinit var mainpageDocument: Document

    fun getMainpageAsync(scope: CoroutineScope? = GlobalScope): Job? {
        return scope?.launch(Dispatchers.IO) {

            database.pageDao().getIfUpdated(FarsroidApi.MAIN_PAGE)?.let {
                mainpageDocument = Jsoup.parse(it.data)
            } ?: try {
                val html = service.mainPage().execute().document()
                database.pageDao().put(
                    Page(
                        url = FarsroidApi.MAIN_PAGE,
                        data = html.toString(),
                        updated = now()
                    )
                )
                mainpageDocument = html
            } catch (e: IOException) {
                e.printStackTrace()
                cancel()
            }

        }?.let {
            SyncJob.add(JOB_GET_MAIN_PAGE, it)
            it
        }
    }

}