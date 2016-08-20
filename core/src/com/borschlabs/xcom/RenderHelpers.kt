package com.borschlabs.xcom

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * @author octopussy
 */

fun ShapeRenderer.draw(shapeType: ShapeRenderer.ShapeType, color: Color, block: ShapeRenderer.() -> Unit) {
    begin(shapeType)
    this.color = color
    block()
    end()
}
