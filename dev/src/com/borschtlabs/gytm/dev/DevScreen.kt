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

fun ShapeRenderer.draw(type: ShapeRenderer.ShapeType, color: Color, block: () -> Unit) {
    begin(type)
    this.color = color
    block.invoke()
    end()
}

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

    val dist = 50

    val startX = 2
    val startY = 6
    val unitSize = 2

    var endX = 8
    var endY = 2
    val areas = mutableMapOf<Int, TurnArea>()

    var path: List<TurnArea.WayPoint>? = null
    var smoothedPath: List<TurnArea.WayPoint>? = null


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

        Gdx.input.inputProcessor = DevInputController(cam, onTap)

        cam.position.set(VIEWPORT_WIDTH / 2f, VIEWPORT_WIDTH / 2f - 10, 0f)
        resize(Gdx.graphics.width, Gdx.graphics.height)

        areas.put(1, TurnArea.create(level, startX, startY, 1, dist))
        areas.put(2, TurnArea.create(level, startX, startY, 2, dist))
        areas.put(3, TurnArea.create(level, startX, startY, 3, dist))
        areas.put(4, TurnArea.create(level, startX, startY, 4, dist))
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

    private val onTap: (x: Float, y: Float) -> Unit = { x, y ->
        when {
            unitSize % 2 != 0 -> {
                endX = Math.floor(x.toDouble() - unitSize / 2).toInt()
                endY = Math.floor(y.toDouble() - unitSize / 2).toInt()
            }

            else -> {
                endX = Math.round(x.toDouble() - unitSize / 2).toInt()
                endY = Math.round(y.toDouble() - unitSize / 2).toInt()
            }
        }

        path = areas[unitSize]!!.getPath(startX, startY, endX, endY, unitSize / 2.5f, false)
        smoothedPath = areas[unitSize]!!.getPath(startX, startY, endX, endY, unitSize / 2.5f, true)
    }

    private fun drawDebugLevelGfx() {
        enableBlending()

        debugSR.projectionMatrix = cam.combined

        debugSR.draw(ShapeRenderer.ShapeType.Filled, Color(1f, 0f, 0f, 0.5f)) {
            for (y in 0..level.height - 1) {
                for (x in 0..level.width - 1) {
                    val cell = level.getCell(x, y)
                    cell?.apply {
                        if (isWall) {
                            // drawDebugRect(x, y)
                        }
                    }
                }
            }
        }

        debugSR.draw(ShapeRenderer.ShapeType.Line, Color.MAROON) {
            drawDebugRect(endX.toFloat(), endY.toFloat(), unitSize.toFloat())
        }

        debugSR.draw(ShapeRenderer.ShapeType.Filled, Color(0f, 1f, 0f, 0.3f)) {
            drawDebugTurnArea(startX, startY, unitSize, dist)
        }

        debugSR.draw(ShapeRenderer.ShapeType.Line, Color(1f, 0f, 0f, 1f)) {
            path?.let { drawDebugPath(it) }
        }

        debugSR.draw(ShapeRenderer.ShapeType.Line, Color(0f, 0f, 1f, 1f)) {
            smoothedPath?.let { drawDebugPath(it) }
        }


        /*debugSR.begin(ShapeRenderer.ShapeType.Line)
        debugSR.color = Color.BROWN
        debugSR.line(2.7891815f, 6.3675447f, 5.7891817f, 5.3675447f)

        debugSR.color = Color.YELLOW
        debugSR.line(2.715395f,6.146185f, 5.715395f,5.146185f)

        debugSR.end()*/

    }

    private fun drawDebugPath(path: List<TurnArea.WayPoint>) {
        for (i in 0..path.size - 2) {
            debugSR.line(path[i].center, path[i + 1].center)
        }

        path.forEach {
            debugSR.circle(it.center.x, it.center.y, 0.1f, 10)
        }
    }

    private fun drawDebugTurnArea(x: Int, y: Int, unitSize: Int, maxDistance: Int) {
        val area = areas.getOrPut(unitSize) {
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