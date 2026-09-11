package wseemann.media.romote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wseemann.ecp.core.KeyPressKeyValues
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import wseemann.media.romote.device.DeviceManager
import wseemann.media.romote.di.IoDispatcher
import wseemann.media.romote.di.MainDispatcher
import wseemann.media.romote.utils.WakeOnLan
import javax.inject.Inject

@AndroidEntryPoint
class CommandReceiver : BroadcastReceiver() {

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    @Inject
    lateinit var deviceManager: DeviceManager

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        if (intent.action == ACTION_TOGGLE_POWER) {
            togglePower(context)
            return
        }

        val keypressKeyValues = intent.getSerializableExtra("keypress") as? KeyPressKeyValues
        if (keypressKeyValues == null) {
            Timber.w("Received intent without a valid 'keypress' extra, ignoring")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                deviceManager.getConnectedDevice()?.performKeyPress(keypressKeyValues)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun togglePower(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            val device = deviceManager.getConnectedDevice()
            val powerMode = device?.queryDeviceInfo()?.powerMode
            when (powerMode) {
                POWER_ON_MODE -> {
                    device.performKeyPress(KeyPressKeyValues.POWER_OFF)
                    pendingResult.finish()
                }
                null -> WakeOnLan.wakeAsync(
                    context,
                    deviceManager,
                    ioDispatcher,
                    mainDispatcher,
                ) { pendingResult.finish() }
                else -> {
                    device.performKeyPress(KeyPressKeyValues.POWER_ON)
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_POWER = "wseemann.media.romote.action.TOGGLE_POWER"
        private const val POWER_ON_MODE = "PowerOn"
    }
}
