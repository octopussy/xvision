package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.utils.Array

/**
 * @author octopussy
 */

class Field : IndexedGraph<FieldCell> {
    val width = 20
    val height = 20

    private val cells:List<FieldCell>

    init {
        val c:MutableList<FieldCell> = mutableListOf()
        var i = 0
        while (i < width * height) {
            c.add(FieldCell(i % width, i / width))
            ++i
        }

        cells = c
    }

    fun getNode(x: Int, y: Int): FieldCell? {
        val index = y * width + x
        if (index < 0 || index >= width*height){
            throw IllegalArgumentException("getNode() index out of bounds!")
        }

        return cells[index]
    }

    override fun getConnections(fromNode: FieldCell): Array<Connection<FieldCell>> {
        val connections:Array<Connection<FieldCell>> = Array()
        if (fromNode.x > 0) {
            connections.add(DefaultConnection<FieldCell>(fromNode, getNode(fromNode.x - 1, fromNode.y)))
        }

        if (fromNode.x < width - 1) {
            connections.add(DefaultConnection<FieldCell>(fromNode, getNode(fromNode.x + 1, fromNode.y)))
        }

        if (fromNode.y > 0) {
            connections.add(DefaultConnection<FieldCell>(fromNode, getNode(fromNode.x, fromNode.y - 1)))
        }

        if (fromNode.y < height - 1) {
            connections.add(DefaultConnection<FieldCell>(fromNode, getNode(fromNode.x, fromNode.y + 1)))
        }


        return connections
    }

    override fun getIndex(node: FieldCell): Int = node.y * width + node.x

    override fun getNodeCount(): Int = width * height
}