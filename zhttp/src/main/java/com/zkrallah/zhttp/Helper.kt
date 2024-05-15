package com.zkrallah.zhttp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

/**
 * Helper function for calling the callback on the main thread.
 */
object Helper {

    /**
     * Deserializes the specified JSON string into an object of the specified type [T].
     *
     * @param json The JSON string to deserialize.
     * @return The deserialized object of type [T], or `null` if the JSON string is `null`.
     */
    inline fun <reified T> Gson.fromJson(json: String?): T? {
        return json?.let {
            val type = object : TypeToken<T>() {}.type
            this.fromJson(json, type)
        } ?: run {
            null
        }
    }

    /**
     * Deserializes the specified JSON string into an object of the specified type [T].
     *
     * @param body The JSON string to deserialize.
     * @return The deserialized object of type [T], or `null` if an error occurs during deserialization.
     */
    inline fun <reified T> Gson.deserializeBody(body: String?): T? {
        return try {
            this.fromJson<T>(body)
        } catch (e: JsonParseException) {
            if (T::class.java == String::class.java) body as T
            else null
        } catch (e: Exception) {
            Log.e("ZHttp", "deserializeBody: $e", e)
            null
        }
    }
}