package com.example.mywiselaundrylife.serve

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mywiselaundrylife.act.FCMActivity
import com.example.mywiselaundrylife.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isEmpty()){
            Log.d(TAG, "${remoteMessage.data}")
            sendNotification(
                remoteMessage.data["title"].toString(),
                remoteMessage.data["body"].toString()
            )
        } else {
            remoteMessage.notification?.let {
                sendNotification(
                    remoteMessage.notification!!.title.toString(),
                    remoteMessage.notification!!.body.toString()
                )
            }
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    // 메시지에 데이터 페이로드가 포함 되어 있을 때 실행되는 메서드
    // 장시간 실행 (10초 이상) 작업의 경우 WorkManager를 사용하여 비동기 작업을 예약한다.
    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
    }

    // 메시지에 데이터 페이로드가 포함 되어 있을 때 실행되는 메서드
    // 10초 이내로 걸릴 때 메시지를 처리한다.
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }


    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(title:String, body:String){
        val intent = Intent(this, FCMActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_local_laundry_service_24)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())

    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            return Result.success()
        }
    }


}