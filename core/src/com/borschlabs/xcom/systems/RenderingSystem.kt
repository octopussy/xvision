package com.borschlabs.xcom.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.components.TextureComponent
import com.borschlabs.xcom.components.TransformComponent

/**
 * @author octopussy
 */

class RenderingSystem(val tiledMap: TiledMap) : EntitySystem() {

    private val batch: SpriteBatch = SpriteBatch()
    private val camera: OrthographicCamera = OrthographicCamera(1000.0f, 1000.0f)

    private val tiledMapRenderer: OrthogonalTiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, batch)

    private var objects: ImmutableArray<Entity> = ImmutableArray(Array())

    private val transformM:ComponentMapper<TransformComponent> = ComponentMapper.getFor(TransformComponent::class.java)
    private val textureM:ComponentMapper<TextureComponent> = ComponentMapper.getFor(TextureComponent::class.java)

    override fun addedToEngine(engine: Engine) {
        objects = engine.getEntitiesFor(Family.all(TransformComponent::class.java, TextureComponent::class.java).get())
    }

    override fun removedFromEngine(engine: Engine) {
    }

    override fun update(deltaTime: Float) {
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        batch.begin()

        for (obj in objects) {
            val transform = transformM.get(obj)
            val texture = textureM.get(obj)
            batch.draw(texture.region, transform.pos.x, transform.pos.y)
        }

        batch.end()
    }

    fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
    }
}