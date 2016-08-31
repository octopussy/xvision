package com.borschtlabs.gytm.dev.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.borschtlabs.gytm.dev.TurnArea
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.TextureComponent
import com.borschtlabs.gytm.dev.core.World

class PlayerActor(world: World) : GameUnitActor(world) {

    init {
        val texComponent = createComponent<TextureComponent> {  }
        texComponent.region = TextureRegion(Texture(Gdx.files.internal("player.png")))
        rootComponent = texComponent
    }
}