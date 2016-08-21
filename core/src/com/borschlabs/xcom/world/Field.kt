package com.borschlabs.xcom.world

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntMap
import com.borschlabs.xcom.geometry.Poly

/**
 * @author octopussy
 */

class Field(tiledMap: TiledMap) : IndexedGraph<FieldCell> {

    private var cells: List<DefaultCell> = mutableListOf()
    private var collisionLayer: TiledMapTileLayer

    val width: Int get() = collisionLayer.width
    val height: Int get() = collisionLayer.height

    val obstacles: MutableList<FieldCell> = mutableListOf()

    init {
        collisionLayer = tiledMap.layers.get("collision") as TiledMapTileLayer
        createCells()
    }

    fun getCell(x: Int, y: Int): FieldCell? {
        val index = y * width + x
        if (index < 0 || index >= width * height) {
            return null
        }

        return cells[index]
    }

    fun getCellAtWorldPoint(x: Float, y: Float): FieldCell?
            = getCell(Math.floor(x.toDouble()).toInt(), Math.floor(y.toDouble()).toInt())

    override fun getConnections(fromNode: FieldCell): Array<Connection<FieldCell>> {
        return fromNode.connections
    }

    override fun getIndex(node: FieldCell): Int = node.y * width + node.x

    override fun getNodeCount(): Int = width * height

    fun getVisibleWalls(center: Vector3, maxDistance: Float, walls: MutableList<Poly.Wall>) {
        val leftX = center.x - maxDistance
        val rightX = center.x + maxDistance
        val topY = center.y + maxDistance
        val bottomY = center.y - maxDistance

        for (o in obstacles) {
            if (o.x + 1.0f < leftX || o.y > topY || o.x > rightX || o.y + 1.0f < bottomY)
                continue

            o.getFacingWalls(center, walls)
        }
    }


    private fun createCells() {
        val c = mutableListOf<DefaultCell>()
        var i = 0
        while (i < nodeCount) {
            val x = i % width
            val y = i / width

            val cell = DefaultCell(x, y, this)
            c.add(cell)
            ++i

            if (collisionLayer.getCell(x, y) != null) {
                obstacles.add(cell)
                cell.isObstacle = true
            }
        }

        cells = c

        c.forEach {
            if (it.x > 0) {
                it.neighbours.put(FieldCell.LEFT_NEIGHBOUR, getCell(it.x - 1, it.y))
            }

            if (it.x < width - 1) {
                it.neighbours.put(FieldCell.RIGHT_NEIGHBOUR, getCell(it.x + 1, it.y))
            }

            if (it.y > 0) {
                it.neighbours.put(FieldCell.BOTTOM_NEIGHBOUR, getCell(it.x, it.y - 1))
            }

            if (it.y < height - 1) {
                it.neighbours.put(FieldCell.TOP_NEIGHBOUR, getCell(it.x, it.y + 1))
            }

            for (n in it.neighbours.values()) {
                if (!obstacles.contains(n)) {
                    it.connections.add(DefaultConnection<FieldCell>(it, n))
                }
            }
        }
    }

    private data class DefaultCell(override val x: Int, override val y: Int, val field: Field) : FieldCell {

        override var isObstacle: Boolean = false

        override val connections: Array<Connection<FieldCell>> = Array()

        private val edges: MutableList<Poly.Wall> = mutableListOf()

        val neighbours = IntMap<FieldCell>()

        init {
            val posX = x.toFloat()
            val posY = y.toFloat()
            val size = 1.0f

            val bl = Vector2(posX, posY)
            val tl = Vector2(posX, posY + size)
            val tr = Vector2(posX + size, posY + size)
            val br = Vector2(posX + size, posY)
            edges.add(Poly.Wall(bl, tl)) // left
            edges.add(Poly.Wall(tl, tr)) // top
            edges.add(Poly.Wall(tr, br)) // right
            edges.add(Poly.Wall(br, bl)) // bottom
        }

        override fun getNeighbour(n: Int): FieldCell? = neighbours.get(n)

        override fun getFacingWalls(center: Vector3, outWalls: MutableList<Poly.Wall>) {
            val checkIfObstacle = fun(n: Int): Boolean {
                val c = getNeighbour(n)
                if (c != null) {
                    return c.isObstacle
                } else {
                    return false
                }
            }

            if (edges[0].corners[0].x > center.x && !checkIfObstacle(FieldCell.LEFT_NEIGHBOUR)) { // check left facing
                outWalls.add(edges[0])
            }

            if (edges[1].corners[0].y < center.y && !checkIfObstacle(FieldCell.TOP_NEIGHBOUR)) { // check top facing
                outWalls.add(edges[1])
            }

            if (edges[2].corners[0].x < center.x && !checkIfObstacle(FieldCell.RIGHT_NEIGHBOUR)) { // check right facing
                outWalls.add(edges[2])
            }

            if (edges[3].corners[0].y > center.y && !checkIfObstacle(FieldCell.BOTTOM_NEIGHBOUR)) { // check bottom facing
                outWalls.add(edges[3])
            }

        }
    }
}