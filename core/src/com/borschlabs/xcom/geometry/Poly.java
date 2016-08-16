package com.borschlabs.xcom.geometry;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author octopussy
 */
public class Poly {
	//private final List<Corner> corners = new ArrayList<Corner>();
	public final List<Wall> walls = new ArrayList<Wall>();

	public Poly(Vector2... list) {
		/*for (int i = 0; i < list.length; ++i) {
			Corner c = new Corner(this, list[i]);
			if (i > 0) {
				Wall w = new Wall(corners.get(i - 1), c);
				walls.add(w);
			}

			corners.add(c);
		}

		walls.add(new Wall(corners.get(corners.size() - 1), corners.get(0)));*/
	}

	public void draw(ShapeRenderer sr) {
		/*for (Wall w : walls) {
			sr.line(w.corners[0].pos, w.corners[1].pos);
		}*/
	}

	/*public List<Corner> getCorners() {
		return corners;
	}*/

	public boolean intersect(Vector2 v1, Vector2 v2, Vector2 out) {
		/*float dist = Float.MAX_VALUE;
		Vector2 nearest = null;
		for (Wall w : walls) {
			Vector2 i = new Vector2();
			if (Intersector.intersectSegments(w.corners[0].pos, w.corners[1].pos, v1, v2, i)) {
				float d = Vector2.len(i.x - v1.x, i.y - v1.y);
				if (d < dist) {
					dist = d;
					nearest = i;
				}
			}
		}

		if (nearest != null) {
			out.set(nearest);
			return true;
		}*/
		return false;
	}

	public static class Wall {
		public final Vector2[] corners = new Vector2[2];

		public Wall(Vector2 c1, Vector2 c2) {
			corners[0] = c1;
			corners[1] = c2;
		}
	}
}
