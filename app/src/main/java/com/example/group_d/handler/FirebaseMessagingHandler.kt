package com.example.group_d.handler

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class FirebaseMessagingHandler {
    lateinit var applicationContext: Context
    fun sendMessageToTopic(topic: String){
        /*db.collection(COL_USER).document(otherUID).get().addOnSuccessListener {
            val token: String = it.data!!["token"] as String

            val message: RemoteMessage = RemoteMessage.Builder(token).addData("type", msgType).build()
            FirebaseMessaging.getInstance().send(message)
        }*/
        val FCM_API = "https://fcm.googleapis.com/fcm/send"
        val notification = JSONObject()
        val notifcationBody = JSONObject()
        try {
            notifcationBody.put("title", "Enter_title")
            notifcationBody.put("message", "MSG")   //Enter your notification message
            notification.put("to", topic)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
                //msg.setText("")
            },
            Response.ErrorListener {
                //Toast.makeText(this, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "key=AAAACHmFr9o:APA91bGypnZZt1R7IHHVja1sOAlualKFFRs4MwrWwdUpmZ4q9yNxVW4ZYDKJ9TiT1MdNwniO6P5At0v7V1yQgU5KkfITMcIGzOCnTnJU7whYoCmCjPCUYZi-pTDSe72RBLXOiHbyjhj9"
                params["Content-Type"] = "application/json"
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
}