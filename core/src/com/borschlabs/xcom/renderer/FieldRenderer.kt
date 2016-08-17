package com.borschlabs.xcom.renderer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.UnitTurnArea

/**
 * @author octopussy
 */

class FieldRenderer(val field: Field, val debugShapeRenderer: ShapeRenderer) {

    fun render(delta: Float) {
        drawDebugGeometry()
    }

    private fun drawDebugGeometry() {
        drawDebugGrid()
        drawDebugBounds()

        drawTestReachableCells(7, 7, 4)
    }

    private fun drawDebugBounds() {
        val w = field.width.toFloat()
        val h = field.height.toFloat()

        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Line, Color.RED) {
            line(0.0f, 0.0f, 0.0f, h)
            line(0.0f, h, w, h)
            line(w, h, w, 0.0f)
            line(w, 0.0f, 0.0f, 0.0f)
        }
    }

    private fun drawDebugGrid() {
        val w = field.width.toFloat()
        val h = field.height.toFloat()

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

    private fun drawTestReachableCells(startX: Int, startY: Int, maxDistance:Int) {
        val turnArea = UnitTurnArea(field)
        turnArea.calculateArea(startX, startY, maxDistance)

        for ((x, y) in turnArea.reachableCells) {
            fillCell(x, y, Color(0.0f, 1.0f, 0.0f, 0.5f))
        }
    }

    private fun fillCell(cellX: Int, cellY:Int, color:Color) {
        debugShapeRenderer.draw(ShapeRenderer.ShapeType.Filled, color) {
            rect(cellX.toFloat(), cellY.toFloat(), 1.0f, 1.0f)
        }
    }
}