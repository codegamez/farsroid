package com.codegames.farsroid.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codegames.farsroid.model.App
import com.codegames.farsroid.model.Page

@Database(entities = [App::class, Page::class], version = 3)
@TypeConverters(AppTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun pageDao(): PageDao
}