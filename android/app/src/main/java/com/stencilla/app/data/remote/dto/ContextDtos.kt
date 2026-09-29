package com.stencilla.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val temperature: Double,
    val condition: String,
)

@Serializable
data class ForecastDay(
    val date: String,
    val high: Double,
    val low: Double,
    val condition: String,
)

@Serializable
data class ForecastResponse(val days: List<ForecastDay>)
