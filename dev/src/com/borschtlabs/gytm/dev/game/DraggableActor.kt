package com.borschtlabs.gytm.dev.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.TextureComponent
import com.borschtlabs.gytm.dev.core.World

/**
 * @author octopussy
 */

class DraggableActor(world: World) : Actor(world) {
    init {
        val tex = createComponent<TextureComponent> {
            region = TextureRegion(Texture(Gdx.files.internal("eye.png")))
        }

        rootComponent = tex
    }
}