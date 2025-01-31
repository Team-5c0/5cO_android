package com.example.mywiselaundrylife.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MyWiseLaundryLifeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createMessagingChannel("fcm_default_channel", notificationManager)
    }
}

fun createMessagingChannel(getChannel: String, notificationManager: NotificationManager) {
    val existingChannel = notificationManager.getNotificationChannel(getChannel)

    // 이미 존재하는 채널이 없을 경우에만 생성
    if (existingChannel == null) {
        val channelName = "완료 알림"
        val channel = NotificationChannel(
            getChannel, channelName, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}