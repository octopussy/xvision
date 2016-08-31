package com.borschtlabs.gytm.dev

import com.badlogic.gdx.math.Vector2

/**
 * @author octopussy
 */

class PathInterpolator {

    private val points: MutableList<Vector2> = mutableListOf()

    private val segments: MutableList<Float> = mutableListOf()

    private var length: Float = 0.0f

    fun addPoint(x: Float, y: Float) {
        points.add(Vector2(x, y))
        if (points.size > 1) {
            val i = points.lastIndex
            val seg = Vector2.len(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y)
            segments.add(seg)
            length += seg
        }
    }

    fun addPoint(v: Vector2) {
        addPoint(v.x, v.y)
    }

    fun interpolate(distancePassed: Double, out: Vector2): Boolean {
        if ((distancePassed < 0 || distancePassed >= length ) && points.size > 0) {
            out.set(points.last())
            return false
        }

        var i = 0
        var dist = 0.0f
        while (dist < distancePassed) {
            dist += segments[i]
            ++i
        }

        val p1 = points[i - 1].cpy()
        val p2 = points[i]

        val distToLastPoint = dist - segments[i - 1]
        val alpha = (distancePassed - distToLastPoint) / segments[i - 1]
        out.set(p1.lerp(p2, alpha.toFloat()))

        return true
    }

}