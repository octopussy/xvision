package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

/**
 * @author octopussy
 */

class TextureComponent(actor: Actor) : ActorComponent(actor) {
    var region: TextureRegion? = null
    val origin: Vector2 = Vector2(0.5f, 0.5f)
}