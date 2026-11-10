package wseemann.media.romote.model

data class SettingsScreenUiState(
    val notificationStyle: NotificationStyle = NotificationStyle.THUMB_FRIENDLY,
    val hardwareVolumeEnabled: Boolean = true,
    val shakeToPauseEnabled: Boolean = false,
    val notificationWidgetEnabled: Boolean = false,
    val findRemoteSupported: Boolean = false,
    val versionName: String = ""
)
