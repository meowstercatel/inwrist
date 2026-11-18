package com.inwrist.app.data

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class InPostClient(private val tokens: TokenStore) {

    class AuthException(message: String) : IOException(message)

    private class Response(val code: Int, val body: String)

    @Throws(IOException::class)
    fun fetchTrackedParcels(): List<Parcel> {
        if (!tokens.isLoggedIn) throw AuthException("Not logged in")
        if (tokens.accessToken == null) refreshAccessToken()

        var response = get("v4/parcels/tracked")
        if (response.code == 401) {
            refreshAccessToken()
            response = get("v4/parcels/tracked")
        }
        if (response.code == 401) throw AuthException("Session expired")
        if (response.code != 200) throw IOException("HTTP ${response.code}")

        try {
            return parseParcels(JSONObject(response.body))
        } catch (e: JSONException) {
            throw IOException("Bad response", e)
        }
    }

    private fun refreshAccessToken() {
        val form = "client_id=${encode(CLIENT_ID)}" +
            "&grant_type=refresh_token" +
            "&refresh_token=${encode(tokens.refreshToken.orEmpty())}"
        val response = request(
            "POST", "global/oauth2/token", false,
            "application/x-www-form-urlencoded", form
        )
        if (response.code == 400 || response.code == 401) {
            tokens.clear()
            throw AuthException("Refresh token rejected")
        }
        if (response.code != 200) throw IOException("Token refresh HTTP ${response.code}")
        try {
            val json = JSONObject(response.body)
            tokens.save(json.getString("access_token"), nonEmpty(json.optString("refresh_token")))
        } catch (e: JSONException) {
            throw IOException("Bad token response", e)
        }
    }

    private fun get(path: String): Response = request("GET", path, true, null, null)

    private fun request(
        method: String,
        path: String,
        authorized: Boolean,
        contentType: String?,
        body: String?
    ): Response {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Accept-Language", "pl-PL")
            connection.setRequestProperty("X-App-Id", "pl.inpost.inmobile")
            if (authorized) {
                connection.setRequestProperty("Authorization", "Bearer ${tokens.accessToken}")
            }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", contentType)
                connection.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = connection.responseCode
            val stream = if (code >= 400) connection.errorStream else connection.inputStream
            return Response(code, readAll(stream))
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val BASE_URL = "https://api-inmobile-pl.easypack24.net/"
        private const val CLIENT_ID = "inpost-mobile"
        private const val USER_AGENT =
            "InPost Mobile/4.23.1(42301000) (Android 14; Wear OS; PL pl-PL)"
        private val EXPIRY_FORMAT = DateTimeFormatter.ofPattern("dd.MM, HH:mm")

        fun parseParcels(root: JSONObject): List<Parcel> {
            val parcels = root.optJSONArray("parcels") ?: return emptyList()
            return (0 until parcels.length()).mapNotNull { i ->
                val json = parcels.optJSONObject(i) ?: return@mapNotNull null
                val number = json.optString("shipmentNumber", "")
                val point = json.optJSONObject("pickUpPoint")
                Parcel(
                    id = number,
                    trackingNumber = number,
                    sender = senderName(json.optJSONObject("sender")),
                    status = mapStatus(json.optString("status", "")),
                    lockerName = point?.optString("name", "-") ?: "-",
                    lockerAddress = address(point?.optJSONObject("addressDetails")),
                    pickupCode = nonEmpty(json.optString("openCode")) ?: "------",
                    expiresAt = formatExpiry(nonEmpty(json.optString("expiryDate")))
                )
            }
        }

        fun mapStatus(status: String): ParcelStatus = when (status) {
            "READY_TO_PICKUP", "STACK_IN_BOX_MACHINE" -> ParcelStatus.READY_TO_PICKUP
            "DELIVERED" -> ParcelStatus.PICKED_UP
            else -> ParcelStatus.IN_TRANSIT
        }

        private fun senderName(sender: JSONObject?): String {
            if (sender == null) return "InPost"
            return nonEmpty(sender.optString("name"))
                ?: nonEmpty(sender.optString("companyName"))
                ?: "InPost"
        }

        private fun address(details: JSONObject?): String {
            if (details == null) return ""
            val street = details.optString("street", "")
            val building = details.optString("buildingNumber", "")
            val city = details.optString("city", "")
            val line = "$street $building".trim()
            return when {
                city.isEmpty() -> line
                line.isEmpty() -> city
                else -> "$line, $city"
            }
        }

        private fun formatExpiry(iso: String?): String {
            if (iso == null) return "-"
            return try {
                ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.systemDefault()).format(EXPIRY_FORMAT)
            } catch (e: RuntimeException) {
                iso
            }
        }

        private fun nonEmpty(value: String?): String? =
            if (value.isNullOrEmpty() || value == "null") null else value

        private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

        private fun readAll(stream: InputStream?): String =
            stream?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
    }
}
