package com.codegames.farsroid.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codegames.farsroid.model.Page
import java.util.*

@Dao
interface PageDao {

    @Insert
    fun insert(page: Page)

    @Update
    fun update(page: Page)

    @Query(" SELECT * FROM Page WHERE url = :url LIMIT 1 ")
    fun get(url: String): Page?

    @Query(" DELETE FROM Page WHERE updated < :time ")
    fun deleteOlds(time: Long)

}

fun PageDao.put(page: Page) {
    if(get(page.url) == null)
        insert(page)
    else
        update(page)
}

fun Page?.isOld(duration: Long = 60 * 30 * 1000): Boolean {

    val now = Date().time

    return when {
        this == null -> true
        this.updated < now - duration -> true
        else -> false
    }

}

fun PageDao.getIfUpdated(url: String, duration: Long = 60 * 30 * 1000): Page? {

    val page = get(url)

    return when {
        page == null -> null
        page.isOld(duration) -> null
        else -> page
    }

}