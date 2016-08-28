package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.borschtlabs.gytm.dev.core.BaseScreen
import enableBlending

/**
 * @author octopussy
 */

class DevScreen : BaseScreen() {

    private lateinit var level: Level

    private lateinit var levelRenderer: LevelRenderer

    val dist = 50

    val startX = 2
    val startY = 6
    val unitSize = 2

    var endX = 8
    var endY = 2
    val areas = mutableMapOf<Int, TurnArea>()

    var path: List<TurnArea.WayPoint>? = null
    var smoothedPath: List<TurnArea.WayPoint>? = null


    override fun show() {
        super.show()

        Gdx.gl.glLineWidth(5f)

        val params = TmxMapLoader.Parameters()
        params.generateMipMaps = true
        params.textureMagFilter = Texture.TextureFilter.MipMapNearestNearest
        params.textureMinFilter = Texture.TextureFilter.MipMapNearestNearest

        level = LevelLoader("maps").load("test")

        levelRenderer = LevelRenderer(level, batch)

        debugSR = ShapeRenderer()

        areas.put(1, TurnArea.create(level, startX, startY, 1, dist))
        areas.put(2, TurnArea.create(level, startX, startY, 2, dist))
        areas.put(3, TurnArea.create(level, startX, startY, 3, dist))
        areas.put(4, TurnArea.create(level, startX, startY, 4, dist))
    }

    override fun dispose() {
        super.dispose()

        levelRenderer.dispose()
        level.dispose()
    }

    override fun render(delta: Float) {
        super.render(delta)

        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        cam.update()
        levelRenderer.render(cam)

        drawDebugLevelGfx()

        drawDebugUI()
    }

    override fun onTap (px: Float, py: Float) {
        when {
            unitSize % 2 != 0 -> {
                endX = Math.floor(px.toDouble() - unitSize / 2).toInt()
                endY = Math.floor(py.toDouble() - unitSize / 2).toInt()
            }

            else -> {
                endX = Math.round(px.toDouble() - unitSize / 2).toInt()
                endY = Math.round(py.toDouble() - unitSize / 2).toInt()
            }
        }

        path = areas[unitSize]!!.getPath(startX, startY, endX, endY, unitSize / 2.5f, false)
        smoothedPath = areas[unitSize]!!.getPath(startX, startY, endX, endY, unitSize / 2.5f, true)
    }

    private fun drawDebugLevelGfx() {
        enableBlending()

        debugSR.projectionMatrix = cam.combined

        debugSR.draw(ShapeRenderer.ShapeType.Filled, Color(1f, 0f, 0f, 0.5f)) {
            for (y in 0..level.height - 1) {
                for (x in 0..level.width - 1) {
                    val cell = level.getCell(x, y)
                    cell?.apply {
                        if (isWall) {
                            // drawDebugRect(x, y)
                        }
                    }
                }
            }
        }

        debugSR.draw(ShapeRenderer.ShapeType.Line, Color.MAROON) {
            drawDebugRect(endX.toFloat(), endY.toFloat(), unitSize.toFloat())
        }

        debugSR.draw(ShapeRenderer.ShapeType.Filled, Color(0f, 1f, 0f, 0.3f)) {
            drawDebugTurnArea(startX, startY, unitSize, dist)
        }

        /*debugSR.draw(ShapeRenderer.ShapeType.Line, Color(1f, 0f, 0f, 1f)) {
            path?.let { drawDebugPath(it) }
        }*/

        debugSR.draw(ShapeRenderer.ShapeType.Line, Color(0f, 0f, 1f, 1f)) {
            smoothedPath?.let { drawDebugPath(it) }
        }


        /*debugSR.begin(ShapeRenderer.ShapeType.Line)
        debugSR.color = Color.BROWN
        debugSR.line(2.7891815f, 6.3675447f, 5.7891817f, 5.3675447f)

        debugSR.color = Color.YELLOW
        debugSR.line(2.715395f,6.146185f, 5.715395f,5.146185f)

        debugSR.end()*/

    }

    private fun drawDebugPath(path: List<TurnArea.WayPoint>) {
        for (i in 0..path.size - 2) {
            debugSR.line(path[i].center, path[i + 1].center)
        }

        path.forEach {
            debugSR.circle(it.center.x, it.center.y, 0.1f, 10)
        }
    }

    private fun drawDebugTurnArea(x: Int, y: Int, unitSize: Int, maxDistance: Int) {
        val area = areas.getOrPut(unitSize) {
            TurnArea.create(level, x, y, unitSize, maxDistance)
        }

        val shift = (unitSize - 1) * 0.5f
        area.waypoints.forEach { drawDebugRect(it.x + shift, it.y + shift, 1f) }
    }
}