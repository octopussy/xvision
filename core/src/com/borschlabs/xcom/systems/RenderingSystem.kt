package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.components.GameUnitComponent
import com.borschlabs.xcom.components.TextureComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.renderer.draw
import com.borschlabs.xcom.world.GameUnitTurnArea

/**
 * @author octopussy
 */

class RenderingSystem(val camera: OrthographicCamera, val tiledMap: TiledMap) : EntitySystem() {

    private val cellSize = (tiledMap.layers[0] as TiledMapTileLayer).tileWidth

    private val TURN_AREA_COLOR = Color(0f, 0f, 1f, 0.2f)

    private val batch: SpriteBatch = SpriteBatch()

    private val debugShapeRenderer: ShapeRenderer = ShapeRenderer()

    private val tiledMapRenderer: OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

    private var objects: ImmutableArray<Entity> = ImmutableArray(Array())
    private var units: ImmutableArray<Entity> = ImmutableArray(Array())

    private val transformM: ComponentMapper<TransformComponent> = ComponentMapper.getFor(TransformComponent::class.java)
    private val textureM: ComponentMapper<TextureComponent> = ComponentMapper.getFor(TextureComponent::class.java)
    private val unitM: ComponentMapper<GameUnitComponent> = ComponentMapper.getFor(GameUnitComponent::class.java)

    override fun addedToEngine(engine: Engine) {
        objects = engine.getEntitiesFor(Family.all(TransformComponent::class.java, TextureComponent::class.java).get())
        units = engine.getEntitiesFor(Family.all(GameUnitComponent::class.java).get())
    }

    override fun removedFromEngine(engine: Engine) {
    }

    override fun update(deltaTime: Float) {
        camera.update()

        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        debugShapeRenderer.projectionMatrix = camera.combined

        enableBlending()
        for (e in units) {
            val unit = unitM.get(e)
            if (unit.isTurnAreaVisible) {
                drawTurnArea(unit.turnArea)
            }
        }

        disableBlending()

        batch.projectionMatrix = camera.combined
        batch.begin()

        for (obj in objects) {
            val transform = transformM.get(obj)
            val texture = textureM.get(obj)
            batch.draw(texture.region, transform.pos.x, transform.pos.y)
        }

        batch.end()
    }

    fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
    }

    private fun drawTurnArea(turnArea: GameUnitTurnArea) {
        turnArea.reachableCells.forEach { fillCell(it.x, it.y, TURN_AREA_COLOR) }
    }

    private fun fillCell(cellX: Int, cellY: Int, color: Color) {
        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Filled, color) {
            rect(cellX.toFloat() * cellSize, cellY.toFloat() * cellSize, cellSize, cellSize)
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