package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.Vector2
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.CameraActor
import com.borschtlabs.gytm.dev.core.VisibilityComponent
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

    private var dragActors = mutableSetOf<Actor>()
    private var draggingActor: Actor? = null

    private var actorStartDragLocation: Vector2 = Vector2()

    private val startDragLocation: Vector2 = Vector2()

    private val onTap: (x: Float, y: Float) -> Unit = {
        x, y -> player.moveToCell(x.toInt(), y.toInt())
    }

    override fun show() {
        super.show()

        world.loadLevel("test")

        /*player = world.spawnActor<PlayerActor> {
            location.set(10f, 10f)

            createComponent<VisibilityComponent> {
                isEnabled = true
            }
        }*/

        cameraActor = world.spawnActor<CameraActor> {
            setAsActiveCamera()
        }

        dragActors.add(world.spawnActor<DraggableActor> {
            boundsRadius = 1f
            location.set(2f, 2f)

            createComponent<VisibilityComponent> {
                isEnabled = true
            }
        })

        dragActors.add(world.spawnActor<DraggableActor> {
            boundsRadius = 1f
            location.set(20f, 20f)

            createComponent<VisibilityComponent> {
                isEnabled = true
            }
        })

        dragActors.add(world.spawnActor<DraggableActor> {
            boundsRadius = 1f
            location.set(35f, 30f)

            createComponent<VisibilityComponent> {
                isEnabled = true
            }
        })

        val coreSys = engine.getSystem(CoreSystem::class.java)
        coreSys.inputDelegate = this

        Gdx.input.inputProcessor = InputMultiplexer(coreSys, DevInputController(cameraActor))
    }

    override fun render(delta: Float) {
        super.render(delta)

        drawDebugUI()
    }

    override fun touchDown(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
        dragActors.forEach {
            if (it.hit(worldX, worldY)){
                draggingActor = it
                startDragLocation.set(worldX, worldY)
                actorStartDragLocation.set(it.location)
                return@forEach
            }
        }

        return draggingActor != null
    }

    override fun touchDragged(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
        if (draggingActor != null) {
            draggingActor!!.location.set(actorStartDragLocation.x + (worldX - startDragLocation.x),
                    actorStartDragLocation.y + (worldY - startDragLocation.y))
        }
        return draggingActor != null
    }

    override fun touchUp(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
        val handle = draggingActor != null
        draggingActor = null
        return handle
    }
}

