package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.math.Vector3
import com.borschtlabs.gytm.dev.core.systems.CoreSystem

/**
 * @author octopussy
 */

class CameraActor(world: World) : Actor(world) {

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

    private val core: CoreSystem get() = world.engine.getSystem(CoreSystem::class.java)

    fun setAsActiveCamera() {
        core.activeCamera = getComponent(CameraComponent::class.java).camera
    }

    fun unproject(vec: Vector3): Vector3 = camComponent.camera.unproject(vec)

    fun move(vec: Vector3) = camComponent.camera.translate(vec)
}