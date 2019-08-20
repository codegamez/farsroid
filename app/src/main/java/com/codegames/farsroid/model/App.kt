package com.codegames.farsroid.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class App(
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "title") val title: String = "",
    @PrimaryKey
    @ColumnInfo(name = "link") val link: String = "",
    @ColumnInfo(name = "image_url") val imageUrl: String = "",
    @ColumnInfo(name = "mode") val mode: String = "",
    @ColumnInfo(name = "type") val type: String = "",
    @ColumnInfo(name = "download_count") val downloadCount: String = "",
    @ColumnInfo(name = "last_update") val lastUpdate: String = "",
    @ColumnInfo(name = "comment_count") val commentCount: String = "",
    @ColumnInfo(name = "version") val version: String = "",
    @ColumnInfo(name = "like_count") val likeCount: String = "",
    @ColumnInfo(name = "content") val content: String = "",
    @ColumnInfo(name = "visit_count") val visitCount: String = "",
    @ColumnInfo(name = "content_less") val contentLess: String = "",
    @ColumnInfo(name = "download_links") val downloadLinks: Array<Pair<String, String>> = arrayOf(),
    @ColumnInfo(name = "category_link") val categoryLink: String = "",
    @ColumnInfo(name = "category_name") val categoryName: String = "",
    @ColumnInfo(name = "rate") val rate: String = "",
    @ColumnInfo(name = "info_list") val infoList: Array<String> = arrayOf(),
    @ColumnInfo(name = "screenshot_list") val screenshotList: Array<String> = arrayOf(),
    @ColumnInfo(name = "updated") var updated: Long = 0,
    @ColumnInfo(name = "seen") var seen: Long = 0
)
