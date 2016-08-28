package com.borschtlabs.gytm.dev.core

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.borschtlabs.gytm.dev.DevInputController

/**
 * @author octopussy
 */

abstract class BaseScreen : ScreenAdapter() {
    private val VIEWPORT_WIDTH = 30

    val engine: Engine = Engine()

    val world: World = World()

    private lateinit var font: BitmapFont

    lateinit var cam: OrthographicCamera
    lateinit var batch: Batch

    private lateinit var guiCam: OrthographicCamera
    private lateinit var guiBatch: Batch

    protected lateinit var debugSR: ShapeRenderer

    abstract fun onTap(px: Float, py: Float)

    override fun show() {
        cam = OrthographicCamera()

        font = BitmapFont(true)

        batch = SpriteBatch()
        guiBatch = SpriteBatch()
        guiCam = OrthographicCamera()

        Gdx.input.inputProcessor = DevInputController(cam, {
            x, y ->
            onTap(x, y)
        })

        cam.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_WIDTH / 2f - 10, 0f)
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun dispose() {
        font.dispose()
        batch.dispose()
        guiBatch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        guiCam.setToOrtho(true, width.toFloat(), height.toFloat())

        val aspectRatio = height / width.toFloat()
        val w = VIEWPORT_WIDTH.toFloat()

        val pos = cam.position.cpy()
        if (aspectRatio < 1) {
            cam.setToOrtho(false, w, w * aspectRatio)
        } else {
            cam.setToOrtho(false, w / aspectRatio, w)
        }

        cam.position.set(pos)
        cam.update()
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
            return
        }

        engine.update(delta)
    }

    fun drawDebugUI() {
        guiCam.update()
        guiBatch.projectionMatrix = guiCam.combined
        guiBatch.begin()
        font.draw(guiBatch, "${Gdx.graphics.framesPerSecond} fps\n" +
                "w: ${Gdx.graphics.width} h: ${Gdx.graphics.height}\n" +
                "ar: ${Gdx.graphics.height / Gdx.graphics.width.toFloat()}", 10f, 10f)
        guiBatch.end()
    }

    fun drawDebugRect(x: Float, y: Float, size: Float = 1f) {
        debugSR.rect(x.toFloat(), y.toFloat(), size, size)
    }
}
