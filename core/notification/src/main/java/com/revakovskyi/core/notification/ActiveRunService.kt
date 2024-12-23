package com.revakovskyi.core.notification

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import kotlin.time.Duration

/**
 * A foreground service for tracking an active run.
 *
 * This service handles notifications to keep users informed about the current run's duration and allows
 * the app to continue monitoring the run even when the app is in the background. It interacts with
 * the notification system and provides deep linking to navigate back to the active run screen.
 */
class ActiveRunService : Service() {

    private val notificationManager by lazy { getSystemService<NotificationManager>()!! }

    private val baseNotification by lazy {
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(com.revakovskyi.core.presentation.designsystem.R.drawable.logo)
            .setContentTitle(getString(R.string.active_run))
    }

    /***
     * Injected state flow tracking elapsed time for the run
     */
    private val elapsedTime by inject<StateFlow<Duration>>()

    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start(extractActivityClass(intent))
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    /**
     * Extracts the activity class from the intent, used to create a pending intent for navigation.
     *
     * @param intent The intent containing the activity class name.
     * @return The extracted class object.
     * @throws IllegalArgumentException If the activity class name is not provided.
     */
    private fun extractActivityClass(intent: Intent): Class<*> {
        val activityClassName = intent.getStringExtra(EXTRA_ACTIVITY_CLASS)
            ?: throw IllegalArgumentException(getString(R.string.no_activity_class_provided))
        return Class.forName(activityClassName)
    }

    private fun start(activityClass: Class<*>) {
        if (!_isServiceActive.value) {
            _isServiceActive.value = true
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
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Creates a notification with a pending intent for deep linking back to the active run screen.
     *
     * @param activityClass The class to navigate back to.
     * @return The built notification.
     */
    private fun createNotificationWithPendingIntent(activityClass: Class<*>): Notification {
        val activityIntent = createActivityIntent(activityClass)
        val pendingIntent = createPendingIntent(activityIntent)

        return baseNotification
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)
            .build()
    }

    /**
     * Creates an intent for the activity to navigate back to from the notification.
     */
    private fun createActivityIntent(activityClass: Class<*>): Intent {
        return Intent(applicationContext, activityClass).apply {
            data = DEEP_LINK.toUri()
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    /**
     * Creates a pending intent with a back stack for navigation.
     *
     * @param activityIntent The intent to the target activity.
     * @return The created pending intent.
     */
    private fun createPendingIntent(activityIntent: Intent): PendingIntent? {
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
    }

    /**
     * Updates the notification with the current elapsed time using a coroutine flow.
     */
    private fun updateNotification() {
        elapsedTime
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
        _isServiceActive.value = false
        serviceScope.cancel()
        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }


    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        const val DEEP_LINK = "running_tracker://active_run"

        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "active_run"
        private const val EXTRA_ACTIVITY_CLASS = "EXTRA_ACTIVITY_CLASS"
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"

        /**
         * Creates an intent to start the service with the specified activity class.
         */
        fun createServiceStartingIntent(context: Context, activityClass: Class<*>): Intent {
            return Intent(context, ActiveRunService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACTIVITY_CLASS, activityClass.name)
            }
        }

        /**
         * Creates an intent to stop the service.
         */
        fun createServiceStoppingIntent(context: Context): Intent {
            return Intent(context, ActiveRunService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

}
