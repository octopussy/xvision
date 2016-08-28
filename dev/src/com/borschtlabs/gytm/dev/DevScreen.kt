package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.borschtlabs.gytm.dev.core.CameraActor
import com.borschtlabs.gytm.dev.core.systems.CoreSystem
import com.borschtlabs.gytm.dev.game.BaseScreen
import enableBlending

/**
 * @author octopussy
 */

class DevScreen : BaseScreen() {

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

        areas.put(1, TurnArea.create(world.level!!, startX, startY, 1, dist))
        areas.put(2, TurnArea.create(world.level!!, startX, startY, 2, dist))
        areas.put(3, TurnArea.create(world.level!!, startX, startY, 3, dist))
        areas.put(4, TurnArea.create(world.level!!, startX, startY, 4, dist))

        val camActor = world.spawnActor<CameraActor> {
            location.set(20f, 20f, 0f)
            setAsActiveCamera()
        }
    }

    override fun render(delta: Float) {
        super.render(delta)

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

        debugSR.projectionMatrix = engine.getSystem(CoreSystem::class.java).activeCamera.combined

        val level = world.level ?: return

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
        val area = areas[unitSize]

        val shift = (unitSize - 1) * 0.5f
        area?.waypoints?.forEach { drawDebugRect(it.x + shift, it.y + shift, 1f) }
    }
}