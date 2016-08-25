package com.borschtlabs.gytm.dev

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

        return Level.create(width, height, tileWidth, tileHeight)
    }

    private companion object {
        val LEVEL_FILE_EXT = ".tmx"
    }
}