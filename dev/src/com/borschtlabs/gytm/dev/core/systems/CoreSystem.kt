package com.borschtlabs.gytm.dev.core.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.CameraComponent
import com.borschtlabs.gytm.dev.core.World
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

class CoreSystem(val world: World) : EntitySystem(0) {

    private val VIEWPORT_WIDTH = 30

    var activeCamera: OrthographicCamera = DEFAULT_CAMERA

    private var actors: ImmutableArray<Entity> by Delegates.notNull()

    override fun addedToEngine(engine: Engine) {
        actors = engine.getEntitiesFor(Family.all(CameraComponent::class.java).get())
    }


    override fun update(deltaTime: Float) {
        activeCamera.update()

        actors.forEach {
            if (it != null && it is Actor) {
                it.tick(deltaTime)
            }
        }
    }

    fun resize(width: Int, height: Int) {
        val aspectRatio = height / width.toFloat()
        val w = VIEWPORT_WIDTH.toFloat()

        val pos = activeCamera.position.cpy()
        if (aspectRatio < 1) {
            activeCamera.setToOrtho(false, w, w * aspectRatio)
        } else {
            activeCamera.setToOrtho(false, w / aspectRatio, w)
        }

        activeCamera.position.set(pos)
        activeCamera.update()
    }

    companion object {
        val DEFAULT_CAMERA: OrthographicCamera = OrthographicCamera()
    }
}