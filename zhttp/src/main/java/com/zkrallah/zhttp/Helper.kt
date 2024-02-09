package com.zkrallah.zhttp

import android.os.Handler
import android.os.Looper

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
}