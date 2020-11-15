package com.io.ellipse.data.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.io.ellipse.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {

    private val defaultChannelId: String = context.getString(R.string.channel_id)

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(defaultChannelId, name, importance)
            mChannel.description = descriptionText

            notificationManager.createNotificationChannel(mChannel)
        }
    }

    fun createNotification() = NotificationCompat.Builder(context, defaultChannelId)

    fun showNotification(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }
}