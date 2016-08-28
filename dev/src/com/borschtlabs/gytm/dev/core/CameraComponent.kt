package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.graphics.OrthographicCamera

/**
 * @author octopussy
 */

class CameraComponent(owner:Actor) : ActorComponent(owner) {
    val camera = OrthographicCamera()

    override fun update(dt: Float) {
        camera.position.set(location)
    }
}