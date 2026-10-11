package wseemann.media.romote.model

data class SettingsScreenUiState(
    val hardwareVolumeEnabled: Boolean = true,
    val shakeToPauseEnabled: Boolean = false,
    val notificationWidgetEnabled: Boolean = false,
    val findRemoteSupported: Boolean = false,
    val versionName: String = ""
)
