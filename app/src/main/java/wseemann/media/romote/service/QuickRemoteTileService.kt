package wseemann.media.romote.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import wseemann.media.romote.R
import wseemann.media.romote.utils.RemoteControlNotification

class QuickRemoteTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val message = if (RemoteControlNotification.show(this)) {
            R.string.quick_remote_notification_shown
        } else {
            R.string.quick_remote_notification_blocked
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
