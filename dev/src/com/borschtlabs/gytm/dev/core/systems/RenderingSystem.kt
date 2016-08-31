package com.borschtlabs.gytm.dev.core.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.borschtlabs.gytm.dev.EmptyMapRenderer
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.TextureComponent
import com.borschtlabs.gytm.dev.core.World
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

class RenderingSystem(val world: World) : EntitySystem(1) {

    private val DEFAULT_CAMERA: OrthographicCamera = OrthographicCamera()
    private val VIEWPORT_WIDTH = 30

    var activeCamera: OrthographicCamera = DEFAULT_CAMERA
        set(value) {
            field = value
            resize(Gdx.graphics.width, Gdx.graphics.height)
        }

    private var currentLevelName = ""

    private var levelRenderer: MapRenderer = EmptyMapRenderer()

    private val mainBatch: SpriteBatch = SpriteBatch()

    private var textureEntities: ImmutableArray<Entity> by Delegates.notNull()
    private var textureCompMapper: ComponentMapper<TextureComponent> by Delegates.notNull()

    override fun update(deltaTime: Float) {
        activeCamera.update()

        tickActors(deltaTime)

        drawLevel()

        drawActorsWithTextures()
    }

    override fun addedToEngine(engine: Engine) {
        textureEntities = engine.getEntitiesFor(Family.all(TextureComponent::class.java).get())
        textureCompMapper = ComponentMapper.getFor(TextureComponent::class.java)
    }

    private fun tickActors(deltaTime: Float) {
        engine.entities.forEach {
            if (it != null && it is Actor) {
                it.tick(deltaTime)
            }
        }
    }

    fun resize(width: Int, height: Int) {
        val aspectRatio = height / width.toFloat()
        val w = VIEWPORT_WIDTH.toFloat()

        val pos = activeCamera.position.cpy()
        if (aspectRatio < 1) {
            activeCamera.setToOrtho(false, w, w * aspectRatio)
        } else {
            activeCamera.setToOrtho(false, w / aspectRatio, w)
        }

        activeCamera.position.set(pos)
        activeCamera.update()
    }

    private fun drawLevel() {
        if (world.level != null && world.level != null && currentLevelName != world.levelName) {
            val level = world.level!!
            levelRenderer = OrthogonalTiledMapRenderer(level.tiledMap, 1f / level.cellSize, mainBatch)
            currentLevelName = world.levelName
        }

        levelRenderer.setView(activeCamera)
        levelRenderer.render()
    }

    private fun drawActorsWithTextures() {

        val level = world.level!!
        val pixelsToMetres: Float = 1f / level.cellSize

        mainBatch.begin()

        textureEntities.forEach {
            val tc = textureCompMapper.get(it)
            tc.region?.let {
                val width = tc.region!!.regionWidth.toFloat()
                val height = tc.region!!.regionHeight.toFloat()
                val originX = width * tc.origin.x
                val originY = height * tc.origin.y

                mainBatch.draw(tc.region,
                        tc.location.x - originX, tc.location.y - originY,
                        originX, originY,
                        width, height,
                        tc.scale.x * pixelsToMetres, tc.scale.y * pixelsToMetres,
                        MathUtils.radiansToDegrees * tc.rotation)
            }
        }

        mainBatch.end()
    }
}