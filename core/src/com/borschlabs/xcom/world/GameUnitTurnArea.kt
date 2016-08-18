package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.GraphPath
import com.badlogic.gdx.ai.pfa.Heuristic
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.borschlabs.xcom.clamp

/**
 * @author octopussy
 */

class GameUnitTurnArea(val world: World) {
    private var _reachableCells: MutableSet<FieldCell> = mutableSetOf()

    val reachableCells: List<FieldCell>
        get() = _reachableCells.toList()

    fun calculateArea(startCell:FieldCell, maxDistance: Int) {
        _reachableCells = mutableSetOf()
        _reachableCells.add(startCell)

        val minX = (startCell.x - maxDistance).clamp(0, world.width - 1)
        val minY = (startCell.y - maxDistance).clamp(0, world.height - 1)
        val maxX = (startCell.x + maxDistance).clamp(0, world.width - 1)
        val maxY = (startCell.y + maxDistance).clamp(0, world.height - 1)

        var y = minY
        while (y <= maxY) {
            var x = minX
            while (x <= maxX) {
                val c = world.getCell(x, y)
                if (!_reachableCells.contains(c)){
                    traceRoute(startCell.x, startCell.y, x, y, maxDistance)
                }

                ++x
            }

            ++y
        }
    }

    private fun traceRoute(startX: Int, startY: Int, targetX: Int, targetY: Int, maxDistance: Int) {
        val finder = IndexedAStarPathFinder<FieldCell>(world)
        val path:GraphPath<FieldCell> = DefaultGraphPath<FieldCell>()
        finder.searchNodePath(world.getCell(startX, startY), world.getCell(targetX, targetY), FieldHeuristic(), path)

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