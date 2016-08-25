package com.borschtlabs.gytm.dev

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Disposable

/**
 * @author octopussy
 */

class LevelRenderer(private val level: Level, batch: Batch): Disposable {

    private val renderer: OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(level.tiledMap, batch)

    fun render(cam: OrthographicCamera) {
        renderer.setView(cam)
        renderer.render()
    }

    override fun dispose() {
        renderer.dispose()
    }
}
