package com.borschtlabs.gytm.dev

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector3

/**
 * @author octopussy
 */

class DevInputController(val camera: OrthographicCamera) : GestureDetector(GestureListener(camera)) {

    override fun scrolled(amount: Int): Boolean {
        camera.zoom += amount * 0.2f
        camera.zoom = Math.max(0.6f, Math.min(2.0f, camera.zoom))

        return true
    }
    private class GestureListener(val camera: Camera): GestureAdapter() {
        override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
            val centerScr = Vector3()
            camera.translate(camera.unproject(Vector3().add(-deltaX, -deltaY, 0f)).sub(camera.unproject(centerScr)))
            return true
        }
    }
}