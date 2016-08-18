package com.borschlabs.xcom.renderer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

/**
 * @author octopussy
 */

class RenderContext(val batch: Batch, val shapeRenderer: ShapeRenderer, val camera: OrthographicCamera) {
    var blending: Boolean = false
        set(value) {
            field = value
            if (value) {
                Gdx.gl.glEnable(GL20.GL_BLEND)
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            } else {
                Gdx.gl.glDisable(GL20.GL_BLEND)
            }
        }
}