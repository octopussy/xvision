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

    var angleFromCenter: Float = 0f

    override fun reset() {
        position.set(0f, 0f)
        angleFromCenter = 0f
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

class VisibilityComponent(owner: Actor) : ActorComponent(owner) {

    private val FAR_DISTANCE = 1000f

    private val DISTANCE_EPSILON = 0.001f

    private val ANGLE_DEVIATION = 0.000001f

    class DebugGraphics {
        var color: Color = Color.BLUE
        val lines = mutableListOf<Pair<Vector2, Vector2>>()
    }

    var isEnabled: Boolean = true

    var maxDistance = 100.0f

    var showDebug = true

    val resultPoints = Array<Point>()

    val debugInfo = mutableListOf<DebugGraphics>()

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

        Pools.freeAll(resultPoints)
        resultPoints.clear()

        debugInfo.clear()

        val centerX = location.x
        val centerY = location.y

        val level = owner.world.level

        val swX: Float = (Math.round(centerX) - maxDistance)
        val swY: Float = (Math.round(centerY) - maxDistance)
        val neX: Float = (swX + maxDistance * 2)
        val neY: Float = (swY + maxDistance * 2)

        val tmp: Vector2 = Vector2()

        addPointToAll(swX, swY)
        addPointToAll(neX, swY)
        addPointToAll(neX, neY)
        addPointToAll(swX, neY)

        for (y in swY.toInt()..neY.toInt()) {
            for (x in swX.toInt()..neX.toInt()) {
                val cell = level.getCell(x, y)

                if (cell != null && cell.isWall) {
                    val xx = cell.x.toFloat()
                    val yy = cell.y.toFloat()

                    if (centerY < yy && !checkWall(x, y - 1)) {
                        addPointToAll(xx, yy)
                        addPointToAll(xx + 1f, yy)
                    }

                    if (centerX > xx + 1f && !checkWall(x + 1, y)) {
                        addPointToAll(xx + 1f, yy)
                        addPointToAll(xx + 1f, yy + 1f)
                    }

                    if (centerY > yy + 1f && !checkWall(x, y + 1)) {
                        addPointToAll(xx + 1f, yy + 1f)
                        addPointToAll(xx, yy + 1f)
                    }

                    if (centerX < xx && !checkWall(x - 1, y)) {
                        addPointToAll(xx, yy + 1f)
                        addPointToAll(xx, yy)
                    }
                }
            }
        }

        val dir = Vector2()
        val farPoint: Vector2 = Vector2()

        fun nearestIntersection(centerX: Float, centerY: Float, toX: Float, toY: Float, out: Vector2) {
            if (level.nearestIntersection(centerX, centerY, toX, toY, out)) {
                return
            }

            if (Intersector.intersectSegments(centerX, centerY, toX, toY, swX, swY, swX, neY, out)){
                return
            }

            if (Intersector.intersectSegments(centerX, centerY, toX, toY, swX, neY, neX, neY, out)) {
                return
            }

            if (Intersector.intersectSegments(centerX, centerY, toX, toY, neX, neY, neX, swY, out)) {
                return
            }

            if (Intersector.intersectSegments(centerX, centerY, toX, toY, neX, swY, swX, swY, out)) {
                return
            }
        }

        allPoints.forEach {
            // trace straight
            dir.set(it.position.x - centerX, it.position.y - centerY).setLength(FAR_DISTANCE)
            farPoint.set(centerX + dir.x, centerY + dir.y)

            var mainPointAdded = false
            val distanceToMainPoint = Vector2.len(centerX - it.position.x, centerY - it.position.y)
            nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
            val nearestDistance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
            if (nearestDistance + DISTANCE_EPSILON >= distanceToMainPoint) {
                addPoint(it.position.x, it.position.y, dir.angleRad())
                mainPointAdded = true
            }


            if (mainPointAdded) {
                // trace left -0.0001 rad

                dir.set(it.position.x - centerX, it.position.y - centerY).rotateRad(ANGLE_DEVIATION).setLength(FAR_DISTANCE)
                farPoint.set(centerX + dir.x, centerY + dir.y)

                nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
                var distance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
                if (distance + DISTANCE_EPSILON >= distanceToMainPoint) {
                    addPoint(tmp.x, tmp.y, dir.angleRad())
                }


                // trace right +0.0001 rad
                dir.set(it.position.x - centerX, it.position.y - centerY).rotateRad(-ANGLE_DEVIATION).setLength(FAR_DISTANCE)
                farPoint.set(centerX + dir.x, centerY + dir.y)

                nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
                distance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
                if (distance + DISTANCE_EPSILON >= distanceToMainPoint) {
                    addPoint(tmp.x, tmp.y, dir.angleRad())
                }
            }

        }

        resultPoints.sort { p1, p2 ->
            when {
                p1.angleFromCenter < p2.angleFromCenter -> 1
                p1.angleFromCenter > p2.angleFromCenter -> -1
                else -> 0
            }
        }

        addPoint(resultPoints[0].position.x, resultPoints[0].position.y, resultPoints[0].angleFromCenter)
    }

    private fun addPointToAll(x: Float, y: Float) {
        val p = Pools.obtain(Point::class.java)
        p.position.set(x, y)
        allPoints.add(p)
    }

    private fun checkWall(x: Int, y: Int): Boolean {
        val c = owner.world.level.getCell(x, y)
        return c != null && c.isWall
    }

    private fun addPoint(x: Float, y: Float, angle: Float) {
        val p = Pools.obtain(Point::class.java)
        p.position.set(x, y)
        p.angleFromCenter = angle
        resultPoints.add(p)
    }
}