package com.borschtlabs.gytm.dev.core.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.borschtlabs.gytm.dev.EmptyMapRenderer
import com.borschtlabs.gytm.dev.core.*
import com.borschtlabs.gytm.dev.draw
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
            viewport.camera = field
            resize(Gdx.graphics.width, Gdx.graphics.height)
        }

    val viewport: ExtendViewport = ExtendViewport(30.0f, 17.0f)

    private var currentLevelName = ""

    private var levelRenderer: MapRenderer = EmptyMapRenderer()

    private val mainBatch: SpriteBatch = SpriteBatch()

    private var debugShapeRenderer: ShapeRenderer = ShapeRenderer()

    private var visibilityEntities: ImmutableArray<Entity> by Delegates.notNull()

    private var textureEntities: ImmutableArray<Entity> by Delegates.notNull()
    private var textureCompMapper: ComponentMapper<TextureComponent> by Delegates.notNull()

    override fun update(deltaTime: Float) {
        activeCamera.update()

        tickActors(deltaTime)

        drawLevel()

        drawActorsWithTextures()

        drawDebug()
    }

    override fun addedToEngine(engine: Engine) {
        visibilityEntities = engine.getEntitiesFor(Family.all(VisibilityComponent::class.java).get())
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
            viewport.minWorldWidth = w
            viewport.minWorldHeight = w * aspectRatio
        } else {
            viewport.minWorldWidth = w / aspectRatio
            viewport.minWorldHeight = w
        }

        activeCamera.position.set(pos)
        viewport.update(width, height)
    }

    private fun drawLevel() {
        if (currentLevelName != world.levelName) {
            val level = world.level
            levelRenderer = OrthogonalTiledMapRenderer(level.tiledMap, 1f / level.cellSize, mainBatch)
            currentLevelName = world.levelName
        }

        levelRenderer.setView(activeCamera)
        levelRenderer.render()
    }

    private fun drawActorsWithTextures() {

        val level = world.level
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

    private fun drawDebug() {
        if (true) { // debug

            Gdx.gl.glLineWidth(1.5f)

            debugShapeRenderer.projectionMatrix = activeCamera.combined

            debugShapeRenderer.draw(ShapeRenderer.ShapeType.Line, Color.RED) {

                engine.entities.forEach {
                    if (it != null && it is Actor) {
                        circle(it.location.x, it.location.y, it.boundsRadius, 12)
                    }
                }

            }

            enableBlending()
            for (e in visibilityEntities) {
                val vc = e.getComponent(VisibilityComponent::class.java)
                if (vc.isEnabled) {
                    //drawDebugVisMap(vc.points)

                    vc.debugInfo.forEach { info ->
                        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Line, info.color) {
                            info.lines.forEach {
                                line(it.first, it.second)
                            }
                        }
                    }
                }
            }
            disableBlending()
        }
    }

    private fun drawDebugVisMap(points: Array<Point>) {
        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Line, Color.GOLD) {

            for (i in 1..points.size - 1) {
                line(points[i - 1].position, points[i].position)
            }
        }
    }

    private fun enableBlending() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun disableBlending() {
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }
}