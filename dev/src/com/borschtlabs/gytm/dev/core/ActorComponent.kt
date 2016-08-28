package com.borschtlabs.gytm.dev.core

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector3

/**
 * @author octopussy
 */

abstract class ActorComponent(val owner:Actor) : Component {

    val location: Vector3 = Vector3()

    var rotation: Float = 0f

    abstract fun update(dt: Float)
}