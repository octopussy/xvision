package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.borschtlabs.gytm.dev.core.CameraActor
import com.borschtlabs.gytm.dev.core.systems.CoreSystem
import com.borschtlabs.gytm.dev.core.systems.InputDelegate
import com.borschtlabs.gytm.dev.game.BaseDevScreen
import com.borschtlabs.gytm.dev.game.DraggableActor
import com.borschtlabs.gytm.dev.game.PlayerActor
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

class DevScreen : BaseDevScreen(), InputDelegate {

    private var player: PlayerActor by Delegates.notNull()

    private var cameraActor: CameraActor by Delegates.notNull()

    private var dragActor: DraggableActor by Delegates.notNull()

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

        dragActor = world.spawnActor<DraggableActor> {
            boundsRadius = 1f
            location.set(2f, 2f, 0f)
        }

        val coreSys = engine.getSystem(CoreSystem::class.java)
        coreSys.inputDelegate = this

        Gdx.input.inputProcessor = InputMultiplexer(coreSys, DevInputController(cameraActor))
    }

    override fun render(delta: Float) {
        super.render(delta)

        drawDebugUI()
    }

    private var isActorDragging: Boolean = false

    private var actorStartDragLocation: Vector3 = Vector3()

    private val startDragLocation: Vector2 = Vector2()

    override fun touchDown(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
        val hit = dragActor.hit(worldX, worldY)

        if (hit) {
            isActorDragging = true
            startDragLocation.set(worldX, worldY)
            Gdx.app.log("down", "$startDragLocation")
            actorStartDragLocation.set(dragActor.location)
        }

        return hit
    }

    override fun touchDragged(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
        if (isActorDragging) {
            Gdx.app.log("drag", "$worldX")
            dragActor.location.set(actorStartDragLocation.x + (worldX - startDragLocation.x),
                    actorStartDragLocation.y + (worldY - startDragLocation.y), 0f)
        }
        return isActorDragging
    }

    override fun touchUp(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
        val handle = isActorDragging
        isActorDragging = false
        return handle
    }
}

