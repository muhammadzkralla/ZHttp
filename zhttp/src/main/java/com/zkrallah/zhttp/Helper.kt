package com.zkrallah.zhttp

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

/**
 * Helper function for calling the callback on the main thread.
 */
object Helper {

    /**
     * Helper function to call the callback on the main thread.
     *
     * @param response Successful HttpResponse.
     * @param e Exception that occurred during the request.
     * @param callback Callback to handle the response or exception.
     */
    fun <T> callOnMainThread(
        response: Response<T>?, e: Exception?, callback: ZListener<T>
    ) {
        Handler(Looper.getMainLooper()).post {
            response?.let {
                callback.onSuccess(it)
            }
            e?.let {
                callback.onFailure(it)
            }
        }
    }

    inline fun <reified T> Gson.fromJson(json: String?): T? {
        return json?.let {
            val type = object : TypeToken<T>() {}.type
            this.fromJson(json, type)
        } ?: run {
            null
        }
    }

    inline fun <reified T> Gson.deserializeBody(body: String?): T? {
        return try {
            this.fromJson<T>(body)
        } catch (e: JsonParseException) {
            if (T::class.java == String::class.java) body as T
            else null
        } catch (e: Exception) {
            Log.e("ZGet", "deserializeBody: $e", e)
            null
        }
    }
}