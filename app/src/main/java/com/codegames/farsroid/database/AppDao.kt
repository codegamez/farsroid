package com.codegames.farsroid.database

import androidx.room.*
import com.codegames.farsroid.model.App
import java.util.*

@Dao
interface AppDao {

    @Query("SELECT * FROM App ORDER BY seen DESC")
    fun getAll(): List<App>

    @Insert
    fun insertAll(vararg apps: App)

    @Query(" SELECT * FROM APP WHERE link = :link LIMIT 1")
    fun get(link: String): App?

    @Update
    fun updateAll(vararg apps: App)

    @Delete
    fun delete(app: App)

    @Query("DELETE FROM App WHERE link in (SELECT link FROM App ORDER BY seen DESC LIMIT 100 OFFSET :offset)")
    fun deleteOlds(offset: Int)

}

fun App?.isOld(duration: Long = 60 * 30 * 1000): Boolean {

    val now = Date().time

    return when {
        this == null -> true
        this.updated < now - duration -> true
        else -> false
    }

}

fun AppDao.getIfUpdated(url: String, duration: Long = 60 * 30 * 1000): App? {

    val app = get(url)

    return when {
        app == null -> null
        app.isOld(duration) -> null
        else -> app
    }

}