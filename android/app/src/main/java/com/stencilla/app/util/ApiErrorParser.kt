package com.stencilla.app.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

object ApiErrorParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun messageFor(throwable: Throwable): String {
        if (throwable is HttpException) {
            val body = throwable.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                runCatching {
                    val element = json.parseToJsonElement(body).jsonObject
                    return element["detail"]?.jsonPrimitive?.content ?: throwable.message()
                }
            }
            return throwable.message() ?: "Request failed"
        }
        return throwable.message ?: "Something went wrong. Check your connection and try again."
    }
}
