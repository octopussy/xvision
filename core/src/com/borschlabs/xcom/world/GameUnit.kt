package com.borschlabs.xcom.world

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite

/**
 * @author octopussy
 */

open class GameUnit(val world: World, sprite: Sprite) : Sprite(sprite) {
    var position:FieldCell? = null

    val turnArea = GameUnitTurnArea(world)

    fun calcTurnArea(world:World) {
        if (position != null) {
            turnArea.calculateArea(position!!, 5)
        }
    }

    fun render(batch: Batch, delta: Float) {
        batch.begin()
        draw(batch)
        batch.end()
    }
}