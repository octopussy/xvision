package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx

/**
 * @author octopussy
 */

class Level private constructor(val width: Int, val height: Int, val cellSize: Int) {
    init {

    }

    companion object {
        val TAG = "Level"

        fun create(width: Int, height: Int, tileWidth: Int, tileHeight: Int): Level {
            if (width <= 0 || height <= 0) {
                val msg = "Level measures are negative! (w=$width, h=$height)"
                Gdx.app.error(TAG, msg)
                throw IllegalArgumentException(msg)
            }

            if (tileWidth <= 0 || tileHeight <= 0) {
                val msg = "Level tile measures are negative! (w=$tileWidth, h=$tileHeight)"
                Gdx.app.error(TAG, msg)
                throw IllegalArgumentException(msg)
            }

            if (tileWidth != tileHeight) {
                val msg = "Level tile is not a square! (w=$tileWidth, h=$tileHeight)"
                Gdx.app.error(TAG, msg)
                throw IllegalArgumentException(msg)
            }

            return Level(width, height, tileWidth)
        }
    }
}
