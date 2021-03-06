package com.borschtlabs.gytm.dev.core.systems

/**
 * @author octopussy
 */
interface InputDelegate {
    fun touchDown(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean

    fun touchDragged(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean

    fun touchUp(screenX: Int, screenY: Int, worldX: Float, worldY: Float): Boolean
}