package com.example.covider.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import org.json.JSONObject


class MessagingService : FirebaseMessagingService(){
    public fun subscribeToCourse(context:Context, courseID: String){
        Firebase.messaging.subscribeToTopic(courseID)
            .addOnCompleteListener { task ->
                var msg = "Successfully subscribed to $courseID"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe to $courseID"
                }
                Log.d(TAG, msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification ${it.title}:${it.body}")
        }
    }

    public fun publishToCourse(context: Context, courseID: String, msg: String){
        val endpoint = "https://fcm.googleapis.com/fcm/send"
        val apikey = "AAAAOVbHT9Q:APA91bG5KWf9LcXMIbUX_xVpx86kdWQVyjamiZMxiHKN8Zc3WHBMN72oSdOQNzlbKbhlmW35d2BQwueDGjDzphrk6rfXGKYT2AgrnepXytvFtjQ_URaCh1-v5vxvFCGSNU3sjll1EKRB"//"AIzaSyAwQ_Y_4b7ZCdxMvvl4XoKL4aTQapY21c8"
        val queue = Volley.newRequestQueue(context)

        val notification = JSONObject()
        notification.put("body", msg)
        notification.put("title", "Course $courseID")

        val message = JSONObject()
        message.put("to", "/topics/$courseID")
        message.put("notification", notification)

        val body = JSONObject()
        body.put("message", message)

        Log.i(TAG, message.toString())

        val request = object: JsonObjectRequest(Method.POST, endpoint, message,
            Response.Listener { response ->
                if (response != null) {
                    Log.e("Your Array Response", response.toString())
                } else {
                    Log.e("Your Array Response", "Data Null")
                }
            },
            Response.ErrorListener { error ->
                Log.e("error is ", "" + error)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["authorization"] = "key=$apikey"
                return headers
            }
        }

        queue.add(request)
    }

    companion object {
        private const val TAG = "MessageService"
    }
}