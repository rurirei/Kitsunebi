package io.github.rurirei.kitsunebi

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


object PreferencesUtil {

    fun SharedPreferences.put(key: String, value: Int) {
        this.edit().putInt(key, value).apply()
    }

    fun SharedPreferences.put(key: String, value: Boolean) {
        this.edit().putBoolean(key, value).apply()
    }

    fun SharedPreferences.put(key: String, value: String) {
        this.edit().putString(key, value).apply()
    }

    fun SharedPreferences.get(key: String, default: Int): Int {
        return this.getInt(key, default)
    }

    fun SharedPreferences.get(key: String, default: Boolean): Boolean {
        return this.getBoolean(key, default)
    }

    fun SharedPreferences.get(key: String, default: String): String {
        return this.getString(key, default).toString()
    }

    val Context.sharedPreferences: SharedPreferences get() =
            PreferenceManager.getDefaultSharedPreferences(this)

}
