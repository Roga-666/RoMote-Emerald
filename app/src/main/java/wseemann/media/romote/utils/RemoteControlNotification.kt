package wseemann.media.romote.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.RemoteViews
import androidx.core.net.toUri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.wseemann.ecp.core.KeyPressKeyValues
import wseemann.media.romote.R
import wseemann.media.romote.activity.MainActivity
import wseemann.media.romote.receiver.CommandReceiver
import wseemann.media.romote.model.NotificationStyle
import wseemann.media.romote.preferences.AppPreferences

object RemoteControlNotification {

    // V2 intentionally uses a fresh channel: Android does not let an app raise the importance of
    // the original LOW channel after it has been created on the device.
    private const val CHANNEL_ID = "romote_emerald_remote_v2"
    private const val NOTIFICATION_ID = 101

    fun layoutFor(style: NotificationStyle): Int = when (style) {
        NotificationStyle.CLASSIC -> R.layout.notification_remote_expanded
        NotificationStyle.THUMB_FRIENDLY -> R.layout.notification_remote_thumb
    }

    /** Preview the same XML without attaching any Roku command intents. */
    fun createPreviewViews(context: Context, style: NotificationStyle): RemoteViews =
        RemoteViews(context.packageName, layoutFor(style))

    /** A style change only replaces an existing remote; it does not summon a dismissed one. */
    fun refreshIfVisible(context: Context) {
        if (context.getSystemService(NotificationManager::class.java).activeNotifications.any { it.id == NOTIFICATION_ID }) {
            show(context)
        }
    }

    fun show(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = context.getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled()) return false
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.quick_remote_title),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.quick_remote_title)
                setSound(null, null)
                enableVibration(false)
            },
        )

        val preferences = AppPreferences(PreferenceManager.getDefaultSharedPreferences(context))
        val expanded = createRemoteViews(context, layoutFor(preferences.getNotificationStyle()))

        val openApp = PendingIntent.getActivity(
            context,
            10,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_quick_remote_tile)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.quick_remote_notification_hint))
            .setContentIntent(openApp)
            .setCustomBigContentView(expanded)
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
        return manager.activeNotifications.any { it.id == NOTIFICATION_ID }
    }

    fun createRemoteViews(context: Context, layoutId: Int): RemoteViews {
        val expanded = RemoteViews(context.packageName, layoutId)
        link(expanded, context, R.id.notification_up, KeyPressKeyValues.UP, 3)
        link(expanded, context, R.id.notification_left, KeyPressKeyValues.LEFT, 4)
        link(expanded, context, R.id.notification_ok, KeyPressKeyValues.SELECT, 5)
        link(expanded, context, R.id.notification_right, KeyPressKeyValues.RIGHT, 6)
        link(expanded, context, R.id.notification_down, KeyPressKeyValues.DOWN, 7)
        link(expanded, context, R.id.notification_mute, KeyPressKeyValues.VOLUME_MUTE, 8)
        linkPower(expanded, context, R.id.notification_power, 9)
        link(expanded, context, R.id.notification_home, KeyPressKeyValues.HOME, 11)

        link(expanded, context, R.id.notification_volume_up, KeyPressKeyValues.VOLUME_UP, 12)
        link(expanded, context, R.id.notification_volume_down, KeyPressKeyValues.VOLUME_DOWN, 13)
        return expanded
    }

    fun createSlimRemoteViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.emerald_slim_widget)
        linkPower(views, context, R.id.slim_power, 209)
        link(views, context, R.id.slim_play_pause, KeyPressKeyValues.PLAY, 210)
        link(views, context, R.id.slim_mute, KeyPressKeyValues.VOLUME_MUTE, 208)
        return views
    }

    private fun link(
        views: RemoteViews,
        context: Context,
        viewId: Int,
        key: KeyPressKeyValues,
        requestCode: Int,
    ) {
        val intent = Intent(context, CommandReceiver::class.java)
            .setData("romote://remote/key/${key.name}".toUri())
            .putExtra("keypress", key)
        views.setOnClickPendingIntent(viewId, broadcast(context, intent, requestCode))
    }

    private fun linkPower(views: RemoteViews, context: Context, viewId: Int, requestCode: Int) {
        val intent = Intent(context, CommandReceiver::class.java).setAction(CommandReceiver.ACTION_TOGGLE_POWER)
            .setData("romote://remote/power".toUri())
        views.setOnClickPendingIntent(viewId, broadcast(context, intent, requestCode))
    }

    private fun broadcast(context: Context, intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
