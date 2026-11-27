package com.example.spamfilter

import android.content.Context

internal object KeywordStore {
    private fun preferences(context: Context) =
        context.getSharedPreferences("SpamFilter", Context.MODE_PRIVATE)

    fun load(context: Context): Set<String> =
        preferences(context).getStringSet("keywords", emptySet()).orEmpty()

    fun save(context: Context, keywords: Collection<String>) {
        preferences(context).edit().putStringSet("keywords", keywords.toSet()).apply()
    }
}
