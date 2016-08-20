package com.borschlabs.xcom.entities

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.borschlabs.xcom.components.GameUnitComponent
import com.borschlabs.xcom.components.PlayerComponent
import com.borschlabs.xcom.components.TextureComponent
import com.borschlabs.xcom.components.TransformComponent
import com.borschlabs.xcom.world.Field

/**
 * @author octopussy
 */

class Player(val field: Field) : Entity() {

    private var unit: GameUnitComponent

    init {
        val playerTexture = Texture(Gdx.files.internal("player.png"), true)
        playerTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);

        val trans = TransformComponent()
        val texture = TextureComponent()
        val playerComp = PlayerComponent()

        unit = GameUnitComponent(field)
        texture.region = TextureRegion(playerTexture)
        add(trans)
        add(texture)
        add(playerComp)
        add(unit)
    }

    fun setCell(x: Int, y: Int) {
        unit.cell = field.getCell(x, y)
    }
}