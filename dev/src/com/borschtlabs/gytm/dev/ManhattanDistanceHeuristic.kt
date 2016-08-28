package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.pfa.Heuristic

/**
 * @author octopussy
 */
class ManhattanDistanceHeuristic : Heuristic<TurnArea.WayPoint> {

    override fun estimate(node: TurnArea.WayPoint, endNode: TurnArea.WayPoint): Float {
        val xx = (endNode.x - node.x).toDouble()
        val yy = (endNode.y - node.y).toDouble()
        val i = Math.abs(xx) + Math.abs(yy)
        //val i = Math.sqrt(xx * xx + yy * yy)

        return i.toFloat()
    }
}
