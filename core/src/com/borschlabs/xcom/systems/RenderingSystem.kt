package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
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
import com.borschlabs.xcom.components.RouteComponent
import com.borschlabs.xcom.components.TextureComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.draw
import com.borschlabs.xcom.world.FieldCell
import com.borschlabs.xcom.world.GameUnitTurnArea

/**
 * @author octopussy
 */

class RenderingSystem(val camera: OrthographicCamera, val tiledMap: TiledMap) : EntitySystem() {

    private val cellSize = (tiledMap.layers[0] as TiledMapTileLayer).tileWidth

    private val TURN_AREA_COLOR = Color(0f, 0f, 1f, 0.2f)
    private val ROUTE_COLOR = Color(0f, 1f, 1f, 0.6f)

    private val batch: SpriteBatch = SpriteBatch()

    private val debugShapeRenderer: ShapeRenderer = ShapeRenderer()

    private val tiledMapRenderer: OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

    private var visibleObjects: ImmutableArray<Entity> = ImmutableArray(Array())
    private var units: ImmutableArray<Entity> = ImmutableArray(Array())
    private var routes: ImmutableArray<Entity> = ImmutableArray(Array())

    override fun addedToEngine(engine: Engine) {
        visibleObjects = engine.getEntitiesFor(Family.all(TextureComponent::class.java, TransformComponent::class.java).get())
        units = engine.getEntitiesFor(Family.all(GameUnitComponent::class.java).get())
        routes = engine.getEntitiesFor(Family.all(RouteComponent::class.java).get())
    }

    override fun removedFromEngine(engine: Engine) {
        batch.dispose()
        debugShapeRenderer.dispose()
        tiledMapRenderer.dispose()
    }

    override fun update(deltaTime: Float) {
        camera.update()

        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        debugShapeRenderer.projectionMatrix = camera.combined

        drawTurnAreas()

        batch.projectionMatrix = camera.combined
        batch.begin()

        drawVisibleObjects()

        batch.end()

        drawRoutes()
    }

    private fun drawVisibleObjects() {
        disableBlending()

        for (obj in visibleObjects) {
            val transform = Mappers.TRANSFORM.get(obj)
            val texture = Mappers.TEXTURE.get(obj)
            batch.draw(texture.region, transform.pos.x, transform.pos.y)
        }
    }

    private fun drawTurnAreas() {
        enableBlending()
        for (e in units) {
            val unit = Mappers.GAME_UNIT.get(e)
            if (unit.state == GameUnitComponent.Companion.State.IDLE) {
                drawTurnArea(unit.turnArea)
            }
        }
    }

    private fun drawRoutes() {
        enableBlending()
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        debugShapeRenderer.color = ROUTE_COLOR
        for (r in routes) {
            val routeComp = Mappers.ROUTE.get(r)
            val cells = routeComp.route.cells
            if (cells.size > 1) {
                var from = cells[0]
                var i = 1
                while (i < cells.size) {
                    drawRouteSeg(from, cells[i])
                    from = cells[i]
                    ++i
                }
            }

        }

        debugShapeRenderer.end()
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

    private fun drawRouteSeg(from: FieldCell, to: FieldCell) {
        debugShapeRenderer.line(
                from.x * cellSize + cellSize / 2.0f,
                from.y * cellSize + cellSize / 2.0f,
                to.x * cellSize + cellSize / 2.0f,
                to.y * cellSize + cellSize / 2.0f)
    }

    private fun enableBlending() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun disableBlending() {
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }
}