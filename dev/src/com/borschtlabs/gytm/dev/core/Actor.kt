package com.borschtlabs.gytm.dev.core

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

abstract class Actor(val world: World) : Entity() {

    val location: Vector2 get() = rootComponent.location

    var boundsRadius: Float
        get() = rootComponent.boundsRadius
        set(value) {
            rootComponent.boundsRadius = value
        }

    var rootComponent: ActorComponent by Delegates.notNull()

    inline fun <reified T : ActorComponent> createComponent(initializer: T.() -> Unit): T {
        val constr = T::class.java.getConstructor(Actor::class.java)
        val comp = constr.newInstance(this)
        add(comp)
        initializer(comp)
        return comp
    }

    open fun tick(dt: Float) {
        components.forEach {
            if (it != null && it is ActorComponent) {
                it.updateTransformations(rootComponent)
                it.update(dt)
            }
        }
    }

    open fun render(dt: Float) {

    }

    fun hit(worldX: Float, worldY: Float): Boolean = rootComponent.hit(worldX, worldY)
}