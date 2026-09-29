package com.stencilla.app.util

import retrofit2.HttpException

object ApiErrorParser {
    fun parse(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                val code = throwable.code()
                when (code) {
                    400 -> "Invalid request. Please check your input."
                    401 -> "Session expired. Please sign in again."
                    402 -> "Subscription required."
                    403 -> "Access denied."
                    404 -> "Not found."
                    413 -> "Image too large. Please use an image under 10 MB."
                    415 -> "Unsupported image format. Use JPEG, PNG or WebP."
                    429 -> "Too many requests. Please wait a moment."
                    500 -> "Server error. Please try again later."
                    502, 503 -> "Service unavailable. The server may be starting up — try again in 30 seconds."
                    else -> "Request failed ($code)."
                }
            }
            is java.net.SocketTimeoutException -> "Request timed out. Check your connection."
            is java.net.UnknownHostException  -> "No internet connection."
            is java.io.IOException            -> "Network error: ${throwable.message}"
            else -> throwable.message ?: "Something went wrong."
        }
    }

    fun messageFor(throwable: Throwable): String = parse(throwable)
}
