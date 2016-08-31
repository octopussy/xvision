package com.borschtlabs.gytm.dev

import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector3
import com.borschtlabs.gytm.dev.core.CameraActor

/**
 * @author octopussy
 */

class DevInputController(val camera: CameraActor, tapListener:(x: Float, y: Float)->Unit) :
        GestureDetector(GestureListener(camera, tapListener)) {

    override fun scrolled(amount: Int): Boolean {
        camera.zoom += amount * 0.2f
        camera.zoom = Math.max(0.6f, Math.min(2.0f, camera.zoom))

        return true
    }

    private class GestureListener(val camera: CameraActor, val tapListener:(x: Float, y: Float)->Unit): GestureAdapter() {
        override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
            val centerScr = Vector3()
            camera.move(camera.unproject(Vector3().add(-deltaX, -deltaY, 0f)).sub(camera.unproject(centerScr)))
            return true
        }

        override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
            val vec = camera.unproject(Vector3(x, y, 0f))
            tapListener(vec.x, vec.y)
            return true
        }
    }
}