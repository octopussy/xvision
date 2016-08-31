package com.borschtlabs.gytm.dev.game

import com.badlogic.gdx.Gdx
import com.borschtlabs.gytm.dev.TurnArea
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.World
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

open class GameUnitActor(world: World) : Actor(world) {

    var state: State = State.IDLE

    var turnArea: TurnArea by Delegates.notNull()

    val movingRoute: MutableList<TurnArea.WayPoint> = mutableListOf()

    var cellX: Int = 0

    var cellY: Int = 0

    private var distancePassed: Float = 0f

    fun startTurn() {
        state = State.IDLE
        turnArea = TurnArea.create(world.level, cellX, cellY, 1, 50)
    }

    fun moveToCell(toCellX: Int, toCellY: Int) {
        if (state == State.IDLE) {
            startTurn()
        }

        if (!turnArea.getPath(cellX, cellY, toCellX, toCellY, 1f, true, movingRoute)) {
            return
        }

        cellX = toCellX
        cellY = toCellY

        distancePassed = 0f
        state = State.MOVING
    }

    override fun tick(dt: Float) {
        if (state == State.MOVING) {
            stepMovement(dt, 5.0f)
        }
    }

    fun stepMovement(deltaTime: Float, speed: Float) {
        distancePassed += deltaTime * speed

        val cells = movingRoute
        val passed = distancePassed.toDouble()

        val indexA = Math.floor(passed).toInt()
        val indexB = Math.floor(passed).toInt() + 1

        if (indexA < 0 || indexA >= cells.size || indexB < 0 || indexB >= cells.size) {
            Gdx.app.log("333", "$indexA $indexB  ${cells.size}")
            state = State.IDLE
            startTurn()
        } else {
            val cellA = cells[indexA]
            val cellB = cells[indexB]
            val f = passed - Math.floor(passed)

            location.x = ((cellB.x - cellA.x) * f + cellA.x).toFloat()
            location.y = ((cellB.y - cellA.y) * f + cellA.y).toFloat()
        }
    }

    companion object {
        enum class State {
            IDLE, MOVING
        }

    }
}