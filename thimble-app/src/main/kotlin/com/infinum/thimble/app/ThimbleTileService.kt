package com.infinum.thimble.app

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
internal class ThimbleTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_INACTIVE) {
            qsTile.state = Tile.STATE_ACTIVE
//            Thimble.start(this)
        } else {
            qsTile.state = Tile.STATE_INACTIVE
//            Thimble.stop(this)
        }
        qsTile.updateTile()
    }
}
