package com.wozart.aura.utilities

import android.content.Context
import androidx.preference.PreferenceManager


/**
 * Created by Saif on 02/01/21.
 * mds71964@gmail.com
 */
class MyPreferences(context: Context) {
    companion object {
        private const val DARK_STATUS = "io.github.manuelernesto.DARK_STATUS"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var darkMode = preferences.getInt(DARK_STATUS, 2)
        set(value) = preferences.edit().putInt(DARK_STATUS, value).apply()
}