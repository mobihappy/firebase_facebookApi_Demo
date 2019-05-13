package com.example.demoapi

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
@SuppressLint("LongLogTag")
class FirebaseMessagingService: FirebaseMessagingService() {
    val TAG = "FirebaseMessagingService"


    override fun onMessageReceived(p0: RemoteMessage?) {
        Log.d(TAG, "From: " + p0!!.from)

        if (p0!!.notification != null) {
            Log.d("title log",p0!!.notification?.title)
            showNotification(p0!!.notification?.title, p0!!.notification?.body)

        }

    }

    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(p0: String?) {
        Log.d(TAG, "Refreshed token: " + p0)

        sendRegistrationToServer(p0)
    }

    private fun sendRegistrationToServer(token: String?){
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.edit().putString(Constants.FIREBASE_TOKEN, token).apply()
    }


}