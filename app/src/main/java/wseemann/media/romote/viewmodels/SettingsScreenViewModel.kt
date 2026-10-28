package wseemann.media.romote.viewmodels

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import wseemann.media.romote.utils.RemoteControlNotification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wseemann.ecp.core.KeyPressKeyValues
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import wseemann.media.romote.BuildConfig
import wseemann.media.romote.device.DeviceManager
import wseemann.media.romote.di.IoDispatcher
import wseemann.media.romote.event.SettingsScreenUiEvent
import wseemann.media.romote.model.SettingsScreenUiState
import wseemann.media.romote.preferences.AppPreferences
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val deviceManager: DeviceManager,
    private val appPreferences: AppPreferences,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsScreenUiState(
            notificationStyle = appPreferences.getNotificationStyle(),
            hardwareVolumeEnabled = appPreferences.isHardwareVolumeEnabled(),
            shakeToPauseEnabled = appPreferences.isShakeToPauseEnabled(),
            notificationWidgetEnabled = appPreferences.isNotificationWidgetEnabled(),
            versionName = BuildConfig.VERSION_NAME
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val supported = deviceSupportsFindRemote()
            _uiState.update { it.copy(findRemoteSupported = supported) }
        }
    }

    fun onHandleEvent(event: SettingsScreenUiEvent) {
        when (event) {
            is SettingsScreenUiEvent.NotificationStyleSelectedEvent -> {
                appPreferences.setNotificationStyle(event.style)
                _uiState.update { it.copy(notificationStyle = event.style) }
                RemoteControlNotification.refreshIfVisible(context)
            }
            is SettingsScreenUiEvent.HardwareVolumeToggledEvent -> {
                appPreferences.setHardwareVolumeEnabled(event.enabled)
                _uiState.update { it.copy(hardwareVolumeEnabled = event.enabled) }
            }
            is SettingsScreenUiEvent.FindRemoteClickedEvent -> onFindRemoteClicked()
            is SettingsScreenUiEvent.ShakeToPauseToggledEvent -> {
                _uiState.update { it.copy(shakeToPauseEnabled = event.enabled) }
                appPreferences.setShakeToPauseEnabled(event.enabled)
            }
            is SettingsScreenUiEvent.NotificationWidgetToggledEvent -> {
                _uiState.update { it.copy(notificationWidgetEnabled = event.enabled) }
                appPreferences.setNotificationWidgetEnabled(event.enabled)
            }
        }
    }

    private fun onFindRemoteClicked() {
        viewModelScope.launch(ioDispatcher) {
            deviceManager.getConnectedDevice()?.performKeyPress(KeyPressKeyValues.FIND_REMOTE)
        }
    }

    private fun deviceSupportsFindRemote(): Boolean {
        try {
            // Throws when no device is paired.
            val device = deviceManager.getConnectedDevice()

            device?.getDeviceInfo()?.supportsFindRemote?.let { return it.toBoolean() }
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to read the connected device")
        }

        return false
    }
}
