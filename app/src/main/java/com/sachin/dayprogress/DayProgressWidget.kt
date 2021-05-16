package com.sachin.dayprogress

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class DayProgressWidget : AppWidgetProvider() {

    companion object {
        const val WIDGET_IDS_KEY = "mywidgetproviderwidgetids"
        const val DAYPROGRESS = "DAYPROGRESS"
        const val WAKEHOUR = "WAKEHOUR"
        const val WAKEMINUTE = "WAKEMINUTE"
        const val SLEEPHOUR = "SLEEPHOUR"
        const val SLEEPMINUTE = "SLEEPMINUTE"
    }

    private var mWakeHour = 7
    private var mWakeMinute = 30
    private var mSleepHour = 23
    private var mSleepMinute = 0
    private var mProgress = 0.5F

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.hasExtra(WIDGET_IDS_KEY) == true) {
            val ids = intent.extras?.getIntArray(WIDGET_IDS_KEY) ?: intArrayOf()
            onUpdate(context!!, AppWidgetManager.getInstance(context), ids)
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        getData(context)
        setProgress()
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.day_progress_widget)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        views.setOnClickPendingIntent(R.id.progressBar, pendingIntent);

        views.setProgressBar(R.id.progressBar, 100, (mProgress * 100).toInt(), false)
        views.setTextViewText(R.id.tvProgress,   "${(mProgress * 100).toInt()}%",)
        val textColor = when {
            mProgress < 0.25 -> context.getColor(R.color.green)
            mProgress < 0.5 -> context.getColor(R.color.yellow)
            mProgress < 0.75 -> context.getColor(R.color.orange)
            else -> context.getColor(R.color.red)
        }
        views.setTextColor(R.id.tvProgress, textColor)


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getData(context: Context) {
        val sharedPref = context.getSharedPreferences(DAYPROGRESS, Context.MODE_PRIVATE) ?: return
        mWakeHour = sharedPref.getInt(WAKEHOUR, 7)
        mWakeMinute = sharedPref.getInt(WAKEMINUTE, 30)
        mSleepHour = sharedPref.getInt(SLEEPHOUR, 23)
        mSleepMinute = sharedPref.getInt(SLEEPMINUTE, 0)
    }

    fun setProgress() {
        val todayAtFour = Date()
        todayAtFour.hours = 4

        val wakeUpTime = Date()
        wakeUpTime.hours = mWakeHour
        wakeUpTime.minutes = mWakeMinute
        val sleepTime = Date()
        sleepTime.hours = mSleepHour
        sleepTime.minutes = mSleepMinute

        val currentTime = Date()

        if(sleepTime < todayAtFour && currentTime > wakeUpTime) {
            sleepTime.date = sleepTime.date + 1
        }

        if(sleepTime < todayAtFour && currentTime < sleepTime) {
            wakeUpTime.date = wakeUpTime.date - 1
        }

        val timeSpent = currentTime.time - wakeUpTime.time
        val totalAwakeTime = sleepTime.time - wakeUpTime.time
        var progress = (timeSpent.toFloat() / totalAwakeTime.toFloat())
        // You are in your sleep cycle
        if(timeSpent < 0 && totalAwakeTime < 0){
            progress *= -1
        }
        mProgress = progress
    }
}