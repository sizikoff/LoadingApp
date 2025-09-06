package com.amicus.loadingapp


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    fun showProgressNotification(context: Context, progressPercent: Int, year: Int) {
        val title = "Прогресс года"
        val message = "$year is $progressPercent% complete"

        val notification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background) // Замените на вашу иконку
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(progressPercent, notification)
        } catch (e: SecurityException) {
            // Разрешение на уведомления не получено
        }
    }

    fun showYearCompleteNotification(context: Context, year: Int) {
        val title = "Год завершен!"
        val message = "$year is complete"

        val notification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 300, 500))
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(year + 1000, notification) // Уникальный ID
        } catch (e: SecurityException) {
            // Разрешение на уведомления не получено
        }
    }

    fun showNewYearNotifications(context: Context, oldYear: Int, newYear: Int) {
        // Уведомление о завершении старого года
        val completeTitle = "Год завершен!"
        val completeMessage = "$oldYear is complete"

        val completeNotification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(completeTitle)
            .setContentText(completeMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 300, 500))
            .build()

        // Уведомление о начале нового года
        val newYearTitle = "Новый год начался!"
        val newYearMessage = "$newYear is 0% complete"

        val newYearNotification = NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(newYearTitle)
            .setContentText(newYearMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300, 200, 300))
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            // Показываем уведомления с небольшим интервалом
            notificationManager.notify(9999, completeNotification)

            // Показываем второе уведомление через 2 секунды (в реальном приложении)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    notificationManager.notify(10000, newYearNotification)
                } catch (e: SecurityException) {
                    // Разрешение на уведомления не получено
                }
            }, 2000)

        } catch (e: SecurityException) {
            // Разрешение на уведомления не получено
        }
    }

    fun showScheduledProgressNotification(context: Context, progressPercent: Int, year: Int) {
        // Вызывается из AlarmReceiver для запланированных уведомлений
        showProgressNotification(context, progressPercent, year)
    }
}
