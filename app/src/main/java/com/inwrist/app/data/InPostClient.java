package com.inwrist.app.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class InPostClient {

    public static final class AuthException extends IOException {
        AuthException(String message) {
            super(message);
        }
    }

    private static final String BASE_URL = "https://api-inmobile-pl.easypack24.net/";
    private static final String CLIENT_ID = "inpost-mobile";
    private static final String USER_AGENT =
            "InPost Mobile/4.23.1(42301000) (Android 14; Wear OS; PL pl-PL)";
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("dd.MM, HH:mm");

    private final TokenStore tokens;

    public InPostClient(TokenStore tokens) {
        this.tokens = tokens;
    }

    public List<Parcel> fetchTrackedParcels() throws IOException {
        if (!tokens.isLoggedIn()) throw new AuthException("Not logged in");
        if (tokens.accessToken() == null) refreshAccessToken();

        Response response = get("v4/parcels/tracked");
        if (response.code == 401) {
            refreshAccessToken();
            response = get("v4/parcels/tracked");
        }
        if (response.code == 401) throw new AuthException("Session expired");
        if (response.code != 200) throw new IOException("HTTP " + response.code);

        try {
            return parseParcels(new JSONObject(response.body));
        } catch (JSONException e) {
            throw new IOException("Bad response", e);
        }
    }

    private void refreshAccessToken() throws IOException {
        String form = "client_id=" + encode(CLIENT_ID)
                + "&grant_type=refresh_token"
                + "&refresh_token=" + encode(tokens.refreshToken());
        Response response = request("POST", "global/oauth2/token", false,
                "application/x-www-form-urlencoded", form);
        if (response.code == 400 || response.code == 401) {
            tokens.clear();
            throw new AuthException("Refresh token rejected");
        }
        if (response.code != 200) throw new IOException("Token refresh HTTP " + response.code);
        try {
            JSONObject json = new JSONObject(response.body);
            tokens.save(json.getString("access_token"), json.optString("refresh_token", null));
        } catch (JSONException e) {
            throw new IOException("Bad token response", e);
        }
    }

    private Response get(String path) throws IOException {
        return request("GET", path, true, null, null);
    }

    private Response request(String method, String path, boolean authorized,
                             String contentType, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Language", "pl-PL");
            connection.setRequestProperty("X-App-Id", "pl.inpost.inmobile");
            if (authorized) {
                connection.setRequestProperty("Authorization", "Bearer " + tokens.accessToken());
            }
            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", contentType);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int code = connection.getResponseCode();
            InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            return new Response(code, readAll(stream));
        } finally {
            connection.disconnect();
        }
    }

    static List<Parcel> parseParcels(JSONObject root) {
        List<Parcel> result = new ArrayList<>();
        JSONArray parcels = root.optJSONArray("parcels");
        if (parcels == null) return result;
        for (int i = 0; i < parcels.length(); i++) {
            JSONObject json = parcels.optJSONObject(i);
            if (json == null) continue;
            String number = json.optString("shipmentNumber", "");
            JSONObject point = json.optJSONObject("pickUpPoint");
            result.add(new Parcel(
                    number,
                    number,
                    senderName(json.optJSONObject("sender")),
                    mapStatus(json.optString("status", "")),
                    point == null ? "-" : point.optString("name", "-"),
                    point == null ? "" : address(point.optJSONObject("addressDetails")),
                    nonEmpty(json.optString("openCode", null), "------"),
                    formatExpiry(json.optString("expiryDate", null))));
        }
        return result;
    }

    static ParcelStatus mapStatus(String status) {
        switch (status) {
            case "READY_TO_PICKUP":
            case "STACK_IN_BOX_MACHINE":
                return ParcelStatus.READY_TO_PICKUP;
            case "DELIVERED":
                return ParcelStatus.PICKED_UP;
            default:
                return ParcelStatus.IN_TRANSIT;
        }
    }

    private static String senderName(JSONObject sender) {
        if (sender == null) return "InPost";
        String name = nonEmpty(sender.optString("name", null), null);
        if (name == null) name = nonEmpty(sender.optString("companyName", null), null);
        return name == null ? "InPost" : name;
    }

    private static String address(JSONObject details) {
        if (details == null) return "";
        String street = details.optString("street", "");
        String building = details.optString("buildingNumber", "");
        String city = details.optString("city", "");
        String line = (street + " " + building).trim();
        if (city.isEmpty()) return line;
        return line.isEmpty() ? city : line + ", " + city;
    }

    private static String formatExpiry(String iso) {
        if (iso == null || iso.isEmpty() || "null".equals(iso)) return "-";
        try {
            return ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.systemDefault()).format(EXPIRY_FORMAT);
        } catch (RuntimeException e) {
            return iso;
        }
    }

    private static String nonEmpty(String value, String fallback) {
        return value == null || value.isEmpty() || "null".equals(value) ? fallback : value;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String readAll(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (InputStream in = stream) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static final class Response {
        final int code;
        final String body;

        Response(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }
}
