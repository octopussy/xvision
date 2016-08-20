package com.borschlabs.xcom.geometry;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * @author octopussy
 */
public class GeomUtils {
	private static final float EPSILON = 0.001f;

	public static boolean wallFrontFacing(Vector3 center, Poly.Wall w) {
		Vector2 dir = new Vector2((w.corners[1].x + w.corners[0].x) / 2f - center.x, (w.corners[1].y + w.corners[0].y) / 2f - center.y);
		Vector2 leftNormal = new Vector2(-(w.corners[1].y - w.corners[0].y), (w.corners[1].x - w.corners[0].x));
		return dir.dot(leftNormal) < 0;
	}

	public static boolean wallFrontFacing(Vector2 center, float c0x, float c0y, float c1x, float c1y) {
		Vector2 dir = new Vector2((c1x + c0x) / 2f - center.x, (c1y + c0y) / 2f - center.y);
		Vector2 leftNormal = new Vector2(-(c1y - c0y), (c1x - c0x));
		return dir.dot(leftNormal) < 0;
	}

	public static boolean isOnLine(Vector2 v1, Vector2 v2) {
		return v1.isOnLine(v2, EPSILON);
	}
}
