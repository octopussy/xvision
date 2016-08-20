package com.borschlabs.xcom.components

import com.badlogic.ashley.core.Component
import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.FieldCell
import com.borschlabs.xcom.world.GameUnitTurnArea

/**
 * @author octopussy
 */

class GameUnitComponent(field: Field) : Component {

    var actionPoints:Int = 0

    var state:State = State.IDLE

    var cell:FieldCell? = null

    var isTurnAreaVisible = false

    var turnArea: GameUnitTurnArea = GameUnitTurnArea(field)

    fun startTurn(actionPoints: Int) {
        this.actionPoints = actionPoints
        if (cell != null)
            turnArea.calculateArea(cell!!, actionPoints)

    }
    companion object {
        enum class State {
            IDLE, MOVING
        }

    }
}