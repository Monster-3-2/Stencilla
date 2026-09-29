package com.stencilla.app.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.WeatherResponse
import com.stencilla.app.data.remote.dto.ForecastDay
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class DeviceContext(val weather: WeatherResponse?, val calendarEvent: String?)
data class PlannerContext(val forecast: List<ForecastDay>, val events: Map<String, String>)

class ContextRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
) {
    suspend fun load(): DeviceContext {
        val location = location()
        val weather = location?.let { runCatching { api.getWeather(it.latitude, it.longitude) }.getOrNull() }
        return DeviceContext(weather, nextCalendarEvent())
    }

    suspend fun loadPlannerContext(): PlannerContext {
        val location = location()
        val forecast = location?.let { runCatching { api.getForecast(it.latitude, it.longitude) }.getOrDefault(com.stencilla.app.data.remote.dto.ForecastResponse(emptyList())).days }
            ?: emptyList()
        return PlannerContext(forecast, weekEvents())
    }

    private fun location() = if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        manager.getProviders(true).asSequence().mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
    } else null

    private fun nextCalendarEvent(): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return null
        val now = System.currentTimeMillis()
        val cursor = context.contentResolver.query(
            CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
                appendPath(now.toString()); appendPath((now + 24 * 60 * 60 * 1000).toString())
            }.build(), arrayOf(CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.TITLE),
            null, null, "${CalendarContract.Instances.BEGIN} ASC",
        )
        cursor?.use { if (it.moveToFirst()) return it.getString(1)?.takeIf(String::isNotBlank) }
        return null
    }

    private fun weekEvents(): Map<String, String> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) return emptyMap()
        val start = System.currentTimeMillis()
        val end = start + 7 * 24 * 60 * 60 * 1000L
        val result = linkedMapOf<String, String>()
        val cursor = context.contentResolver.query(
            CalendarContract.Instances.CONTENT_URI.buildUpon().apply {
                appendPath(start.toString()); appendPath(end.toString())
            }.build(), arrayOf(CalendarContract.Instances.BEGIN, CalendarContract.Instances.TITLE),
            null, null, "${CalendarContract.Instances.BEGIN} ASC",
        )
        cursor?.use {
            val beginIndex = it.getColumnIndex(CalendarContract.Instances.BEGIN)
            val titleIndex = it.getColumnIndex(CalendarContract.Instances.TITLE)
            while (it.moveToNext()) {
                val day = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(java.util.Date(it.getLong(beginIndex)))
                val title = it.getString(titleIndex)?.trim().orEmpty()
                if (title.isNotEmpty()) result[day] = listOfNotNull(result[day], title).joinToString(" · ").take(120)
            }
        }
        return result
    }
}
