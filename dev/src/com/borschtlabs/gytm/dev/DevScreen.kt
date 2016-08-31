package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.borschtlabs.gytm.dev.core.CameraActor
import com.borschtlabs.gytm.dev.game.BaseDevScreen
import com.borschtlabs.gytm.dev.game.PlayerActor
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

class DevScreen : BaseDevScreen() {

    private var player: PlayerActor by Delegates.notNull()

    private var cameraActor: CameraActor by Delegates.notNull()

    private val onTap: (x: Float, y: Float) -> Unit = {
        x, y -> player.moveToCell(x.toInt(), y.toInt())
    }

    override fun show() {
        super.show()

        world.loadLevel("test")

        player = world.spawnActor<PlayerActor> {
            location.set(0f, 0f, 0f)
        }

        cameraActor = world.spawnActor<CameraActor> {
            setAsActiveCamera()
        }

        Gdx.input.inputProcessor = DevInputController(cameraActor, onTap)
    }

    override fun render(delta: Float) {
        super.render(delta)

        drawDebugUI()
    }
}

