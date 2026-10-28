package wseemann.media.romote.utils

import android.view.KeyEvent
import com.wseemann.ecp.core.KeyPressKeyValues
import org.junit.Assert.*
import org.junit.Test

class HardwareVolumeRouterTest {
    @Test fun `held volume keys send repeats and never send on release`() {
        val router = HardwareVolumeRouter()
        for ((code, command) in listOf(
            KeyEvent.KEYCODE_VOLUME_UP to KeyPressKeyValues.VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN to KeyPressKeyValues.VOLUME_DOWN,
        )) {
            repeat(4) {
                val result = router.route(code, KeyEvent.ACTION_DOWN, true, false)
                assertTrue(result.consumed)
                assertEquals(command, result.command)
            }
            val release = router.route(code, KeyEvent.ACTION_UP, true, false)
            assertTrue(release.consumed)
            assertNull(release.command)
        }
    }

    @Test fun `disabled unfocused background or disconnected input passes through`() {
        val router = HardwareVolumeRouter()
        assertFalse(router.route(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_DOWN, false, false).consumed)
        assertFalse(router.route(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_UP, false, false).consumed)
        assertFalse(router.route(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_DOWN, true, false).consumed)
        assertFalse(router.route(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.ACTION_DOWN, true, true).consumed)
    }

    @Test fun `release stays consumed if focus or setting changes mid gesture`() {
        val router = HardwareVolumeRouter()
        router.route(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_DOWN, true, false)
        val release = router.route(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_UP, false, true)
        assertTrue(release.consumed)
        assertNull(release.command)
        assertFalse(router.route(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.ACTION_UP, false, false).consumed)
    }
}
