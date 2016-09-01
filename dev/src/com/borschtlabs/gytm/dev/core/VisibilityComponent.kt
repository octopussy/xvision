package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

/**
 * @author octopussy
 */

class VisMapPoint : Pool.Poolable {

    val position: Vector2 = Vector2()

    override fun reset() {
        position.set(0f, 0f)
    }

    override fun hashCode(): Int = this.position.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return  false
        if (other is VisMapPoint) {
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

    class DebugGraphics {
        var color: Color = Color.BLUE
        val lines = mutableListOf<Pair<Vector2, Vector2>>()
    }

    var isEnabled: Boolean = true

    var maxDistance = 5f

    var showDebug = true

    val points = Array<VisMapPoint>()

    val debugInfo = mutableListOf<DebugGraphics>()

    private val visibleSegments = Array<Segment>()

    private val visiblePoints = hashSetOf<VisMapPoint>()

    override fun tick(dt: Float) {
        super.tick(dt)

        location.set(owner.location)

        if (!isEnabled) return

        calculateVisibility()
    }

    private fun calculateVisibility() {
        visiblePoints.forEach { Pools.free(it) }
        Pools.freeAll(visibleSegments)

        points.clear()
        visibleSegments.clear()
        visiblePoints.clear()

        debugInfo.clear()

        addPoint(location.x, location.y)

        val debugWalls = DebugGraphics()
        debugWalls.color = Color.MAGENTA
        debugInfo.add(debugWalls)

        val level = owner.world.level
        //val cells = mutableListOf<Level.Cell>()

        val swX:Int = (Math.round(location.x) - maxDistance).toInt()
        val swY:Int = (Math.round(location.y) - maxDistance).toInt()
        val neX:Int = (swX + maxDistance * 2).toInt()
        val neY:Int = (swY + maxDistance * 2).toInt()

        addSegment(swX.toFloat(), swY.toFloat(), neX.toFloat(), swY.toFloat())
        addSegment(neX.toFloat(), swY.toFloat(), neX.toFloat(), neY.toFloat())
        addSegment(neX.toFloat(), neY.toFloat(), swX.toFloat(), neY.toFloat())
        addSegment(swX.toFloat(), neY.toFloat(), swX.toFloat(), swY.toFloat())

        for (y in swY..neY - 1) {
            for (x in swX..neX - 1) {
                val cell = level.getCell(x, y)

                if (cell != null && cell.isWall) {
                    //cells.add(cell)
                    val xx = cell.x.toFloat()
                    val yy = cell.y.toFloat()

                    if (location.y < yy && !checkWall(x, y - 1)) {
                        addSegment(xx, yy, xx + 1f, yy)
                    }

                    if (location.x > xx + 1f && !checkWall(x + 1, y)) {
                        addSegment(xx + 1f, yy, xx + 1f, yy + 1f)
                    }

                    if (location.y > yy + 1f && !checkWall(x, y + 1)) {
                        addSegment(xx + 1f, yy + 1f, xx, yy + 1f)
                    }

                    if (location.x < xx && !checkWall(x - 1, y)) {
                        addSegment(xx, yy + 1f, xx, yy)
                    }
                }
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

        var p = Pools.obtain(VisMapPoint::class.java)
        p.position.set(x1, y1)
        visiblePoints.add(p)

        p = Pools.obtain(VisMapPoint::class.java)
        p.position.set(x2, y2)
        visiblePoints.add(p)
    }

    private fun checkWall(x: Int, y: Int): Boolean {
        val c = owner.world.level.getCell(x, y)
        return c != null && c.isWall
    }

    private fun addPoint(x: Float, y: Float) {
        val p = Pools.obtain(VisMapPoint::class.java)
        p.position.set(x, y)
        points.add(p)
    }
}