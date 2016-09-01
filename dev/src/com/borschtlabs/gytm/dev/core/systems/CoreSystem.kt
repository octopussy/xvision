package com.borschtlabs.gytm.dev.core.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector3
import com.borschtlabs.gytm.dev.core.World

/**
 * @author octopussy
 */

class CoreSystem(val world: World) : EntitySystem(0), InputProcessor {

    var inputDelegate: InputDelegate = NULL_INPUT_DELEGATE

    private val rs: RenderingSystem by lazy { engine.getSystem(RenderingSystem::class.java) }

    private val tempCoords3 = Vector3()
    //private val pointerOverActors = arrayOfNulls<Actor>(20)
    private val pointerTouched = BooleanArray(20)
    private val pointerScreenX = IntArray(20)
    private val pointerScreenY = IntArray(20)

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {

        if (screenX < rs.viewport.screenX || screenX >= rs.viewport.screenX + rs.viewport.screenWidth) return false
        if (Gdx.graphics.height - screenY < rs.viewport.screenY || Gdx.graphics.height - screenY >= rs.viewport.screenY + rs.viewport.screenHeight)
            return false

        pointerTouched[pointer] = true
        pointerScreenX[pointer] = screenX
        pointerScreenY[pointer] = screenY

        rs.activeCamera.unproject(tempCoords3.set(screenX.toFloat(), screenY.toFloat(), 0f))

        /*val event = Pools.obtain(InputEvent::class.java)
        event.type = InputEvent.Type.touchDown
        event.setStage(this)
        event.stageX = tempCoords3.x
        event.stageY = tempCoords3.y
        event.pointer = pointer
        event.button = button
*/
        /*val target = hit(tempCoords.x, tempCoords.y, true)
        if (target == null) {
            if (root.getTouchable() == Touchable.enabled) root.fire(event)
        } else {
            target!!.fire(event)
        }*/

        //val handled = event.isHandled
        //Pools.free(event)
        rs.activeCamera.unproject(tempCoords3.set(screenX.toFloat(), screenY.toFloat(), 0f))

        return inputDelegate.touchDown(screenX, screenY, tempCoords3.x, tempCoords3.y)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        pointerScreenX[pointer] = screenX
        pointerScreenY[pointer] = screenY

        rs.activeCamera.unproject(tempCoords3.set(screenX.toFloat(), screenY.toFloat(), 0f))

        return inputDelegate.touchDragged(screenX, screenY, tempCoords3.x, tempCoords3.y)
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        pointerTouched[pointer] = false
        pointerScreenX[pointer] = screenX
        pointerScreenY[pointer] = screenY

        return inputDelegate.touchUp(screenX, screenY, tempCoords3.x, tempCoords3.y)
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    companion object {
        private val NULL_INPUT_DELEGATE: InputDelegate = object : InputDelegate {
            override fun touchDown(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
                return false
            }

            override fun touchDragged(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
                return false
            }

            override fun touchUp(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean {
                return false
            }

        }
    }
}