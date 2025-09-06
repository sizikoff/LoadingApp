package com.amicus.loadingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PROGRESS_NOTIFICATION = "com.amicus.loadingapp.PROGRESS_NOTIFICATION"
        const val ACTION_NEW_YEAR_COMPLETE = "com.amicus.loadingapp.NEW_YEAR_COMPLETE"
        const val ACTION_NEW_YEAR_START = "com.amicus.loadingapp.NEW_YEAR_START"

        const val EXTRA_PROGRESS_PERCENT = "progress_percent"
        const val EXTRA_YEAR = "year"
        const val EXTRA_OLD_YEAR = "old_year"
        const val EXTRA_NEW_YEAR = "new_year"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received intent: ${intent.action}")

        when (intent.action) {
            ACTION_PROGRESS_NOTIFICATION -> {
                val progressPercent = intent.getIntExtra(EXTRA_PROGRESS_PERCENT, 0)
                val year = intent.getIntExtra(EXTRA_YEAR, 2024)

                Log.d("NotificationReceiver", "Progress notification: $progressPercent% for year $year")
                NotificationHelper.showScheduledProgressNotification(context, progressPercent, year)
            }

            ACTION_NEW_YEAR_COMPLETE -> {
                val oldYear = intent.getIntExtra(EXTRA_OLD_YEAR, 2024)

                Log.d("NotificationReceiver", "Year complete notification for year: $oldYear")
                NotificationHelper.showYearCompleteNotification(context, oldYear)
            }

            ACTION_NEW_YEAR_START -> {
                val oldYear = intent.getIntExtra(EXTRA_OLD_YEAR, 2024)
                val newYear = intent.getIntExtra(EXTRA_NEW_YEAR, 2025)

                Log.d("NotificationReceiver", "New year notifications: $oldYear -> $newYear")
                NotificationHelper.showNewYearNotifications(context, oldYear, newYear)
            }
        }
    }
}
