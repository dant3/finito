package com.github.dant3.finito.tests.snake

import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Timer(val tick: Duration, val callback: () -> Any) {
    private val paused = AtomicBoolean(false)
    private val tickThread = Thread {
        val currentThread = Thread.currentThread()
        while (!currentThread.isInterrupted) {
            while (isPaused) {
                Thread.sleep(100)
            }

            val initialTime = System.currentTimeMillis()
            val targetTime = tick.toMillis() + initialTime
            var diff = targetTime - initialTime
            while (diff > 0) {
                try {
                    Thread.sleep(diff)
                } catch (ignore: Exception) {}
                diff = targetTime - System.currentTimeMillis()
            }
            if (!currentThread.isInterrupted) {
                callback()
            }
        }
    }.apply {
        isDaemon = true
        name = "Timer with duration $tick #${globalCount.incrementAndGet()}"
    }

    var isPaused
        set(value) { this.paused.set(value) }
        get() = this.paused.get()
    fun start() { tickThread.start() }
    fun stop() { tickThread.interrupt() }
    val isStarted
        get() = tickThread.isAlive && !tickThread.isInterrupted

    companion object {
        private val globalCount = AtomicInteger(0)
    }
}