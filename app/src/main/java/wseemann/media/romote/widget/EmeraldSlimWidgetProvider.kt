package wseemann.media.romote.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.os.Bundle
import android.content.Context
import wseemann.media.romote.utils.RemoteControlNotification

/** A compact row of direct Roku controls for home-screen and LockStar hosts. */
class EmeraldSlimWidgetProvider : AppWidgetProvider() {
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        onUpdate(context, manager, intArrayOf(id))
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, RemoteControlNotification.createSlimRemoteViews(context))
    }
}
