package com.codegames.farsroid

import android.app.Application
import androidx.room.Room
import com.codegames.farsroid.api.FarsroidApi
import com.codegames.farsroid.database.AppDatabase
import retrofit2.Retrofit


@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val retrofit = Retrofit.Builder()
            .baseUrl("$BASEURL/")
            .build()

        service = retrofit.create(FarsroidApi::class.java)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()

    }

}