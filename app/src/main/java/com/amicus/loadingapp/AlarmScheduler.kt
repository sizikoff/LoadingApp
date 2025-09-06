package com.amicus.loadingapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleProgressNotifications(year: Int) {
        Log.d("AlarmScheduler", "Scheduling notifications for year: $year")

        // Проверяем разрешение на точные уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Cannot schedule exact alarms - permission not granted")
                return
            }
        }

        // Планируем уведомления для каждого процента (1-100%)
        for (percent in 1..100) {
            scheduleProgressNotification(year, percent)
        }

        // Планируем уведомления для нового года
        scheduleNewYearNotifications(year)
    }

    private fun scheduleProgressNotification(year: Int, percent: Int) {
        val targetTime = calculateTimeForProgress(year, percent)

        if (targetTime <= System.currentTimeMillis()) {
            // Время уже прошло, не планируем
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_PROGRESS_NOTIFICATION
            putExtra(NotificationReceiver.EXTRA_PROGRESS_PERCENT, percent)
            putExtra(NotificationReceiver.EXTRA_YEAR, year)
        }

        val requestCode = year * 1000 + percent // Уникальный код для каждого года и процента
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    pendingIntent
                )
            }

            Log.d("AlarmScheduler", "Scheduled notification for $percent% at ${Date(targetTime)}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Failed to schedule alarm for $percent%", e)
        }
    }

    private fun scheduleNewYearNotifications(currentYear: Int) {
        val nextYear = currentYear + 1

        // Время окончания текущего года
        val endOfYear = Calendar.getInstance().apply {
            set(nextYear, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Планируем уведомление о завершении года
        val completeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NEW_YEAR_COMPLETE
            putExtra(NotificationReceiver.EXTRA_OLD_YEAR, currentYear)
        }

        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            currentYear * 10000 + 1, // Уникальный код
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Планируем уведомление о начале нового года (через 5 секунд после окончания старого)
        val startIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_NEW_YEAR_START
            putExtra(NotificationReceiver.EXTRA_OLD_YEAR, currentYear)
            putExtra(NotificationReceiver.EXTRA_NEW_YEAR, nextYear)
        }

        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            currentYear * 10000 + 2, // Уникальный код
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Планируем уведомление о завершении года
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endOfYear,
                    completePendingIntent
                )

                // Планируем уведомление о начале нового года
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endOfYear + 5000, // +5 секунд
                    startPendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    endOfYear,
                    completePendingIntent
                )

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    endOfYear + 5000,
                    startPendingIntent
                )
            }

            Log.d("AlarmScheduler", "Scheduled new year notifications for $currentYear -> $nextYear")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Failed to schedule new year notifications", e)
        }
    }

    private fun calculateTimeForProgress(year: Int, percent: Int): Long {
        val startOfYear = Calendar.getInstance().apply {
            set(year, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endOfYear = Calendar.getInstance().apply {
            set(year + 1, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalYearMillis = endOfYear.timeInMillis - startOfYear.timeInMillis
        val progressMillis = (totalYearMillis * percent / 100.0).toLong()

        return startOfYear.timeInMillis + progressMillis
    }

    fun cancelAllNotifications(year: Int) {
        Log.d("AlarmScheduler", "Cancelling all notifications for year: $year")

        // Отменяем уведомления прогресса
        for (percent in 1..100) {
            val requestCode = year * 1000 + percent
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        // Отменяем уведомления нового года
        for (i in 1..2) {
            val requestCode = year * 10000 + i
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }
}
