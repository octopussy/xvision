package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.GraphPath
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
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

        val minX = (startX - maxDistance).clamp(0, field.width - 1)
        val minY = (startY - maxDistance).clamp(0, field.height - 1)
        val maxX = (startX + maxDistance).clamp(0, field.width - 1)
        val maxY = (startY + maxDistance).clamp(0, field.height - 1)

        var y = minY
        while (y <= maxY) {
            var x = minX
            while (x <= maxX) {
                val c = field.getNode(x, y)
                if (!_reachableCells.contains(c)){
                    traceRoute(startX, startY, x, y, maxDistance)
                }

                ++x
            }

            ++y
        }
    }

    private fun traceRoute(startX: Int, startY: Int, targetX: Int, targetY: Int, maxDistance: Int) {
        val finder = IndexedAStarPathFinder<FieldCell>(field)
        val path:GraphPath<FieldCell> = DefaultGraphPath<FieldCell>()
        finder.searchNodePath(field.getNode(startX, startY), field.getNode(targetX, targetY), FieldHeuristic(), path)

        var depth = maxDistance + 1
        for (c in path) {
            _reachableCells.add(c)
            --depth
            if (depth == 0) break
        }
    }

    private class FieldHeuristic : Heuristic<FieldCell>{
        override fun estimate(node: FieldCell?, endNode: FieldCell?): Float {
            return 1.0f
        }
    }
}