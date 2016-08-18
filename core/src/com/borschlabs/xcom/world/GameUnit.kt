package com.borschlabs.xcom.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.borschlabs.xcom.renderer.RenderContext

/**
 * @author octopussy
 */

open class GameUnit(val world: World, sprite: Sprite) : Sprite(sprite) {
    var position: FieldCell? = null
        set(value) {
            field = value
            val cellX = value?.x ?: 0
            val cellY = value?.y ?: 0
            x = cellX * world.cellSize
            y = cellY * world.cellSize
        }

    val turnArea = GameUnitTurnArea(world)

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
                shapeRenderer.rect(c.x * world.cellSize, c.y * world.cellSize, world.cellSize, world.cellSize)
            }
            shapeRenderer.end()
        }
    }
}