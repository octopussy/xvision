package com.borschlabs.xcom

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import com.borschlabs.xcom.components.TextureComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.systems.CameraSystem
import com.borschlabs.xcom.systems.RenderingSystem

/**
 * @author octopussy
 */

class GameScreen : Screen {

    private val disposables: MutableList<Disposable> = mutableListOf()

    private lateinit var engine: Engine

    private lateinit var fontTexture: Texture
    private lateinit var font: BitmapFont
    private lateinit var fontShader: ShaderProgram

    override fun show() {
        createFonts()

      //  debugShapeRenderer = ShapeRenderer()

        //uiBatch = SpriteBatch()
        //uiBatch.shader = fontShader
        //disposables.addAll(arrayOf(uiBatch, debugShapeRenderer))

        val params = TmxMapLoader.Parameters()
        params.generateMipMaps = true
        params.textureMagFilter = Texture.TextureFilter.MipMapLinearLinear
        params.textureMinFilter = Texture.TextureFilter.MipMapLinearLinear
        val tiledMap = TmxMapLoader().load("maps/test.tmx", params)

        engine = PooledEngine()
        val cameraSystem = CameraSystem()
        engine.addSystem(cameraSystem)
        engine.addSystem(RenderingSystem(cameraSystem.camera, tiledMap))

        Gdx.input.inputProcessor = cameraSystem.inputProcessor

        createPlayer()

        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    private fun createPlayer() {
        val playerTexture = Texture(Gdx.files.internal("player.png"), true)
        playerTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);

        val player = Entity()
        val trans = TransformComponent()
        val texture = TextureComponent()
        trans.pos = Vector3(32.0f, 32.0f, 0.0f)
        trans.rotation = 45.0f
        texture.region = TextureRegion(playerTexture)
        player.add(trans)
        player.add(texture)

        engine.addEntity(player)
    }

    private fun initGameController() {
        //gameController = GameController(world)
        //disposables.add(gameController)
    }

    override fun pause() {
    }

    override fun hide() {
    }

    override fun resize(width: Int, height: Int) {
        engine.getSystem(RenderingSystem::class.java)?.resize(width, height)
       // camera.setToOrtho(false, width.toFloat(), height.toFloat())
       // camera.position.set(world.width / 2.0f, world.height / 2.0f, 0.0f)
      //  camera.update()

      //  val m = Matrix4()
      //  m.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
    //    uiBatch.projectionMatrix = m
    }

    override fun render(delta: Float) {
      //  mainInputController.update(delta)

      //  camera.update()

      //  debugShapeRenderer.projectionMatrix = camera.combined

        Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        engine.update(delta)

        /*// player
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        debugShapeRenderer.color = Color.RED
        debugShapeRenderer.circle(player.x, player.y, 0.2f)
        debugShapeRenderer.end()
*/
        /*uiBatch.begin()
        font.color = Color.WHITE
        font.draw(uiBatch, "Hello epta!!! " + Gdx.graphics.framesPerSecond + " fps", 10f, (Gdx.graphics.height - 10).toFloat())

        font.draw(uiBatch, "wasd - movement; q,e - rotation; scroll - zooming", 10f, (Gdx.graphics.height - 50).toFloat())
        uiBatch.end()*/

   //     worldRenderer.render(delta)
    }

    override fun resume() {
    }

    override fun dispose() {
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    private fun createFonts() {
        fontTexture = Texture(Gdx.files.internal("arial_black.png"))
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        font = BitmapFont(Gdx.files.internal("arial_black.fnt"), TextureRegion(fontTexture), false)
        fontShader = ShaderProgram(Gdx.files.internal("shaders/font.vsh"), Gdx.files.internal("shaders/font.fsh"))
        println(if (fontShader.isCompiled) "Font shader compiled!" else fontShader.log)
        disposables.addAll(arrayOf(fontTexture, font, fontShader))
    }
}