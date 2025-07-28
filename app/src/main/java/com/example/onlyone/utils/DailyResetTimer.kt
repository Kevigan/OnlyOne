package com.example.onlyone.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

object DailyResetTimer {

    private val _timeUntilReset = MutableStateFlow(getMillisUntilNextUtcMidnight())
    val timeUntilReset: StateFlow<Long> = _timeUntilReset.asStateFlow()

    private var hasStarted = false

    private val listeners = mutableListOf<() -> Unit>()
    private var lastTriggerDay: String? = null

    fun start(listener: () -> Unit) {
        listeners.add(listener)

        if (hasStarted) return
        hasStarted = true

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val millis = getMillisUntilNextUtcMidnight()
                _timeUntilReset.value = millis

                val nowUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val todayKey = "${nowUtc.get(Calendar.YEAR)}-${nowUtc.get(Calendar.DAY_OF_YEAR)}"

                if (millis in 0..5_000L && todayKey != lastTriggerDay) {
                    lastTriggerDay = todayKey
                    listeners.forEach { it() }
                }


                delay(60_000L)
            }
        }
    }

    private fun getMillisUntilNextUtcMidnight(): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DATE, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis - System.currentTimeMillis()
    }
}
