package wseemann.media.romote.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import wseemann.media.romote.model.NotificationStyle

class AppPreferences(private val sharedPreferences: SharedPreferences) {

    fun getNotificationStyle(): NotificationStyle = NotificationStyle.fromPreference(
        sharedPreferences.getString(APP_PREFERENCE_NOTIFICATION_STYLE, NotificationStyle.THUMB_FRIENDLY.preferenceValue),
    )

    fun setNotificationStyle(style: NotificationStyle) {
        sharedPreferences.edit { putString(APP_PREFERENCE_NOTIFICATION_STYLE, style.preferenceValue) }
    }

    fun isNotificationWidgetEnabled(): Boolean {
        return false // sharedPreferences.getBoolean(APP_PREFERENCE_NOTIFICATION_WIDGET, false)
    }

    fun setNotificationWidgetEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(APP_PREFERENCE_NOTIFICATION_WIDGET, enabled)
        }
    }

    fun isHardwareVolumeEnabled(): Boolean =
        sharedPreferences.getBoolean(APP_PREFERENCE_HARDWARE_VOLUME, true)

    fun setHardwareVolumeEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(APP_PREFERENCE_HARDWARE_VOLUME, enabled) }
    }

    fun isShakeToPauseEnabled(): Boolean {
        return sharedPreferences.getBoolean(APP_PREFERENCE_SHAKE_TO_PAUSE, false)
    }

    fun setShakeToPauseEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(APP_PREFERENCE_SHAKE_TO_PAUSE, enabled)
        }
    }

    fun getFirstLaunchMillis(): Long {
        return sharedPreferences.getLong(APP_PREFERENCE_REVIEW_FIRST_LAUNCH, 0L)
    }

    fun setFirstLaunchMillis(millis: Long) {
        sharedPreferences.edit {
            putLong(APP_PREFERENCE_REVIEW_FIRST_LAUNCH, millis)
        }
    }

    fun getEngagedSessionCount(): Int {
        return sharedPreferences.getInt(APP_PREFERENCE_REVIEW_ENGAGED_SESSIONS, 0)
    }

    fun setEngagedSessionCount(count: Int) {
        sharedPreferences.edit {
            putInt(APP_PREFERENCE_REVIEW_ENGAGED_SESSIONS, count)
        }
    }

    fun hasSeenRemoteAccessHelp(): Boolean {
        return sharedPreferences.getBoolean(APP_PREFERENCE_REMOTE_ACCESS_HELP_SEEN, false)
    }

    fun setRemoteAccessHelpSeen() {
        sharedPreferences.edit {
            putBoolean(APP_PREFERENCE_REMOTE_ACCESS_HELP_SEEN, true)
        }
    }

    fun getLastReviewPromptMillis(): Long {
        return sharedPreferences.getLong(APP_PREFERENCE_REVIEW_LAST_PROMPT, 0L)
    }

    fun setLastReviewPromptMillis(millis: Long) {
        sharedPreferences.edit {
            putLong(APP_PREFERENCE_REVIEW_LAST_PROMPT, millis)
        }
    }

    companion object {
        private const val APP_PREFERENCE_NOTIFICATION_STYLE = "emerald_notification_style"
        private const val APP_PREFERENCE_HARDWARE_VOLUME = "hardware_volume_enabled"
        const val APP_PREFERENCE_NOTIFICATION_WIDGET = "notification_checkbox_preference"
        private const val APP_PREFERENCE_SHAKE_TO_PAUSE = "shake_to_pause_checkbox_preference"
        private const val APP_PREFERENCE_REVIEW_FIRST_LAUNCH = "review_first_launch_millis"
        private const val APP_PREFERENCE_REVIEW_ENGAGED_SESSIONS = "review_engaged_session_count"
        private const val APP_PREFERENCE_REVIEW_LAST_PROMPT = "review_last_prompt_millis"
        private const val APP_PREFERENCE_REMOTE_ACCESS_HELP_SEEN = "remote_access_help_seen"
    }
}
