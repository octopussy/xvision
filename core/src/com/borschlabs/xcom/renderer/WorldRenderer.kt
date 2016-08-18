package com.borschlabs.xcom.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Disposable
import com.borschlabs.xcom.world.World

/**
 * @author octopussy
 */

class WorldRenderer(val world: World,
                    val tiledMap: TiledMap,
                    val camera: OrthographicCamera,
                    val debugShapeRenderer: ShapeRenderer) : Disposable {

    private var tiledMapRenderer: OrthogonalTiledMapRenderer

    private var groundLayer: TiledMapTileLayer

    init {
        groundLayer = tiledMap.layers.get("ground") as TiledMapTileLayer
        tiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap)
    }

    fun render(delta: Float) {
        // draw ground
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        world.units.forEach { it.render(tiledMapRenderer.batch, delta) }

        drawDebugGeometry()
    }

    override fun dispose() {
        tiledMapRenderer.dispose()
    }

    private fun drawDebugGeometry() {
       // drawDebugGrid()
        drawDebugBounds()
       // drawObstacles()
    }

    private fun drawDebugBounds() {
        val w = world.width.toFloat()
        val h = world.height.toFloat()

        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Line, Color.RED) {
            line(0.0f, 0.0f, 0.0f, h)
            line(0.0f, h, w, h)
            line(w, h, w, 0.0f)
            line(w, 0.0f, 0.0f, 0.0f)
        }
    }

    private fun drawDebugGrid() {
        val w = world.width.toFloat()
        val h = world.height.toFloat()

        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Line, Color(0.0f, 1.0f, 0.0f, 0.1f)) {
            var i = w.toFloat()
            while (i >= 0.0f) {
                line(i, 0.0f, i, h)
                i -= 1.0f
            }

            i = h
            while (i >= 0.0f) {
                line(0.0f, i, w, i)
                i -= 1.0f
            }
        }
    }

    private fun drawObstacles() {
        world.obstacles.forEach { fillCell(it.x, it.y, Color.RED) }
    }

    private fun fillCell(cellX: Int, cellY:Int, color:Color) {
        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Filled, color) {
            rect(cellX.toFloat(), cellY.toFloat(), 1.0f, 1.0f)
        }
    }
}