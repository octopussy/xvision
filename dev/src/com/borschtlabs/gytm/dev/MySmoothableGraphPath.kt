package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath
import com.badlogic.gdx.math.Vector2

/**
 * @author octopussy
 */

class MySmoothableGraphPath() : DefaultGraphPath<TurnArea.WayPoint>(), SmoothableGraphPath<TurnArea.WayPoint, Vector2> {

    override fun truncatePath(newLength: Int) {
        nodes.truncate(newLength)
    }

    override fun getNodePosition(index: Int): Vector2 = nodes[index].center

    override fun swapNodes(i1: Int, i2: Int) {
        val t = nodes[i1]
        nodes[i1] = nodes[i2]
        nodes[i2] = t
    }

}