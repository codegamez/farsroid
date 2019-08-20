package com.codegames.farsroid

import kotlinx.coroutines.Job

const val JOB_GET_MAIN_PAGE = "get-main-page"
const val JOB_GET_TOP_APPS = "top-apps"
const val JOB_GET_MOST_DOWNLOADED_APPS = "most-downloaded-apps"
const val JOB_GET_SUGGESTED_APPS = "suggested-apps"
const val JOB_GET_TOP_GAMES = "top-games"
const val JOB_GET_MOST_DOWNLOADED_GAMES = "most-downloaded-games"
const val JOB_GET_SUGGESTED_GAMES = "suggested-games"
const val JOB_SEARCH = "get-search"

object SyncJob {

    private var uuid = 0
    private val jobs = hashMapOf<String, Job>()

    fun add(key: String, job: Job) = synchronized(this) {
        jobs[key] = job
    }

    fun add(job: Job) = synchronized(this) {
        val key = "_" + uuid++
        job.invokeOnCompletion {
            synchronized(this) { jobs.remove(key) }
        }
        jobs[key] = job
    }

    fun cancel(key: String) = synchronized(this) {
        val job = jobs[key] ?: return@synchronized
        if(job.isActive) job.cancel()
        jobs.remove(key)
    }

    fun cancelAll() = synchronized(this) {
        jobs.values.forEach {
            if(it.isActive) it.cancel()
        }
        jobs.clear()
    }

    fun get(key: String) = synchronized(this) {
        jobs[key]
    }

}