package wseemann.media.romote.utils

import android.view.KeyEvent
import com.wseemann.ecp.core.KeyPressKeyValues

/** Tracks complete key gestures, including held-key repeats and their release. */
class HardwareVolumeRouter {
    private val handledKeys = mutableSetOf<Int>()

    data class Result(val consumed: Boolean, val command: KeyPressKeyValues? = null)

    fun route(keyCode: Int, action: Int, eligible: Boolean, canceled: Boolean): Result {
        val key = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> KeyPressKeyValues.VOLUME_UP
            KeyEvent.KEYCODE_VOLUME_DOWN -> KeyPressKeyValues.VOLUME_DOWN
            else -> return Result(false)
        }
        if (action == KeyEvent.ACTION_UP && handledKeys.remove(keyCode)) return Result(true)
        if (action == KeyEvent.ACTION_DOWN && eligible && !canceled) {
            handledKeys.add(keyCode)
            return Result(true, key)
        }
        return Result(false)
    }
}
