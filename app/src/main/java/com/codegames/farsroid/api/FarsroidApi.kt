package com.codegames.farsroid.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface FarsroidApi {

    @GET(MAIN_PAGE)
    fun mainPage(): Call<ResponseBody>

    @GET(TOP_APPS)
    fun topApps(): Call<ResponseBody>

    @GET(TOP_GAMES)
    fun topGames(): Call<ResponseBody>

    @GET(MOST_DOWNLOADED_APPS)
    fun mostDownloadedApps(): Call<ResponseBody>

    @GET(MOST_DOWNLOADED_GAMES)
    fun mostDownloadedGames(): Call<ResponseBody>

    @GET(SUGGESTED_APPS)
    fun suggestedApps(): Call<ResponseBody>

    @GET(SUGGESTED_GAMES)
    fun suggestedGames(): Call<ResponseBody>

    @GET("{url}/page/{page}")
    fun appList(@Path("url") url: String, @Path("page") page: Int): Call<ResponseBody>

    @GET("search/{text}/page/{page}")
    fun search(@Path("text") text: String, @Path("page") page: Int): Call<ResponseBody>

    @GET("{url}")
    fun appPage(@Path("url") url: String): Call<ResponseBody>

    companion object {
        const val MAIN_PAGE = "/"
        const val TOP_APPS = "top-apps"
        const val TOP_GAMES = "top-games"
        const val MOST_DOWNLOADED_APPS = "most-downloaded-apps-of-month"
        const val MOST_DOWNLOADED_GAMES = "most-downloaded-games-of-month"
        const val SUGGESTED_GAMES = "cat/game/suggested-games"
        const val SUGGESTED_APPS = "cat/game/suggested-apps"
    }

}