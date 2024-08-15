package com.revakovskyi.run.presentation.activeRun.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.revakovskyi.core.peresentation.ui.formatted
import com.revakovskyi.run.domain.LocationManager
import com.revakovskyi.run.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class ActiveRunService : Service() {

    private val notificationManager by lazy { getSystemService<NotificationManager>()!! }

    private val baseNotification by lazy {
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(com.revakovskyi.core.presentation.designsystem.R.drawable.logo)
            .setContentTitle(getString(R.string.active_run))
    }

    private val locationManager by inject<LocationManager>()

    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start(extractActivityClass(intent))
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    private fun extractActivityClass(intent: Intent): Class<*> {
        val activityClassName = intent.getStringExtra(EXTRA_ACTIVITY_CLASS)
            ?: throw IllegalArgumentException(getString(R.string.no_activity_class_provided))
        return Class.forName(activityClassName)
    }

    private fun start(activityClass: Class<*>) {
        if (!isServiceActive) {
            isServiceActive = true
            createNotificationChannel()
            val notification = createNotificationWithPendingIntent(activityClass)
            startForeground(NOTIFICATION_ID, notification)
            updateNotification()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.active_run),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotificationWithPendingIntent(activityClass: Class<*>): Notification {
        val activityIntent = createActivityIntent(activityClass)
        val pendingIntent = createPendingIntent(activityIntent)

        return baseNotification
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createActivityIntent(activityClass: Class<*>): Intent {
        return Intent(applicationContext, activityClass).apply {
            data = DEEP_LINK.toUri()
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    private fun createPendingIntent(activityIntent: Intent): PendingIntent? {
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private fun updateNotification() {
        locationManager.elapsedTime
            .onEach { elapsedTime ->
                val notification = baseNotification
                    .setContentText(elapsedTime.formatted())
                    .build()

                notificationManager.notify(NOTIFICATION_ID, notification)
            }
            .launchIn(serviceScope)
    }

    private fun stop() {
        stopSelf()
        isServiceActive = false
        serviceScope.cancel()
        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }


    companion object {
        var isServiceActive = false

        const val DEEP_LINK = "running_tracker://active_run"

        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "active_run"
        private const val EXTRA_ACTIVITY_CLASS = "EXTRA_ACTIVITY_CLASS"
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        fun createServiceStartingIntent(context: Context, activityClass: Class<*>): Intent {
            return Intent(context, ActiveRunService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACTIVITY_CLASS, activityClass.name)
            }
        }

        fun createServiceStoppingIntent(context: Context): Intent {
            return Intent(context, ActiveRunService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

}
