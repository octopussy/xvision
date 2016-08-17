package com.borschlabs.xcom

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import com.borschlabs.xcom.input.ScrollingInputController

/**
 * @author octopussy
 */

class GameScreen : Screen {

    private val VP_SIZE: Float = 100.0f

    private val disposables:MutableList<Disposable> = mutableListOf()

    private lateinit var fontTexture: Texture
    private lateinit var font: BitmapFont
    private lateinit var fontShader: ShaderProgram
    private lateinit var uiBatch: SpriteBatch

    private lateinit var camera: OrthographicCamera

    private lateinit var debugShapeRenderer: ShapeRenderer

    private lateinit var inputMultiplexer: InputMultiplexer
    private lateinit var mainInputController: GestureDetector

    override fun show() {
        createFonts()

        camera = OrthographicCamera()
        camera.update()
        debugShapeRenderer = ShapeRenderer()

        uiBatch = SpriteBatch()
        uiBatch.shader = fontShader
        disposables.addAll(arrayOf(uiBatch, debugShapeRenderer))

        initInput()
    }

    override fun pause() {
    }

    override fun hide() {
    }

    override fun resize(width: Int, height: Int) {
        val ar = width / height.toFloat()
        camera.setToOrtho(false, VP_SIZE * ar, VP_SIZE)

        val m = Matrix4()
        m.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
        uiBatch.projectionMatrix = m
    }

    override fun render(delta: Float) {
        //processInput()

        camera.update()

        debugShapeRenderer.projectionMatrix = camera.combined

        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        /*// player
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        debugShapeRenderer.color = Color.RED
        debugShapeRenderer.circle(player.x, player.y, 0.2f)
        debugShapeRenderer.end()
*/
        uiBatch.begin()
        font.color = Color.WHITE
        font.draw(uiBatch, "Hello epta!!! " + Gdx.graphics.framesPerSecond + " fps", 10f, (Gdx.graphics.height - 10).toFloat())

        font.draw(uiBatch, "wasd - movement; q,e - rotation; scroll - zooming", 10f, (Gdx.graphics.height - 50).toFloat())
        uiBatch.end()

        drawDebugGeometry()
    }

    override fun resume() {
    }

    override fun dispose() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    private fun drawDebugGeometry() {
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        debugShapeRenderer.color = Color.BLUE

        debugShapeRenderer.line(0.0f, 0.0f, 0.0f, 100.0f)
        debugShapeRenderer.line(0.0f, 100.0f, 100.0f, 100.0f)
        debugShapeRenderer.line(100.0f, 100.0f, 100.0f, 0.0f)
        debugShapeRenderer.line(100.0f, 0.0f, 0.0f, 0.0f)

        debugShapeRenderer.end()
    }

    private fun createFonts() {
        fontTexture = Texture(Gdx.files.internal("arial_black.png"))
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        font = BitmapFont(Gdx.files.internal("arial_black.fnt"), TextureRegion(fontTexture), false)
        fontShader = ShaderProgram(Gdx.files.internal("shaders/font.vsh"), Gdx.files.internal("shaders/font.fsh"))
        println(if (fontShader.isCompiled) "Font shader compiled!" else fontShader.log)
        disposables.addAll(arrayOf(fontTexture, font, fontShader))
    }

    private fun initInput() {
        mainInputController = ScrollingInputController(camera)
        inputMultiplexer = InputMultiplexer(mainInputController)
        Gdx.input.inputProcessor = inputMultiplexer
    }
}