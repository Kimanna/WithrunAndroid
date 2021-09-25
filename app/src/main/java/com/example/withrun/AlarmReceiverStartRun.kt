package com.example.withrun

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log

class AlarmReceiverStartRun: BroadcastReceiver() {

    companion object {
        const val TAG = "AlarmReceiver"
        const val NOTIFICATION_ID = 0
        const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
    }

    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent roomno : ${intent.getIntExtra("roomNo",0)}")

            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            createNotificationChannel()
            deliverNotification(context, intent)


    }

    private fun deliverNotification(context: Context, intent: Intent) {
        val contentIntent = Intent(context, Home::class.java)
        contentIntent.putExtra("location","AlarmReceiverStartRun")
        contentIntent.putExtra("roomNo", intent.getIntExtra("roomNo",0))
        contentIntent.putExtra("longStartTime", intent.getStringExtra("longStartTime"))

        contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder =
            NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("경기시작")
                .setContentText("경기가 시작되었습니다. 경기 진행을 위해 입장해 주세요.")
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                "Stand up notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "AlarmManager"
            notificationManager.createNotificationChannel(
                notificationChannel)
        }
    }

    //백그라운드일때만 푸시메세지 보내고싶을 때 사용
    private fun isAppRunning(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        for (i in procInfos.indices) {
            if (procInfos[i].processName == context.packageName) {
                Log.d(TAG, procInfos[i].processName)
                return true
            }
        }
        return false
    }

}