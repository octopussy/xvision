package com.borschlabs.xcom.world

import com.borschlabs.xcom.clamp

/**
 * @author octopussy
 */

class UnitTurnArea(val field: Field) {
    private var _reachableCells: MutableSet<FieldCell> = mutableSetOf()

    val reachableCells: List<FieldCell>
        get() = _reachableCells.toList()

    fun calculateArea(startX: Int, startY: Int, maxDistance: Int) {
        _reachableCells = mutableSetOf()
        _reachableCells.add(FieldCell(startX, startY))

        val minX = (startX - Math.ceil(maxDistance / 2.0).toInt() - 1).clamp(0, field.width - 1)
        val minY = (startY - Math.ceil(maxDistance / 2.0).toInt() - 1).clamp(0, field.height - 1)
        val maxX = (minX + maxDistance * 2).clamp(0, field.width - 1)
        val maxY = (minY + maxDistance * 2).clamp(0, field.height - 1)

        var y = minY
        while (y <= maxY) {
            var x = minX
            while (x <= maxX) {
                _reachableCells.add(FieldCell(x, y))
                ++x
            }

            ++y
        }
    }
}