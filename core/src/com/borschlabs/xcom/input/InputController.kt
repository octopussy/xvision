package com.borschlabs.xcom.input

import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenAccessor
import aurelienribon.tweenengine.TweenManager
import aurelienribon.tweenengine.equations.Quad
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

/**
 * @author octopussy
 */
class InputController(private val camera: OrthographicCamera) : GestureDetector(InputController.GListener(camera)) {

    private val tweenManager: TweenManager

    init {
        camera.zoom = MIN_ZOOM

        tweenManager = TweenManager()
        Tween.registerAccessor(OrthographicCamera::class.java, CameraAccessor())
    }

    fun update(deltaTime: Float) {
        tweenManager.update(deltaTime)
    }

    override fun scrolled(amount: Int): Boolean {
        tweenManager.killAll()

        Tween.to(camera, CameraAccessor.TYPE_ZOOM, 1f).target(camera.zoom + ZOOM_SPEED * amount).ease(Quad.OUT).start(tweenManager)

        return false
    }

    private class GListener internal constructor(private val camera: OrthographicCamera) : GestureDetector.GestureAdapter() {

        private var startZoom: Float = 0.toFloat()

        override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
            if (pointer == 1) {
                startZoom = camera.zoom
            }
            return false
        }

        override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
            val centerScr = Vector3()
            camera.translate(camera.unproject(Vector3().add(-deltaX, -deltaY, 0f)).sub(camera.unproject(centerScr)))
            return true
        }

        override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
            return false
        }

        override fun zoom(initialDistance: Float, distance: Float): Boolean {
            camera.zoom = startZoom * initialDistance / distance

            camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM)
            return true
        }

        override fun pinch(initialPointer1: Vector2?, initialPointer2: Vector2?, pointer1: Vector2?, pointer2: Vector2?): Boolean {
            return false
        }
    }

    private class CameraAccessor : TweenAccessor<OrthographicCamera> {

        override fun getValues(target: OrthographicCamera, tweenType: Int, returnValues: FloatArray): Int {
            when (tweenType) {
                TYPE_ZOOM -> {
                    returnValues[0] = target.zoom
                    return 1
                }
                else -> {
                    returnValues[0] = 0f
                    return 1
                }
            }
        }

        override fun setValues(target: OrthographicCamera, tweenType: Int, newValues: FloatArray) {
            when (tweenType) {
                TYPE_ZOOM -> {
                    target.zoom = newValues[0]
                    target.zoom = MathUtils.clamp(target.zoom, MIN_ZOOM, MAX_ZOOM)
                }
                else -> {
                }
            }
        }

        companion object {

            internal val TYPE_ZOOM = 1
        }
    }

    companion object {

        private val MIN_ZOOM = 1.0f
        private val MAX_ZOOM = 3.0f
        private val ZOOM_SPEED = 1.0f
    }
}
