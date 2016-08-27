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

        Gdx.input.inputProcessor = DevInputController(cam)

        cam.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_WIDTH / 2f - 10, 0f)
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

        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        cam.update()
        levelRenderer.render(cam)

        drawDebugLevelGfx()

        drawUI()
    }

    private val _areas = mutableMapOf<Int, TurnArea>()

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
                       // drawDebugRect(x, y)
                    }
                }
            }
        }

        debugSR.end()

        debugSR.begin(ShapeRenderer.ShapeType.Filled)
        debugSR.projectionMatrix = cam.combined

        val dist = 50

        val startX = 13
        val startY = 16
        val endX = 39
        val endY = 19
        val unitSize = 1

        debugSR.color = Color(1f, 0f, 1f, 0.5f)
        drawDebugTurnArea(startX, startY, unitSize, dist)

        debugSR.end()

        val path = _areas[unitSize]!!.getPath(startX, startY, endX, endY, false)
        val smoothedPath = _areas[unitSize]!!.getPath(startX, startY, endX, endY, true)

        debugSR.begin(ShapeRenderer.ShapeType.Line)

        debugSR.color = Color(1f, 0f, 0f, 1f)
        drawDebugPath(path, 1)

        debugSR.color = Color(0f, 0f, 1f, 1f)
        drawDebugPath(smoothedPath, 1)

        debugSR.end()

    }

    private fun drawDebugPath(path: List<TurnArea.WayPoint>, unitSize: Int) {
        for (i in 0..path.size - 2) {
            debugSR.line(path[i].center, path[i + 1].center)
        }

        path.forEach {
            debugSR.circle(it.center.x, it.center.y, 0.1f, 10)
        }
    }

    private fun drawDebugTurnArea(x: Int, y: Int, unitSize:Int, maxDistance: Int) {
        val area = _areas.getOrPut(unitSize) {
            TurnArea.create(level, x, y, unitSize, maxDistance)
        }

        val shift = (unitSize - 1) * 0.5f
        area.waypoints.forEach { drawDebugRect(it.x + shift, it.y + shift, 1f) }
    }

    private fun drawDebugRect(x: Float, y: Float, size: Float = 1f) {
        debugSR.rect(x.toFloat(), y.toFloat(), size, size)
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