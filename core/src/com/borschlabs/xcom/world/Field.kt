package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.utils.Array

/**
 * @author octopussy
 */

class Field : IndexedGraph<FieldCell> {
    val width = 30
    val height = 30

    private val cells:List<FieldCell>

    val obstacles:List<FieldCell>

    init {
        val c:MutableList<FieldCell> = mutableListOf()
        var i = 0
        while (i < width * height) {
            c.add(FieldCell(i % width, i / width))
            ++i
        }

        cells = c

        val o:MutableList<FieldCell> = mutableListOf()
        o.add(getNode(10, 10))
        o.add(getNode(10, 11))
        o.add(getNode(11, 11))
        o.add(getNode(12, 11))
        o.add(getNode(10, 12))

        obstacles = o
    }

    fun getNode(x: Int, y: Int): FieldCell {
        val index = y * width + x
        if (index < 0 || index >= width*height){
            throw IllegalArgumentException("getNode() index out of bounds!")
        }

        return cells[index]
    }

    override fun getConnections(fromNode: FieldCell): Array<Connection<FieldCell>> {
        val neighbours = mutableListOf<FieldCell>()
        val connections:Array<Connection<FieldCell>> = Array()
        if (fromNode.x > 0) {
            neighbours.add(getNode(fromNode.x - 1, fromNode.y))
        }

        if (fromNode.x < width - 1) {
            neighbours.add(getNode(fromNode.x + 1, fromNode.y))
        }

        if (fromNode.y > 0) {
            neighbours.add(getNode(fromNode.x, fromNode.y - 1))
        }

        if (fromNode.y < height - 1) {
            neighbours.add(getNode(fromNode.x, fromNode.y + 1))
        }

        for (n in neighbours) {
            if (!obstacles.contains(n)) {
                connections.add(DefaultConnection<FieldCell>(fromNode, n))
            }
        }

        return connections
    }

    override fun getIndex(node: FieldCell): Int = node.y * width + node.x

    override fun getNodeCount(): Int = width * height
}