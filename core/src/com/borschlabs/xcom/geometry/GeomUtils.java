package com.borschlabs.xcom.geometry;

import com.badlogic.gdx.math.Vector2;

/**
 * @author octopussy
 */
public class GeomUtils {
	private static final float EPSILON = 0.001f;

	public static boolean wallFrontFacing(Vector2 center, Poly.Wall w) {
		Vector2 dir = new Vector2((w.corners[1].x + w.corners[0].x) / 2f - center.x, (w.corners[1].y + w.corners[0].y) / 2f - center.y);
		Vector2 leftNormal = new Vector2(-(w.corners[1].y - w.corners[0].y), (w.corners[1].x - w.corners[0].x));
		return dir.dot(leftNormal) < 0;
	}

	public static boolean isOnLine(Vector2 v1, Vector2 v2) {
		return v1.isOnLine(v2, EPSILON);
	}
}
