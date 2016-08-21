package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.borschlabs.xcom.geometry.Poly

/**
 * @author octopussy
 */

interface FieldCell {
    val x: Int
    val y: Int

    val isObstacle: Boolean

    // ai & path finding stuff
    val connections: Array<Connection<FieldCell>>

    fun getFacingWalls(center: Vector3, outWalls: MutableList<Poly.Wall>)

    fun getNeighbour(n: Int): FieldCell?

    companion object {
        val LEFT_NEIGHBOUR = 0
        val TOP_NEIGHBOUR = 1
        val RIGHT_NEIGHBOUR = 2
        val BOTTOM_NEIGHBOUR = 3
    }
}