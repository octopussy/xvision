package com.borschtlabs.gytm.dev.level

import com.badlogic.gdx.maps.tiled.TmxMapLoader

/**
 * @author octopussy
 */

class LevelLoader(val levelsPath: String) {

    fun load(levelName: String): Level {
        val tiledMap = TmxMapLoader().load("$levelsPath/$levelName${LEVEL_FILE_EXT}")

        return Level.create(tiledMap)
    }

    private companion object {
        val TAG: String = "LevelLoader"
        val LEVEL_FILE_EXT = ".tmx"
    }
}