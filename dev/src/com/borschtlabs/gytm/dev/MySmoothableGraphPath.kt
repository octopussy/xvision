package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.pfa.DefaultGraphPath
import com.badlogic.gdx.math.Vector2

/**
 * @author octopussy
 */

class MySmoothableGraphPath() : DefaultGraphPath<TurnArea.WayPoint>() {

    fun truncatePath(newLength: Int) {
        nodes.truncate(newLength)
    }

    fun getNodePosition(index: Int): Vector2 = nodes[index].center

    fun swapNodes(i1: Int, i2: Int) {
        val t = nodes[i1]
        nodes[i1] = nodes[i2]
        nodes[i2] = t
    }

}