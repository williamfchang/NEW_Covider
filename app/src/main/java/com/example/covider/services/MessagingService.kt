package com.example.covider.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.covider.R
import com.example.covider.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging


class MessagingService : FirebaseMessagingService(){
    private val defaultChannelName = "default"
    private val channelID = "10001"

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var notificationID = 1

    private fun createNotificationChannel() {
        val name = defaultChannelName
        val descriptionText = "Covider App Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    public fun subscribeToCourse(courseID: String){
        Firebase.messaging.subscribeToTopic(courseID)
            .addOnCompleteListener { task ->
                var msg = "Successfully subscribed to $courseID"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe to $courseID"
                }
                Log.d(TAG, msg)
            }
    }

    public fun subToAllUserCourses(){
        val currUserID = auth.currentUser!!.uid
        db.collection("users").document(currUserID).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val user = task.result.toObject(User::class.java)
                    if (user != null) {
                        for (course in user.courses) {
                            subscribeToCourse(course)
                        }
                    }

                }
            }

    }

    public fun unsubscribeFromCourse(courseID: String){
        Firebase.messaging.unsubscribeFromTopic(courseID)
            .addOnCompleteListener { task ->
                var msg = "Successfully unsubscribed to $courseID"
                if (!task.isSuccessful) {
                    msg = "Failed to unsubscribe to $courseID"
                }
                Log.d(TAG, msg)
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification received: ${it.title}:${it.body}")

            // create the notification channel
            createNotificationChannel()

//            val intent = Intent(applicationContext, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }
//            val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelID)
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.notification_icon)
//                .setContentIntent(pendingIntent)
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationID++, builder.build())
            }
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val currUserID = auth.currentUser!!.uid

        // store the users current FCM token for notifications
        db.collection("users").document(currUserID).update("token", s)
    }

    public fun loadToken(){
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Saving user token to firebase")
                val currUserID = auth.currentUser!!.uid
                db.collection("users").document(currUserID).update("token", task.result)
            }
        }
    }

    companion object {
        private const val TAG = "MessageService"
    }
}