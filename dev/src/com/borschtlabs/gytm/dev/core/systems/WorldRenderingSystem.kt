package com.borschtlabs.gytm.dev.core.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.borschtlabs.gytm.dev.EmptyMapRenderer
import com.borschtlabs.gytm.dev.core.World

/**
 * @author octopussy
 */

class WorldRenderingSystem(val world: World) : EntitySystem(1) {

    private var currentLevelName = ""

    private var levelRenderer: MapRenderer = EmptyMapRenderer()

    private val mainBatch: SpriteBatch = SpriteBatch()

    private val coreSystem: CoreSystem get() = engine.getSystem(CoreSystem::class.java)

    override fun update(deltaTime: Float) {

        drawLevel()
    }

    private fun drawLevel() {
        if (world.level != null && world.level != null && currentLevelName != world.levelName) {
            val level = world.level!!
            levelRenderer = OrthogonalTiledMapRenderer(level.tiledMap, 1f / level.cellSize, mainBatch)
            currentLevelName = world.levelName
        }

        levelRenderer.setView(coreSystem.activeCamera)
        levelRenderer.render()
    }
}