package com.vitalflowapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reminders will be rescheduled when the app is opened
            // For a more robust solution, you could use WorkManager here
        }
    }
}
