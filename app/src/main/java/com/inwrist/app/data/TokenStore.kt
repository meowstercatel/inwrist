package com.inwrist.app.data

import android.content.Context
import android.content.SharedPreferences

class TokenStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)

    val isLoggedIn: Boolean
        get() = refreshToken != null

    fun save(accessToken: String, refreshToken: String?) {
        val editor = prefs.edit().putString(KEY_ACCESS, accessToken)
        if (refreshToken != null) editor.putString(KEY_REFRESH, refreshToken)
        editor.apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS = "auth"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
