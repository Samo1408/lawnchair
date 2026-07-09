package com.superlauncher.util

import android.net.TrafficStats
import android.os.Handler
import android.os.Looper

class NetworkMonitor(private val onUpdate: (dlSpeed: Float, ulSpeed: Float) -> Unit) {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0
    private var lastTime: Long = 0
    private var running = false

    fun start() {
        if (running) return
        running = true
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (!running) return
                val now = System.currentTimeMillis()
                val rx = TrafficStats.getTotalRxBytes()
                val tx = TrafficStats.getTotalTxBytes()
                val elapsed = ((now - lastTime) / 1000f).coerceAtLeast(0.5f)
                val dlSpeed = ((rx - lastRxBytes) / elapsed / 1024f).coerceAtLeast(0f)
                val ulSpeed = ((tx - lastTxBytes) / elapsed / 1024f).coerceAtLeast(0f)
                onUpdate(dlSpeed, ulSpeed)
                lastRxBytes = rx; lastTxBytes = tx; lastTime = now
                handler?.postDelayed(this, 2000)
            }
        }
        handler?.post(runnable!!)
    }

    fun stop() {
        running = false
        handler?.removeCallbacks(runnable ?: return)
        handler = null; runnable = null
    }
}