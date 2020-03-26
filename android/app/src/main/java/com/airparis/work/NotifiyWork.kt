/*
This file is part of airparis.

airparis is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or any 
later version.

airparis is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with airparis.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.airparis.work

import airparis.data.http.AirparifAPI
import airparis.data.http.model.util.Day
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.airparis.R
import com.airparis.activity.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotifyWork(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val id = inputData.getLong(NOTIFICATION_ID, 0).toInt()
        GlobalScope.launch {
            val indexList = AirparifAPI().requestIndex()
            val indexToday = indexList.firstOrNull { it.date == Day.TODAY.value }?.indice
            Log.d(NotifyWork::class.simpleName, "indexList = $indexList\nindexToday=$indexToday")
            if (indexToday != null) {
                sendNotification(id, indexToday.toString())
            } else {
                // TODO notification error
            }
        }
        return success()
    }

    private fun sendNotification(id: Int, notificationContentText: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val titleNotification = applicationContext.getString(R.string.app_name)
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.oxygen_24)
            .setContentTitle(titleNotification)
            .setContentText(notificationContentText)
            .setContentIntent(pendingIntent)
            .setPriority(PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL_ID)
            val channelDescription =
                applicationContext.getString(R.string.notification_channel_description)
            val channelName = applicationContext.getString(R.string.notification_channel_name)
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = channelDescription
                }
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
    }

    companion object {
        const val NOTIFICATION_ID = "airparis_notification_id"
        const val NOTIFICATION_CHANNEL_ID = "airparis_channel_01"
        const val NOTIFICATION_WORK = "airparis_notification_work"
    }
}