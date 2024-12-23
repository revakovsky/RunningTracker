package com.revakovskyi.core.domain.stopwatch

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object Stopwatch {

    /**
     * Emits the elapsed time in a flow at a fixed interval of 200 milliseconds.
     *
     * This function creates a cold [Flow] that continuously calculates the time difference
     * between consecutive emissions.
     *
     * @return A [Flow] of [Duration], representing the time elapsed since the last emission.
     */
    fun timeAndEmit(): Flow<Duration> {
        return flow {
            var lastEmitTime = System.currentTimeMillis()
            while (true) {
                delay(200L)
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - lastEmitTime
                emit(elapsedTime.milliseconds)
                lastEmitTime = currentTime
            }
        }
    }

}