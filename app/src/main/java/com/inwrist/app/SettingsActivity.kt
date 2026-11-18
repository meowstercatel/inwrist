package com.inwrist.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.inwrist.app.data.TokenStore

class SettingsActivity : Activity() {

    private lateinit var tokens: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        tokens = TokenStore(this)
        findViewById<View>(R.id.logout).setOnClickListener {
            tokens.clear()
            render()
        }
        render()
    }

    private fun render() {
        val loggedIn = tokens.isLoggedIn
        findViewById<TextView>(R.id.account_status)
            .setText(if (loggedIn) R.string.logged_in else R.string.logged_out)
        findViewById<View>(R.id.logout).visibility = if (loggedIn) View.VISIBLE else View.GONE
    }
}
