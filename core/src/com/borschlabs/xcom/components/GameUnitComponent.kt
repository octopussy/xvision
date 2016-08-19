package com.borschlabs.xcom.components

import com.badlogic.ashley.core.Component
import com.borschlabs.xcom.world.Field
import com.borschlabs.xcom.world.FieldCell
import com.borschlabs.xcom.world.GameUnitTurnArea

/**
 * @author octopussy
 */

class GameUnitComponent(field: Field) : Component {

    var actionPoints:Int = 4

    var state:State = State.IDLE

    var cell:FieldCell = field.getCell(0, 0)

    var isTurnAreaVisible = false

    var isTurnAreaCalculated = false

    var turnArea: GameUnitTurnArea = GameUnitTurnArea(field)

    companion object {
        enum class State {
            IDLE, MOVING
        }
    }
}