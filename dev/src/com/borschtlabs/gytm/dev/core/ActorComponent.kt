package com.borschtlabs.gytm.dev.core

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

/**
 * @author octopussy
 */

abstract class ActorComponent(val owner: Actor) : Component {

    private val children: MutableList<ActorComponent> = mutableListOf()

    val location: Vector3 = Vector3()

    var scale: Vector2 = Vector2(1.0f, 1.0f)

    var rotation: Float = 0f

    var boundsRadius = 0.0f

    private val bounds: Circle = Circle()

    fun updateTransformations(parent: ActorComponent) {
        bounds.radius = boundsRadius
        bounds.setPosition(parent.location.x, parent.location.y)
    }

    open fun update(dt: Float) {
    }

    fun hit(worldX: Float, worldY: Float) = bounds.contains(worldX, worldY)
}