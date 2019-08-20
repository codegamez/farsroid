package com.codegames.farsroid.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Page(
    @PrimaryKey
    @ColumnInfo(name = "url") val url: String = "",
    @ColumnInfo(name = "data") val data: String = "",
    @ColumnInfo(name = "updated") var updated: Long = 0
)