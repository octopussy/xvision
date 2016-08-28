package com.borschtlabs.gytm.dev

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapRenderer
import com.badlogic.gdx.math.Matrix4

/**
 * @author octopussy
 */

class EmptyMapRenderer : MapRenderer {
    override fun setView(camera: OrthographicCamera?) { }

    override fun setView(projectionMatrix: Matrix4?, viewboundsX: Float, viewboundsY: Float, viewboundsWidth: Float, viewboundsHeight: Float) { }

    override fun render() { }

    override fun render(layers: IntArray?) { }
}