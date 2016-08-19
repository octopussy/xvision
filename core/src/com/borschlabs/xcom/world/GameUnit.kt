package com.borschlabs.xcom.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.borschlabs.xcom.renderer.RenderContext

/**
 * @author octopussy
 */

open class GameUnit(val field: Field, sprite: Sprite) : Sprite(sprite) {
    var position: FieldCell? = null
        set(value) {
            field = value
            val cellX = value?.x ?: 0
            val cellY = value?.y ?: 0
            x = cellX * this.field.cellSize
            y = cellY * this.field.cellSize
        }

    val turnArea = GameUnitTurnArea(field)

    private val turnAreaColor = Color(0.0f, 1.0f, 1.0f, 0.1f)

    fun startNewTurn() {
        calcTurnArea()
    }

    fun calcTurnArea() {
        if (position != null) {
            turnArea.calculateArea(position!!, 5)
        }
    }

    fun render(renderContext: RenderContext, delta: Float) {
        with(renderContext) {
            batch.begin()
            draw(batch)
            batch.end()

            renderContext.blending = true

            // turn area
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.projectionMatrix = renderContext.camera.combined
            shapeRenderer.color = turnAreaColor
            for (c in turnArea.reachableCells) {
                shapeRenderer.rect(c.x * field.cellSize, c.y * field.cellSize, field.cellSize, field.cellSize)
            }
            shapeRenderer.end()
        }
    }
}