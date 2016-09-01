package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

/**
 * @author octopussy
 */

class Point : Pool.Poolable {

    val position: Vector2 = Vector2()

    override fun reset() {
        position.set(0f, 0f)
    }

    override fun hashCode(): Int = this.position.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is Point) {
            return this.position.equals(other.position)
        } else {
            return false
        }
    }
}

class Segment : Pool.Poolable {
    var x1: Float = 0f
    var y1: Float = 0f
    var x2: Float = 0f
    var y2: Float = 0f

    override fun reset() {
        x1 = 0f; y1 = 0f; x2 = 0f; y2 = 0f
    }
}

class VisibilityComponent(owner: Actor) : ActorComponent(owner) {

    private val FAR_DISTANCE: Float = 1000f

    class DebugGraphics {
        var color: Color = Color.BLUE
        val lines = mutableListOf<Pair<Vector2, Vector2>>()
    }

    var isEnabled: Boolean = true

    var maxDistance = 5.5f

    var showDebug = true

    val resultPoints = Array<Point>()

    val debugInfo = mutableListOf<DebugGraphics>()

    private val visibleSegments = Array<Segment>()

    private val allPoints = hashSetOf<Point>()

    override fun tick(dt: Float) {
        super.tick(dt)

        location.set(owner.location)

        if (!isEnabled) return

        calculateVisibility()
    }

    private fun calculateVisibility() {
        allPoints.forEach { Pools.free(it) }
        allPoints.clear()

        Pools.freeAll(visibleSegments)
        visibleSegments.clear()

        Pools.freeAll(resultPoints)
        resultPoints.clear()

        debugInfo.clear()

        val centerX = location.x
        val centerY = location.y

        addPoint(centerX, centerY)

        val debugWalls = DebugGraphics()
        debugWalls.color = Color.MAGENTA
        debugInfo.add(debugWalls)

        val level = owner.world.level
        //val cells = mutableListOf<Level.Cell>()

        val swX: Float = (Math.round(centerX) - maxDistance)
        val swY: Float = (Math.round(centerY) - maxDistance)
        val neX: Float = (swX + maxDistance * 2)
        val neY: Float = (swY + maxDistance * 2)

        addSegment(swX, swY, neX, swY)
        addSegment(neX, swY, neX, neY)
        addSegment(neX, neY, swX, neY)
        addSegment(swX, neY, swX, swY)

        for (y in swY.toInt()..neY.toInt()) {
            for (x in swX.toInt()..neX.toInt()) {
                val cell = level.getCell(x, y)

                if (cell != null && cell.isWall) {
                    //cells.add(cell)
                    val xx = cell.x.toFloat()
                    val yy = cell.y.toFloat()

                    if (centerY < yy && !checkWall(x, y - 1)) {
                        addSegment(xx, yy, xx + 1f, yy)
                    }

                    if (centerX > xx + 1f && !checkWall(x + 1, y)) {
                        addSegment(xx + 1f, yy, xx + 1f, yy + 1f)
                    }

                    if (centerY > yy + 1f && !checkWall(x, y + 1)) {
                        addSegment(xx + 1f, yy + 1f, xx, yy + 1f)
                    }

                    if (centerX < xx && !checkWall(x - 1, y)) {
                        addSegment(xx, yy + 1f, xx, yy)
                    }
                }
            }
        }

        val tmp: Vector2 = Vector2()
        val tmp2: Vector2 = Vector2()

        fun nearestIntersection(x1: Float, y1: Float, x2: Float, y2: Float, out: Vector2): Boolean {
            var found = false
            var nearestDist = Float.MAX_VALUE

            visibleSegments.forEach {
                if (Intersector.intersectSegments(x1, y1, x2, y2, it.x1, it.y1, it.x2, it.y2, tmp2)) {
                    val dist = Vector2.len(tmp2.x - x1, tmp2.y - y1)
                    if (dist < nearestDist) {
                        nearestDist = dist
                        out.set(tmp2.x, tmp2.y)
                        found = true
                    }
                }
            }

            return found
        }

        val dir = Vector2()
        val farPoint: Vector2 = Vector2()

        allPoints.forEach {
            // trace straight
            dir.set(it.position.x - centerX, it.position.y - centerY).setLength(FAR_DISTANCE)
            farPoint.set(centerX + dir.x, centerY + dir.y)

            val distance = Vector2.len(centerX - it.position.x, centerY - it.position.y)

            val intersectionOccurred = nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
            val nearestDistance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
            if (!intersectionOccurred || nearestDistance + 0.0001f >= distance) {
                addPoint(it.position.x, it.position.y)
            }
        }

        if (showDebug) {
            visibleSegments.forEach { debugWalls.lines.add(Pair(Vector2(it.x1, it.y1), Vector2(it.x2, it.y2))) }
        }
    }

    private fun addSegment(x1: Float, y1: Float, x2: Float, y2: Float) {
        val seg = Pools.obtain(Segment::class.java)
        seg.x1 = x1
        seg.y1 = y1
        seg.x2 = x2
        seg.y2 = y2

        visibleSegments.add(seg)

        var p = Pools.obtain(Point::class.java)
        p.position.set(x1, y1)
        allPoints.add(p)

        p = Pools.obtain(Point::class.java)
        p.position.set(x2, y2)
        allPoints.add(p)
    }

    private fun checkWall(x: Int, y: Int): Boolean {
        val c = owner.world.level.getCell(x, y)
        return c != null && c.isWall
    }

    private fun addPoint(x: Float, y: Float) {
        val p = Pools.obtain(Point::class.java)
        p.position.set(x, y)
        resultPoints.add(p)
    }
}