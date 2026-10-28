package wseemann.media.romote.event

import wseemann.media.romote.model.NotificationStyle

sealed interface SettingsScreenUiEvent {
    data class NotificationStyleSelectedEvent(val style: NotificationStyle) : SettingsScreenUiEvent
    data class HardwareVolumeToggledEvent(val enabled: Boolean) : SettingsScreenUiEvent

    data object FindRemoteClickedEvent : SettingsScreenUiEvent

    data class ShakeToPauseToggledEvent(val enabled: Boolean) : SettingsScreenUiEvent

    data class NotificationWidgetToggledEvent(val enabled: Boolean) : SettingsScreenUiEvent
}
