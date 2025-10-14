package com.onlyone.app.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

object DailyResetTimer {

    private val _timeUntilReset = MutableStateFlow(getMillisUntilNextUtcMidnight())
    val timeUntilReset: StateFlow<Long> = _timeUntilReset.asStateFlow()

    private var started = false
    private val listeners = mutableListOf<() -> Unit>()

    fun start(listener: () -> Unit) {
        listeners.add(listener)
        if (started) return
        started = true

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val millis = getMillisUntilNextUtcMidnight()
                _timeUntilReset.value = millis

                // Wait exactly until next midnight (add a small buffer)
                kotlinx.coroutines.delay(millis + 1000L)

                // Update countdown immediately after wake-up
                _timeUntilReset.value = getMillisUntilNextUtcMidnight()

                // Notify all listeners once per midnight tick
                listeners.forEach { it() }
            }
        }
    }

    private fun getMillisUntilNextUtcMidnight(): Long {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = now
            add(Calendar.DATE, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis - now
    }
}

