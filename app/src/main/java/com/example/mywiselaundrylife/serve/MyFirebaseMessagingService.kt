package com.example.mywiselaundrylife.serve

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.act.StartActivity
import com.example.mywiselaundrylife.app.createMessagingChannel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("Firebase", "${remoteMessage}")

        if (remoteMessage.data.isEmpty()){
            Log.d("Firebase", "${remoteMessage.data}")
            sendNotification(
                remoteMessage.data["title"].toString(),
                remoteMessage.data["body"].toString()
            )
        } else {
            remoteMessage.notification?.let {
                sendNotification(it.title ?: "알림", it.body ?: "내용")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    @SuppressLint("RemoteViewLayout")
    private fun msgView(title: String, message : String): RemoteViews {
        val remoteView = RemoteViews("com.example.mywiselaundrylife", R.layout.notification)
        remoteView.setTextViewText(R.id.msgTitle, title)
        remoteView.setTextViewText(R.id.msgCnt, message)
        remoteView.setImageViewResource(R.id.logo, R.drawable.logo)
        return remoteView
    }

    @SuppressLint("ObsoleteSdkInt", "WrongConstant")
    private fun sendNotification(title: String, body: String) {
        val intent = Intent(this, StartActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId ="fcm_default_channel"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.img)
            .setVibrate(longArrayOf(1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentTitle(title)
            .setContentText(body)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCustomContentView(msgView(title, body))

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}