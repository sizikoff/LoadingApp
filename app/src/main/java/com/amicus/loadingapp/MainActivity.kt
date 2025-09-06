package com.amicus.loadingapp

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var alarmManager: AlarmManager
    private lateinit var progressTextView: TextView
    private lateinit var gifImageView: ImageView

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "year_progress_channel"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        const val EXACT_ALARM_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация Views
        progressTextView = findViewById(R.id.progressTextView)
        gifImageView = findViewById(R.id.gifImageView)

        // Инициализация системных сервисов
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        sharedPreferences = getSharedPreferences("YearProgressPrefs", Context.MODE_PRIVATE)

        // Загрузка GIF
        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_launcher_background)
            .into(gifImageView)

        // Создание канала уведомлений
        createNotificationChannel()

        // Проверка разрешений
        checkAndRequestPermissions()

        // Проверка и обновление года
        checkAndUpdateYear()

        // Запуск отслеживания прогресса
        startProgressTracking()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Прогресс года"
            val descriptionText = "Уведомления о прогрессе года"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestPermissions() {
        // Проверка разрешения на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }

        // Проверка разрешения на точные уведомления для Android 12+
        checkExactAlarmPermission()
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivityForResult(intent, EXACT_ALARM_REQUEST_CODE)
            }
        }
    }

    private fun checkAndUpdateYear() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val storedYear = sharedPreferences.getInt("lastUpdatedYear", currentYear)

        if (currentYear > storedYear) {
            // Новый год наступил
            sharedPreferences.edit().apply {
                putInt("lastUpdatedYear", currentYear)
                putFloat("lastProgressPercent", 0f)
                apply()
            }

            // Показываем уведомления о новом годе
            NotificationHelper.showNewYearNotifications(this, storedYear, currentYear)

            // Планируем уведомления на новый год
            scheduleNotificationsForYear(currentYear)
        } else {
            // Планируем уведомления на текущий год (если еще не запланированы)
            scheduleNotificationsForYear(currentYear)
        }
    }

    private fun scheduleNotificationsForYear(year: Int) {
        val alarmScheduler = AlarmScheduler(this)
        alarmScheduler.scheduleProgressNotifications(year)
    }

    private fun startProgressTracking() {
        lifecycleScope.launch {
            var lastNotifiedPercent = sharedPreferences.getFloat("lastProgressPercent", 0f)

            while (isActive) {
                val (progress, text) = ProgressManager.updateProgress()
                progressTextView.text = text

                // Проверяем, увеличился ли прогресс на 1%
                val currentPercent = progress.toFloat()
                val currentPercentInt = currentPercent.toInt()
                val lastPercentInt = lastNotifiedPercent.toInt()

                if (currentPercentInt > lastPercentInt) {
                    // Прогресс увеличился на 1% или больше
                    sharedPreferences.edit()
                        .putFloat("lastProgressPercent", currentPercent)
                        .apply()

                    // Показываем уведомление
                    NotificationHelper.showProgressNotification(
                        this@MainActivity,
                        currentPercentInt,
                        Calendar.getInstance().get(Calendar.YEAR)
                    )

                    lastNotifiedPercent = currentPercent
                }

                // Проверяем, не достигли ли мы 100%
                if (currentPercent >= 100.0 && lastNotifiedPercent < 100.0) {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    NotificationHelper.showYearCompleteNotification(this@MainActivity, currentYear)

                    sharedPreferences.edit()
                        .putFloat("lastProgressPercent", 100f)
                        .apply()

                    lastNotifiedPercent = 100f
                }

                delay(1000) // Проверяем каждую секунду
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение получено, можем отправлять уведомления
                } else {
                    // Разрешение отклонено, уведомления работать не будут
                }
            }
        }
    }
}
