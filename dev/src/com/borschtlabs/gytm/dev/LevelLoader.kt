package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TmxMapLoader

/**
 * @author octopussy
 */

class LevelLoader(val levelsPath: String) {

    fun load(levelName: String): Level {
        val tiledMap = TmxMapLoader().load("$levelsPath/$levelName$LEVEL_FILE_EXT")

        val width = tiledMap.properties.get("width") as Int
        val height = tiledMap.properties.get("height") as Int
        val tileWidth = tiledMap.properties.get("tilewidth") as Int
        val tileHeight = tiledMap.properties.get("tileheight")  as Int

        val level = Level.createBlank(width, height, tileWidth, tileHeight, tiledMap)

        val groundLayer = tiledMap.layers.get("ground")

        if (groundLayer == null) {
            val msg = "Level has no 'ground' layer."
            Gdx.app.error(TAG, msg)
            throw IllegalArgumentException(msg)
        }

        return level
    }

    private companion object {
        val TAG: String = "LevelLoader"
        val LEVEL_FILE_EXT = ".tmx"
    }
}