package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import enableBlending

/**
 * @author octopussy
 */

class DevScreen : ScreenAdapter() {

    private val VIEWPORT_WIDTH = 30

    private lateinit var font: BitmapFont
    private lateinit var guiCam: OrthographicCamera
    private lateinit var batch: Batch
    private lateinit var guiBatch: Batch

    private lateinit var cam: OrthographicCamera

    private lateinit var level: Level

    private lateinit var levelRenderer: LevelRenderer

    private lateinit var debugSR: ShapeRenderer

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

        level = LevelLoader("maps").load("test")

        levelRenderer = LevelRenderer(level, batch)

        debugSR = ShapeRenderer()

        resize(Gdx.graphics.width, Gdx.graphics.height)
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

        val aspectRatio = height / width.toFloat()
        val w = VIEWPORT_WIDTH.toFloat()

        if (aspectRatio < 1) {
            cam.setToOrtho(false, w, w * aspectRatio)
        } else {
            cam.setToOrtho(false, w / aspectRatio, w)
        }
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

        drawDebugLevelGfx()

        drawUI()
    }

    private fun drawDebugLevelGfx() {
        enableBlending()

        debugSR.begin(ShapeRenderer.ShapeType.Filled)
        debugSR.projectionMatrix = cam.combined
        debugSR.color = Color(1f, 0f, 0f, 0.5f)

        for(y in 0..level.height - 1) {
            for (x in 0..level.width - 1) {
                val cell = level.getCell(x, y)
                cell?.apply {
                    if (isWall) {
                        drawDebugRect(x, y)
                    }
                }
            }
        }

        debugSR.end()

        debugSR.begin(ShapeRenderer.ShapeType.Filled)
        debugSR.projectionMatrix = cam.combined

        drawNavGrid(3, Color(0f, 1f, 0f, 0.5f))

        debugSR.end()
    }

    private fun drawNavGrid(size:Int, color: Color) {
        debugSR.color = color

        val grid: NavGrid = level.navGrids.get(size)
        for ((x, y) in grid.waypoints) {
            debugSR.circle(x, y, 0.1f, 8)
        }
    }

    private fun drawDebugRect(x: Int, y: Int) {
        debugSR.rect(x.toFloat(), y.toFloat(), 1f, 1f)
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