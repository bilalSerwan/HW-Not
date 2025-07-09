package com.fastlink.huaweinotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import com.fastlink.huaweinotification.ui.theme.HuaweiNotificationTheme
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuaweiNotificationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Bilal",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

object PushServiceManager {
    private const val TAG = "PushServiceManager"

    fun isGmsAvailable(context: Context): Boolean {
        return try {
            val resultCode =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            Log.e(TAG, "GMS check error: ${e.message}")
            false
        }
    }

    fun isHmsAvailable(context: Context): Boolean {
        return try {
            val resultCode =
                HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            Log.e(TAG, "HMS check error: ${e.message}")
            false
        }
    }

    fun getPushToken(context: Context) {
        when {
            isGmsAvailable(context) -> {}
            isHmsAvailable(context) -> {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val token = HmsInstanceId.getInstance(context)
                                .getToken("113669113", "HCM")
                            Log.i(TAG, "HMS Token: $token")

                            withContext(Dispatchers.Main) {
                                sendTokenToServer(token, "HMS")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to get HMS token: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "HMS getToken error: ${e.message}")
                }
            }

            else -> {
                Log.w(TAG, "Neither GMS nor HMS is available")
            }
        }
    }

    private fun sendTokenToServer(token: String, type: String) {
        // TODO: Implement sending token to your server, Your server should store the token and its type (FCM or HMS)
    }
}


class HuaweiServices : HmsMessageService() {
    private val TAG = "HmsMessagingService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Received HMS push token: $token")
        // TODO: Send this token to your server
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG," onMessageReceived: $remoteMessage")
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "Received HMS push message")

        if (remoteMessage.data.isNotEmpty()) {
            Log.i(TAG, "Message data payload: ${remoteMessage.data}")
            val customData = remoteMessage.data
            Log.i(TAG, "Custom Data: $customData")
        } else {
            Log.w(TAG, "No data payload in the message")
        }

        remoteMessage.data.let { notification ->
            sendNotification(notification, notification)
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        Log.d(TAG, "Sending notification: Title: $title, Body: $body")
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "hms_channel",
                "HMS Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "hms_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
        notificationManager.notify(0, notificationBuilder.build())
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HuaweiNotificationTheme {
        Greeting("Android")
    }
}