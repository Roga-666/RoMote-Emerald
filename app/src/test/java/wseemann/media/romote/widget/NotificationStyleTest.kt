package wseemann.media.romote.widget

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import com.wseemann.ecp.core.KeyPressKeyValues
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import wseemann.media.romote.R
import wseemann.media.romote.model.NotificationStyle
import wseemann.media.romote.preferences.AppPreferences
import wseemann.media.romote.receiver.CommandReceiver
import wseemann.media.romote.utils.RemoteControlNotification

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class NotificationStyleTest {
    private lateinit var context: Application
    private lateinit var preferences: AppPreferences

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        preferences = AppPreferences(PreferenceManager.getDefaultSharedPreferences(context))
        context.getSystemService(NotificationManager::class.java).cancelAll()
    }

    @Test
    fun stylePersistsAndUnknownValuesFallBackToClassic() {
        assertEquals(NotificationStyle.THUMB_FRIENDLY, preferences.getNotificationStyle())
        preferences.setNotificationStyle(NotificationStyle.CLASSIC)
        assertEquals(NotificationStyle.CLASSIC, AppPreferences(PreferenceManager.getDefaultSharedPreferences(context)).getNotificationStyle())
        preferences.setNotificationStyle(NotificationStyle.THUMB_FRIENDLY)
        assertEquals(NotificationStyle.THUMB_FRIENDLY, preferences.getNotificationStyle())
        assertEquals(NotificationStyle.CLASSIC, NotificationStyle.fromPreference("future-or-corrupt-value"))
    }

    @Test
    fun selectionRefreshesVisibleNotificationAndDoesNotResurrectDismissedOne() {
        val manager = context.getSystemService(NotificationManager::class.java)
        RemoteControlNotification.refreshIfVisible(context)
        assertTrue(manager.activeNotifications.isEmpty())
        assertTrue(RemoteControlNotification.show(context))
        val id = manager.activeNotifications.single().id
        assertEquals(R.layout.notification_remote_thumb, manager.activeNotifications.single().notification.bigContentView.layoutId)
        preferences.setNotificationStyle(NotificationStyle.CLASSIC)
        RemoteControlNotification.refreshIfVisible(context)
        assertEquals(id, manager.activeNotifications.single().id)
        assertEquals(R.layout.notification_remote_expanded, manager.activeNotifications.single().notification.bigContentView.layoutId)
        preferences.setNotificationStyle(NotificationStyle.THUMB_FRIENDLY)
        RemoteControlNotification.refreshIfVisible(context)
        assertEquals(R.layout.notification_remote_thumb, manager.activeNotifications.single().notification.bigContentView.layoutId)
        manager.cancelAll()
        RemoteControlNotification.refreshIfVisible(context)
        assertTrue(manager.activeNotifications.isEmpty())
    }

    @Test
    fun bothStylesSendTheSameCommandsWithoutWidgetCollisions() {
        val commands = mapOf(
            R.id.notification_up to KeyPressKeyValues.UP,
            R.id.notification_left to KeyPressKeyValues.LEFT,
            R.id.notification_ok to KeyPressKeyValues.SELECT,
            R.id.notification_right to KeyPressKeyValues.RIGHT,
            R.id.notification_down to KeyPressKeyValues.DOWN,
            R.id.notification_home to KeyPressKeyValues.HOME,
            R.id.notification_mute to KeyPressKeyValues.VOLUME_MUTE,
            R.id.notification_volume_up to KeyPressKeyValues.VOLUME_UP,
            R.id.notification_volume_down to KeyPressKeyValues.VOLUME_DOWN,
        )
        // Build a widget too: its PendingIntents must not overwrite notification actions.
        RemoteControlNotification.createSlimRemoteViews(context).apply(context, FrameLayout(context))
        for (style in NotificationStyle.entries) {
            val root = RemoteControlNotification.createRemoteViews(context, RemoteControlNotification.layoutFor(style)).apply(context, FrameLayout(context))
            for ((id, command) in commands) {
                val view = root.findViewById<View>(id)
                assertTrue(view.performClick())
                val intent = shadowOf(context).broadcastIntents.last()
                assertEquals(command, intent.getSerializableExtra("keypress"))
                assertEquals("romote://remote/key/${command.name}", intent.dataString)
                assertEquals(CommandReceiver::class.java.name, intent.component?.className)
            }
            assertTrue(root.findViewById<View>(R.id.notification_power).performClick())
            assertEquals(CommandReceiver.ACTION_TOGGLE_POWER, shadowOf(context).broadcastIntents.last().action)
        }
    }

    @Test
    fun previewsHaveNoCommandListeners() {
        val before = shadowOf(context).broadcastIntents.size
        for (style in NotificationStyle.entries) {
            val root = RemoteControlNotification.createPreviewViews(context, style).apply(context, FrameLayout(context))
            fun check(view: View) {
                assertFalse(view.hasOnClickListeners())
                view.performClick()
                if (view is ViewGroup) for (i in 0 until view.childCount) check(view.getChildAt(i))
            }
            check(root)
        }
        assertEquals(before, shadowOf(context).broadcastIntents.size)
    }
}
