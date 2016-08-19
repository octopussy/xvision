package com.borschlabs.xcom.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.Disposable

/**
 * @author octopussy
 */

class GameController(val world: World_) : Disposable {
    private var player: PlayerUnit? = null

    private var playerTexture: Texture

    init {
        playerTexture = Texture(Gdx.files.internal("player.png"), true)
        playerTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
    }

    override fun dispose() {
        playerTexture.dispose()
    }

    fun spawnPlayer(x: Int, y: Int) {
        val sprite = Sprite(playerTexture)

        player = PlayerUnit(world, sprite)
        player?.position = world.getCell(x, y)

        world.units.add(player!!)
    }

    fun startPlayerTurn() {
        player?.startNewTurn()
    }
}