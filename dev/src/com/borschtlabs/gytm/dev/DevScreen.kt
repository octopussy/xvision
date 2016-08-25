package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader

/**
 * @author octopussy
 */

class DevScreen : ScreenAdapter() {

    private lateinit var font: BitmapFont
    private lateinit var guiCam: OrthographicCamera
    private lateinit var batch: Batch
    private lateinit var guiBatch: Batch

    private lateinit var cam: OrthographicCamera

    private lateinit var level: Level

    private lateinit var levelRenderer: LevelRenderer

    override fun show() {
        cam = OrthographicCamera()

        batch = SpriteBatch()
        guiBatch = SpriteBatch()
        guiCam = OrthographicCamera()
        font = BitmapFont(true)

        val params = TmxMapLoader.Parameters()
        params.generateMipMaps = true
        params.textureMagFilter = Texture.TextureFilter.MipMapNearestNearest
        params.textureMinFilter = Texture.TextureFilter.MipMapNearestNearest

        resize(Gdx.graphics.width, Gdx.graphics.height)

        level = LevelLoader("maps").load("test")

        levelRenderer = LevelRenderer(level, batch)
    }

    override fun dispose() {
        levelRenderer.dispose()
        level.dispose()
        font.dispose()
        batch.dispose()
        guiBatch.dispose()

    }
    override fun resize(width: Int, height: Int) {
        guiCam.setToOrtho(true, width.toFloat(), height.toFloat())
        cam.setToOrtho(false, width.toFloat(), height.toFloat())
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
            return
        }

        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        cam.update()

        levelRenderer.render(cam)

        drawUI()
    }

    private fun drawUI() {
        guiCam.update()
        guiBatch.projectionMatrix = guiCam.combined
        guiBatch.begin()
        font.draw(guiBatch, "${Gdx.graphics.framesPerSecond} fps\n" +
                "w: ${Gdx.graphics.width} h: ${Gdx.graphics.height}\n" +
                "ar: ${Gdx.graphics.height / Gdx.graphics.width.toFloat()}", 10f, 10f)
        guiBatch.end()
    }

}