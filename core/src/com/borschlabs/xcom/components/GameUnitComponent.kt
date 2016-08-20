package com.borschlabs.xcom.components

import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.FieldCell
import com.borschlabs.xcom.world.GameUnitTurnArea
import com.borschlabs.xcom.world.Route

/**
 * @author octopussy
 */

class GameUnitComponent(val field: Field) : TransformComponent() {

    var actionPoints: Int = 0

    var state: State = State.IDLE

    var cell: FieldCell? = null

    var turnArea: GameUnitTurnArea = GameUnitTurnArea(field)

    var movingRoute: Route = Route()

    private var distancePassed: Float = 0f

    fun startTurn(actionPoints: Int) {
        state = State.IDLE
        this.actionPoints = actionPoints
        if (cell != null)
            turnArea.calculateArea(cell!!, actionPoints)

    }

    fun startMoving(route: Route) {
        distancePassed = 0f
        movingRoute = route
        state = State.MOVING
        cell = route.cells.last()

        actionPoints -= route.cells.size - 1
        if (actionPoints < 0 ) actionPoints = 0
    }

    fun stepMovement(deltaTime: Float, speed: Float) {
        distancePassed += deltaTime * speed

        val cells = movingRoute.cells
        val passed = distancePassed.toDouble()

        val indexA = Math.floor(passed).toInt()
        val indexB = Math.floor(passed).toInt() + 1

        if (indexA < 0 || indexA >= cells.size || indexB < 0 || indexB >= cells.size) {
            state = State.IDLE
            turnArea.calculateArea(cell!!, actionPoints)
        } else {
            val cellA = cells[indexA]
            val cellB = cells[indexB]
            val f = passed - Math.floor(passed)

            pos.x = ((cellB.x - cellA.x) * f + cellA.x).toFloat() * field.cellSize
            pos.y = ((cellB.y - cellA.y) * f + cellA.y).toFloat() * field.cellSize
        }
    }

    companion object {
        enum class State {
            IDLE, MOVING
        }

    }
}