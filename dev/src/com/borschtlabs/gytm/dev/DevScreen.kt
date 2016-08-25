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
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

/**
 * @author octopussy
 */

class DevScreen : ScreenAdapter() {

    private lateinit var font: BitmapFont
    private lateinit var guiCam: OrthographicCamera
    private lateinit var batch: Batch
    private lateinit var guiBatch: Batch

    private lateinit var cam: OrthographicCamera
    private lateinit var tiledMapRenderer: TiledMapRenderer

    private lateinit var tiledMap: TiledMap

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

        tiledMap = TmxMapLoader().load("maps/test.tmx", params)

        tiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

        resize(Gdx.graphics.width, Gdx.graphics.height)

        val level = LevelLoader("maps").load("test")
    }

    override fun dispose() {
        font.dispose()
        tiledMap.dispose()
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

        //batch.projectionMatrix = cam.combined
        tiledMapRenderer.setView(cam)
        tiledMapRenderer.render()

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