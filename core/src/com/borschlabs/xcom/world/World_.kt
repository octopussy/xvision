package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.utils.Array

/**
 * @author octopussy
 */

class World_(tiledMap:TiledMap) : IndexedGraph<FieldCell> {

    private var cells:List<FieldCell> = mutableListOf()
    private var collisionLayer: TiledMapTileLayer

    val width:Int get() = collisionLayer.width
    val height:Int get() = collisionLayer.height

    val cellSize:Float get() = collisionLayer.tileWidth

    val units:MutableList<GameUnit> = mutableListOf()
    val obstacles:MutableList<FieldCell> = mutableListOf()

    init {
        collisionLayer = tiledMap.layers.get("collision") as TiledMapTileLayer

        createCells()
    }

    fun getCell(x: Int, y: Int): FieldCell {
        val index = y * width + x
        if (index < 0 || index >= width*height){
            throw IllegalArgumentException("getCell() index out of bounds!")
        }

        return cells[index]
    }

    override fun getConnections(fromNode: FieldCell): Array<Connection<FieldCell>> {
        val neighbours = mutableListOf<FieldCell>()
        val connections:Array<Connection<FieldCell>> = Array()
        if (fromNode.x > 0) {
            neighbours.add(getCell(fromNode.x - 1, fromNode.y))
        }

        if (fromNode.x < width - 1) {
            neighbours.add(getCell(fromNode.x + 1, fromNode.y))
        }

        if (fromNode.y > 0) {
            neighbours.add(getCell(fromNode.x, fromNode.y - 1))
        }

        if (fromNode.y < height - 1) {
            neighbours.add(getCell(fromNode.x, fromNode.y + 1))
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

    private fun createCells() {
        val c = mutableListOf<FieldCell>()
        var i = 0
        while (i < nodeCount) {
            val x = i % width
            val y = i / width
            val cell = DefaultCell(x, y)
            c.add(cell)
            ++i

            if (collisionLayer.getCell(x, y) != null) {
                obstacles.add(cell)
            }
        }

        cells = c
    }

    private data class DefaultCell(override val x: Int, override val y: Int) : FieldCell
}