package com.luisvicente.prontotix.service

object TrackingStatusManager {

    @Volatile
    var isRunning: Boolean = false
        private set

    fun setRunning(
        running: Boolean
    ) {
        isRunning = running
    }
}