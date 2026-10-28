package wseemann.media.romote.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.os.Bundle
import android.content.Context
import android.content.Intent
import wseemann.media.romote.R
import wseemann.media.romote.activity.MainActivity
import wseemann.media.romote.utils.RemoteControlNotification

/** Commands are explicit receiver intents, so taps work without opening an activity. */
class EmeraldRemoteWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        onUpdate(context, manager, intArrayOf(id))
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = RemoteControlNotification.createRemoteViews(context, R.layout.emerald_remote_widget)
        views.setOnClickPendingIntent(
            R.id.emerald_widget_title,
            PendingIntent.getActivity(
                context, 200, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        manager.updateAppWidget(ids, views)
    }
}
