package com.inwrist.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inwrist.app.data.TokenStore;

public class SettingsActivity extends Activity {

    private TokenStore tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tokens = new TokenStore(this);
        findViewById(R.id.logout).setOnClickListener(v -> {
            tokens.clear();
            render();
        });
        render();
    }

    private void render() {
        boolean loggedIn = tokens.isLoggedIn();
        this.<TextView>findViewById(R.id.account_status)
                .setText(loggedIn ? R.string.logged_in : R.string.logged_out);
        findViewById(R.id.logout).setVisibility(loggedIn ? View.VISIBLE : View.GONE);
    }
}
