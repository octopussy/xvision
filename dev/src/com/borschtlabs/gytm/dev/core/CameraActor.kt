package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.math.Vector3
import com.borschtlabs.gytm.dev.core.systems.RenderingSystem

/**
 * @author octopussy
 */

class CameraActor(world: World) : Actor(world) {

    private val renderingSystem: RenderingSystem get() = world.engine.getSystem(RenderingSystem::class.java)

    private var camComponent: CameraComponent

    init {
        camComponent = createComponent<CameraComponent> {}
        rootComponent = camComponent
    }

    var zoom: Float
        get() = camComponent.camera.zoom
        set(value) {
            camComponent.camera.zoom = value
        }

    fun setAsActiveCamera() {
        renderingSystem.activeCamera = getComponent(CameraComponent::class.java).camera
    }

    fun unproject(vec: Vector3): Vector3 = camComponent.camera.unproject(vec)

    fun move(vec: Vector3) = camComponent.camera.translate(vec)
}