package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.borschtlabs.gytm.dev.core.CameraActor
import com.borschtlabs.gytm.dev.game.BaseDevScreen
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

class DevScreen : BaseDevScreen() {

    private var cameraActor: CameraActor by Delegates.notNull()

    private val onTap: (x: Float, y: Float) -> Unit = {
        x, y ->
        Gdx.app.log("123", "$x $y")
    }

    override fun show() {
        super.show()

        world.loadLevel("test")

        cameraActor = world.spawnActor<CameraActor> {
            //location.set(20f, 20f, 0f)
            setAsActiveCamera()
        }

        Gdx.input.inputProcessor = DevInputController(cameraActor, onTap)
    }

    override fun render(delta: Float) {
        super.render(delta)

        drawDebugUI()
    }
}