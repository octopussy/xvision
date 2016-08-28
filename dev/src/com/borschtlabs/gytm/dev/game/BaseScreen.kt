package com.borschtlabs.gytm.dev.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.borschtlabs.gytm.dev.core.World
import com.borschtlabs.gytm.dev.core.systems.CoreSystem
import com.borschtlabs.gytm.dev.core.systems.WorldRenderingSystem

/**
 * @author octopussy
 */

abstract class BaseScreen : ScreenAdapter() {
    val engine: Engine = Engine()

    val world: World = World(engine)

    private lateinit var font: BitmapFont

    private lateinit var guiCam: OrthographicCamera
    private lateinit var guiBatch: Batch

    protected lateinit var debugSR: ShapeRenderer

    abstract fun onTap(px: Float, py: Float)

    override fun show() {
        initSystems()

        debugSR = ShapeRenderer()

        font = BitmapFont(true)

        guiBatch = SpriteBatch()
        guiCam = OrthographicCamera()

        resize(Gdx.graphics.width, Gdx.graphics.height)

        world.loadLevel("test")
    }

    private fun initSystems() {
        engine.addSystem(CoreSystem(world))
        engine.addSystem(WorldRenderingSystem(world))
    }

    override fun dispose() {
        font.dispose()
        guiBatch.dispose()
    }

    override fun resize(width: Int, height: Int) {
        engine.getSystem(CoreSystem::class.java).resize(width, height)
        guiCam.setToOrtho(true, width.toFloat(), height.toFloat())
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

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
