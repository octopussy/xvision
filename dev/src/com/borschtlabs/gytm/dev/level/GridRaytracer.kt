package com.borschtlabs.gytm.dev.level

/**
 * @author octopussy
 */

object GridRaytracer {

    fun trace(x0: Double, y0: Double, x1: Double, y1: Double, visit: (x: Int, y: Int) -> Boolean) {
        val dx = Math.abs(x1 - x0)
        val dy = Math.abs(y1 - y0)

        var x = Math.floor(x0).toInt();
        var y = Math.floor(y0).toInt();

        var n: Int = 1
        val x_inc: Int
        val y_inc: Int
        var error: Double

        if (dx == 0.0) {
            x_inc = 0;
            error = Double.MAX_VALUE
        } else if (x1 > x0) {
            x_inc = 1;
            n += (Math.floor(x1)).toInt() - x;
            error = (Math.floor(x0) + 1 - x0) * dy;
        } else {
            x_inc = -1;
            n += x - (Math.floor(x1)).toInt();
            error = (x0 - Math.floor(x0)) * dy;
        }

        if (dy == 0.0) {
            y_inc = 0;
            error -= Double.MAX_VALUE
        } else if (y1 > y0) {
            y_inc = 1;
            n += Math.floor(y1).toInt() - y;
            error -= (Math.floor(y0) + 1 - y0) * dx;
        } else {
            y_inc = -1;
            n += y - Math.floor(y1).toInt();
            error -= (y0 - Math.floor(y0)) * dx;
        }

        while (n > 0)
        {
            if (visit(x, y)) {
                return
            }

            if (error > 0) {
                y += y_inc;
                error -= dx;
            } else {
                x += x_inc;
                error += dy;
            }

            --n
        }
    }
}