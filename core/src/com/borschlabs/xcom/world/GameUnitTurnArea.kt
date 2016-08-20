package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.GraphPath
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.borschlabs.xcom.clamp

/**
 * @author octopussy
 */

class GameUnitTurnArea(val field: Field) {
    private var _reachableCells: MutableSet<FieldCell> = mutableSetOf()

    val reachableCells: List<FieldCell>
        get() = _reachableCells.toList()

    fun calculateArea(startCell:FieldCell, maxDistance: Int) {
        _reachableCells = mutableSetOf()
        _reachableCells.add(startCell)

        val minX = (startCell.x - maxDistance).clamp(0, field.width - 1)
        val minY = (startCell.y - maxDistance).clamp(0, field.height - 1)
        val maxX = (startCell.x + maxDistance).clamp(0, field.width - 1)
        val maxY = (startCell.y + maxDistance).clamp(0, field.height - 1)

        var y = minY
        while (y <= maxY) {
            var x = minX
            while (x <= maxX) {
                val c = field.getCell(x, y)
                if (!_reachableCells.contains(c)){
                    traceRoute(startCell.x, startCell.y, x, y, maxDistance)
                }

                ++x
            }

            ++y
        }
    }

    fun getRoute(startCell: FieldCell, targetCell: FieldCell): List<FieldCell> {
        val finder = IndexedAStarPathFinder<FieldCell>(field)
        val path:GraphPath<FieldCell> = DefaultGraphPath<FieldCell>()
        finder.searchNodePath(startCell, targetCell, FieldHeuristic(), path)

        val result: MutableList<FieldCell> = mutableListOf()

        path.forEach { result.add(it) }
        return result
    }

    private fun traceRoute(startX: Int, startY: Int, targetX: Int, targetY: Int, maxDistance: Int) {
        val finder = IndexedAStarPathFinder<FieldCell>(field)
        val path:GraphPath<FieldCell> = DefaultGraphPath<FieldCell>()
        finder.searchNodePath(field.getCell(startX, startY), field.getCell(targetX, targetY), FieldHeuristic(), path)

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