package com.amicus.loadingapp

import java.util.*

object ProgressManager {
    fun updateProgress(): Pair<Double, String> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val startOfYear = Calendar.getInstance().apply {
            set(currentYear, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfYear = Calendar.getInstance().apply {
            set(currentYear + 1, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()
        val totalYearMillis = endOfYear.timeInMillis - startOfYear.timeInMillis
        val elapsedMillis = now - startOfYear.timeInMillis

        val progress = (elapsedMillis.toDouble() / totalYearMillis) * 100

        // Ограничиваем прогресс максимумом 100%
        val clampedProgress = progress.coerceAtMost(100.0)

        val formattedProgress = String.format(java.util.Locale.US, "%.8f", clampedProgress)
        return Pair(clampedProgress, "$currentYear is $formattedProgress% complete")
    }

    fun getProgressForDate(year: Int, month: Int, dayOfMonth: Int, hour: Int = 0, minute: Int = 0): Double {
        val startOfYear = Calendar.getInstance().apply {
            set(year, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endOfYear = Calendar.getInstance().apply {
            set(year + 1, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetDate = Calendar.getInstance().apply {
            set(year, month, dayOfMonth, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalYearMillis = endOfYear.timeInMillis - startOfYear.timeInMillis
        val elapsedMillis = targetDate.timeInMillis - startOfYear.timeInMillis

        val progress = (elapsedMillis.toDouble() / totalYearMillis) * 100
        return progress.coerceIn(0.0, 100.0)
    }

    fun getTimeForProgress(year: Int, progressPercent: Double): Long {
        val startOfYear = Calendar.getInstance().apply {
            set(year, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endOfYear = Calendar.getInstance().apply {
            set(year + 1, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalYearMillis = endOfYear.timeInMillis - startOfYear.timeInMillis
        val progressMillis = (totalYearMillis * progressPercent / 100.0).toLong()

        return startOfYear.timeInMillis + progressMillis
    }
}
