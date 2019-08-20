package com.codegames.farsroid

import android.content.SearchRecentSuggestionsProvider

class MySearchSuggestionProvider: SearchRecentSuggestionsProvider() {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.codegames.farsroid.MySearchSuggestionProvider"
        const val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }

}