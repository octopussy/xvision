package com.borschtlabs.gytm.dev.core

import com.borschtlabs.gytm.dev.core.systems.CoreSystem

/**
 * @author octopussy
 */

class CameraActor(world: World) : Actor(world) {

    init {
        rootComponent = createComponent<CameraComponent> {}
    }

    fun setAsActiveCamera() {
        world.engine.getSystem(CoreSystem::class.java).activeCamera = getComponent(CameraComponent::class.java).camera
    }
}