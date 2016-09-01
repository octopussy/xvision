package com.borschtlabs.gytm.dev.game

import com.badlogic.gdx.math.Vector2
import com.borschtlabs.gytm.dev.PathInterpolator
import com.borschtlabs.gytm.dev.TurnArea
import com.borschtlabs.gytm.dev.core.Actor
import com.borschtlabs.gytm.dev.core.World
import kotlin.properties.Delegates

/**
 * @author octopussy
 */

open class GameUnitActor(world: World) : Actor(world) {

    private var path: PathInterpolator by Delegates.notNull()

    var state: State = State.IDLE

    var turnArea: TurnArea by Delegates.notNull()

    var cellX: Int = 0

    var cellY: Int = 0

    private var distancePassed: Double = 0.0

    private val unitSize: Int = 2

    fun startTurn() {
        state = State.IDLE
        turnArea = TurnArea.create(world.level, cellX, cellY, unitSize, 50)
    }

    fun moveToCell(toCellX: Int, toCellY: Int) {
        if (state == State.IDLE) {
            startTurn()
        } else {
            return
        }

        val movingRoute = mutableListOf<TurnArea.WayPoint>()
        if (!turnArea.getPath(cellX, cellY, toCellX, toCellY, unitSize / 2.0f, true, movingRoute)) {
            return
        }

        path = PathInterpolator()
        movingRoute.forEach {
            path.addPoint(it.center)
        }

        cellX = toCellX
        cellY = toCellY

        distancePassed = 0.0
        state = State.MOVING
    }

    override fun tick(dt: Float) {
        if (state == State.MOVING) {
            stepMovement(dt, 10.0f)
        }
    }

    fun stepMovement(deltaTime: Float, speed: Float) {
        distancePassed += deltaTime * speed

        val pos = Vector2()
        if (!path.interpolate(distancePassed, pos)) {
            state = State.IDLE
            startTurn()
        }

        location.set(pos.x, pos.y)
    }

    companion object {
        enum class State {
            IDLE, MOVING
        }

    }
}