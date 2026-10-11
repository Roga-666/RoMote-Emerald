package wseemann.media.romote.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import wseemann.media.romote.utils.RemoteControlNotification

/** A compact row of direct Roku controls for home-screen and LockStar hosts. */
class EmeraldSlimWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, RemoteControlNotification.createSlimRemoteViews(context))
    }
}
