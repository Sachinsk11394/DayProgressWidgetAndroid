package com.sachin.dayprogress

import android.R.attr
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sachin.dayprogress.DayProgressWidget.Companion.DAYPROGRESS
import com.sachin.dayprogress.DayProgressWidget.Companion.SLEEPHOUR
import com.sachin.dayprogress.DayProgressWidget.Companion.SLEEPMINUTE
import com.sachin.dayprogress.DayProgressWidget.Companion.WAKEHOUR
import com.sachin.dayprogress.DayProgressWidget.Companion.WAKEMINUTE
import java.util.*
import android.R.attr.data

import android.appwidget.AppWidgetManager

import android.content.Intent

import android.content.ComponentName
import com.sachin.dayprogress.DayProgressWidget.Companion.WIDGET_IDS_KEY


class MainActivity : ComponentActivity() {

    private val DarkGreen = Color(0xFF228B22)
    private val DarkRed = Color(0xFF8B0000)
    private val Orange = Color(0xFFFFA500)

    private var mWakeHour: MutableLiveData<Int> =
        MutableLiveData(7)
    private var mWakeMinute: MutableLiveData<Int> = MutableLiveData(30)
    private var mSleepHour: MutableLiveData<Int> =
        MutableLiveData(23)
    private var mSleepMinute: MutableLiveData<Int> = MutableLiveData(0)
    private var mProgress = MutableLiveData(0F)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getData(this)
        refreshWidget()
        setContent {
            MainContentView()
        }
    }

    @Composable
    fun MainContentView() {
        val wakeHour by mWakeHour.observeAsState(7)
        val wakeMinute by mWakeMinute.observeAsState(30)
        val sleepHour by mSleepHour.observeAsState(23)
        val sleepMinute by mSleepMinute.observeAsState(0)
        val progress by mProgress.observeAsState(0F)

        val wakeUpTimePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                mWakeHour.value = hourOfDay
                mWakeMinute.value = minute
                setProgress()
                saveData()
            }, wakeHour.toInt(), wakeMinute.toInt(), false
        )

        val sleepTimePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                mSleepHour.value = hourOfDay
                mSleepMinute.value = minute
                setProgress()
                saveData()
            }, sleepHour.toInt(), sleepMinute.toInt(), false
        )

        MaterialTheme {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(color = Color.Black, darkIcons = false)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color = Color.Black),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Wake up",
                    style = TextStyle(color = DarkGreen, fontSize = 30.sp),
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = { wakeUpTimePickerDialog.show() },
                    modifier = Modifier.width(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DarkGreen,
                        contentColor = Color.Transparent
                    )
                ) {
                    var wakeHourString = "${if(wakeHour > 12) wakeHour - 12 else wakeHour}"
                    wakeHourString = "${if(wakeHour.toString().length == 1) "0" else ""}${wakeHourString}"
                    val wakeAMPM = if(wakeHour > 12) "PM" else "AM"
                    Text(
                        text = "${wakeHourString}:${if (wakeMinute.toString().length == 1) "0" else ""}${wakeMinute} ${wakeAMPM}",
                        style = TextStyle(color = Color.White, fontSize = 25.sp)
                    )
                }
                Text(
                    text = "Sleep",
                    style = TextStyle(color = DarkRed),
                    fontSize = 30.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = { sleepTimePickerDialog.show() },
                    modifier = Modifier.width(150.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DarkRed,
                        contentColor = Color.Transparent
                    )
                ) {
                    var sleepHourString = "${if(sleepHour > 12) sleepHour - 12 else sleepHour}"
                    sleepHourString = "${if(sleepHour.toString().length == 1) "0" else ""}${sleepHourString}"
                    val sleepAMPM = if(sleepHour > 12) "PM" else "AM"
                    Text(
                        text = "${sleepHourString}:${if (sleepMinute.toString().length == 1) "0" else ""}${sleepMinute} ${sleepAMPM}",
                        style = TextStyle(color = Color.White, fontSize = 25.sp)
                    )
                }

                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                )
                Box (contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color.Green, progress = 1F, strokeWidth = 10.dp,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(23.dp)
                    )
                    CircularProgressIndicator(
                        color = Color.Red, progress = animatedProgress, strokeWidth = 10.dp,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(23.dp)
                    )

                    Text(text = "${(progress * 100).toInt()}%",
                        style = TextStyle(color = when {
                            progress < 0.25 -> Color.Green
                            progress < 0.5 -> Color.Yellow
                            progress < 0.75 -> Orange
                            else -> Color.Red
                        }, fontSize = 25.sp)
                    )
                }
                Text(
                    text = if(progress > 0F && progress < 1F)"${100 - (progress * 100).toInt()}% of your day remains" else "Sleep",
                    style = TextStyle(color = when {
                        progress < 0.25 -> Color.Green
                        progress < 0.5 -> Color.Yellow
                        progress < 0.75 -> Orange
                        else -> Color.Red}),
                    fontSize = 30.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        setProgress()
    }

    private fun setProgress() {
        val todayAtFour = Date()
        todayAtFour.hours = 4

        val wakeUpTime = Date()
        wakeUpTime.hours = mWakeHour.value ?: 7
        wakeUpTime.minutes = mWakeMinute.value ?: 30
        val sleepTime = Date()
        sleepTime.hours = mSleepHour.value ?: 23
        sleepTime.minutes = mSleepMinute.value ?: 0

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
        mProgress.value = progress
    }

    private fun saveData() {
        val sharedPref = getSharedPreferences(DAYPROGRESS, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt(WAKEHOUR, mWakeHour.value?:7)
            putInt(WAKEMINUTE, mWakeMinute.value?:30)
            putInt(SLEEPHOUR, mSleepHour.value?:23)
            putInt(SLEEPMINUTE, mSleepMinute.value?:0)
            apply()
        }

        refreshWidget()
    }

    private fun refreshWidget() {
        val man = AppWidgetManager.getInstance(this)
        val ids = man.getAppWidgetIds(
            ComponentName(this, DayProgressWidget::class.java)
        )
        val updateIntent = Intent()
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        updateIntent.putExtra(WIDGET_IDS_KEY, ids);
        sendBroadcast(updateIntent)
    }

    private fun getData(context: Context) {
        val sharedPref = context.getSharedPreferences(DAYPROGRESS, Context.MODE_PRIVATE) ?: return
        mWakeHour.value = sharedPref.getInt(WAKEHOUR, 7)
        mWakeMinute.value = sharedPref.getInt(WAKEMINUTE, 30)
        mSleepHour.value = sharedPref.getInt(SLEEPHOUR, 23)
        mSleepMinute.value = sharedPref.getInt(SLEEPMINUTE, 0)
    }
}