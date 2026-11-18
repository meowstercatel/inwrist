package com.inwrist.app.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class TokenStore {

    private static final String PREFS = "auth";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String accessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public String refreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean isLoggedIn() {
        return refreshToken() != null;
    }

    public void save(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit().putString(KEY_ACCESS, accessToken);
        if (refreshToken != null) editor.putString(KEY_REFRESH, refreshToken);
        editor.apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
