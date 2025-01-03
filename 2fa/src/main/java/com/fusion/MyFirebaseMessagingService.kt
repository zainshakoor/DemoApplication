package com.fusion


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.fusion.twofa.utils.FireBaseHelper
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyFirebaseMessagingService : FirebaseMessagingService() {


    companion object {

        private val TAG = MyFirebaseMessagingService::class.java.simpleName
        fun getFirebaseToken(onComplete: (String?) -> Unit) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                onComplete(it)
            }.addOnFailureListener {
                onComplete(null)
            }
        }
    }

    init {
        Log.d(TAG, "Firebase Service Initialized ")
        getFirebaseToken {
            Log.d(TAG, "Firebase token: $it")
        }
    }


    override fun onNewToken(newToken: String) {

        Log.d(TAG, "New Firebase token received: $newToken")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "onMessageReceived")
        Log.d(TAG, "New Firebase message received from: ${remoteMessage.data}")
        handleRemoteMessage(remoteMessage)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleRemoteMessage(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val remoteMessageData = remoteMessage.data
            val title = remoteMessageData["title"]
            val msg = remoteMessageData["challenge"]
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            Log.d(TAG, " Challenge Key Message data payload: ${msg}")
            val challenge = remoteMessageData["challenge"]

            if (!challenge.isNullOrEmpty()) {
                val sharedPreferences =
                    getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString("challenge", challenge)
                    apply()
                }
                Log.d(TAG, "Updated challenge key saved to SharedPreferences: $challenge")
            }

            // Extract values from the data map
            val type = remoteMessageData["type"]
            val token = remoteMessageData["token"]
            val deviceInfo = remoteMessageData["deviceInfo"]
            val hashMessage = remoteMessageData["hash_message"]

            Log.d("FCM", "Received type: $type")
            Log.d("FCM", "Received token: $token")
            Log.d("FCM", "Received deviceInfo: $deviceInfo")
            Log.d("FCM", "Received hash_message: $hashMessage")

            if (type == "2fa") {
                if (deviceInfo == FireBaseHelper.devInfo && hashMessage == FireBaseHelper.shaMsg) {
                    Log.d("Hello", "ShowDialog")
                    CoroutineScope(Dispatchers.Main).launch {

                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {

                    }
                }
            } else {
                Log.d("Hello", "Showing the Things: ")
            }


        } else {
            remoteMessage.notification?.let {
                displayNotification(
                    context = this,
                    contentTitle = it.title.orEmpty(),
                    contentMessage = it.body.orEmpty()
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayNotification(
        context: Context,
        contentTitle: String = "Two FA",
        contentMessage: String,
    ) {
        val channelId = "channelIDTest"
        Log.d(TAG, "remoteMessageNotification")

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(contentTitle)
            .setContentText(contentMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentMessage))
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setDefaults(Notification.DEFAULT_VIBRATE)
//            .setSmallIcon(R.drawable.ic_username)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Two FA",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(1001, notificationBuilder.build())
    }
}
