package com.borschlabs.xcom.geometry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

import java.util.*;

/**
 * @author octopussy
 */
public class VisibleMapBuilder {
   private static final float EPSILON = 0.01f;

   private final ShapeRenderer sr;

   public VisibleMapBuilder(ShapeRenderer sr) {
      this.sr = sr;
   }

   public void build(final Vector2 center, List<Poly.Wall> inputGeometry, Poly bounds, List<Vector2> outputPoints) {
      outputPoints.clear();

      List<Vector2> corners = new ArrayList<Vector2>();
      //List<Poly.Wall> walls = new ArrayList<Poly.Wall>();

      for (Poly.Wall w : inputGeometry) {
         corners.addAll(Arrays.asList(w.corners));
      }

      /*for (Poly.Wall w : bounds.walls) {
         corners.add(w.corners[0]);
         corners.add(w.corners[1]);
      }*/

      Collections.sort(corners, new Comparator<Vector2>() {
         @Override
         public int compare(Vector2 l, Vector2 r) {
            Vector2 ll = new Vector2(l);
            ll.sub(center);

            Vector2 rr = new Vector2(r);
            rr.sub(center);
            float a1 = ll.angleRad(Vector2.X);
            float a2 = rr.angleRad(Vector2.X);
            if (a1 == a2) return 0;
            return a1 > a2 ? 1 : -1;
         }
      });

      sr.begin(ShapeRenderer.ShapeType.Line);

      Vector2 prevCorner = new Vector2(Float.MAX_VALUE, Float.MAX_VALUE);

      for (Vector2 corner : corners) {
         if (corner.epsilonEquals(prevCorner, EPSILON)) {
            continue;
         }

         prevCorner.set(corner);

         final float distanceToCorner2 = Vector2.len2(corner.x - center.x, corner.y - center.y);
         Vector2 dir = new Vector2(corner);
         dir.sub(center).nor();
         Vector2 farTracePoint = new Vector2(dir);
         farTracePoint.scl(10000f).add(center);

         //sr.setColor(Color.RED);
         // sr.line(center, corner);
         boolean isInvisibleCorner = false;
         for (Poly.Wall w : inputGeometry) {
            Vector2 out = new Vector2();
            Vector2 wallDir = new Vector2(w.corners[1].x - w.corners[0].x, w.corners[1].y - w.corners[0].y);
            wallDir.nor().scl(0.1f);
            wallDir.set(0, 0);
            if (Intersector.intersectSegments(center, farTracePoint, w.corners[0].cpy().sub(wallDir),
               w.corners[1].cpy().add(wallDir), out)) {
               float dist = Vector2.len2(out.x - center.x, out.y - center.y);
               boolean sameCorner = corner.epsilonEquals(out, EPSILON);
               if (dist < distanceToCorner2 && !sameCorner) {
                  isInvisibleCorner = true;
               	//sr.setColor(Color.MAGENTA);
						//sr.line(center, corner);
                  break;
               }
            }
         }

         if (!isInvisibleCorner) {
            List<Vector2> neighbours = new ArrayList<Vector2>();
            for (Poly.Wall w : inputGeometry) {
               if (w.corners[0].epsilonEquals(corner, EPSILON)) {
                  neighbours.add(w.corners[1]);
               } else if (w.corners[1].epsilonEquals(corner, EPSILON)) {
                  neighbours.add(w.corners[0]);
               }
            }

            //boolean t = traceFurther;
            boolean traceFurther = true;
            float side = 0;
            for (int i = 0; i < neighbours.size(); ++i) {
               Vector2 n = neighbours.get(i);
               Vector2 v = new Vector2(n);
               v.sub(corner).nor();
               //sr.setColor(Color.CYAN);
               //sr.line(corner.pos, new Vector2(corner.pos).add(new Vector2(v).scl(10)));
               float s = dir.angleRad(v);

               if (i > 0 && Math.signum(s) != Math.signum(side)) {
                  traceFurther = false;
                  break;
               }
               side = s;
            }
            if (!traceFurther) {
               //sr.setColor(Color.BLUE);
               outputPoints.add(corner);
            } else {
               if (side > 0) { // wall on the right
                  outputPoints.add(corner);
                  //sr.setColor(Color.BLUE);
               }

               float distance = Float.MAX_VALUE;
               Vector2 nearest = null;
               for (Poly.Wall w : inputGeometry) {
                  Vector2 out = new Vector2();
                  if (Intersector.intersectSegments(center, farTracePoint, w.corners[0], w.corners[1], out)) {
                     float dist = Vector2.len2(out.x - corner.x, out.y - corner.y);
                     if (dist < distance && !out.epsilonEquals(corner, EPSILON)) {
                        distance = dist;
                        nearest = out;
                     }
                  }
               }

               if (nearest != null) {
                  outputPoints.add(nearest);
                  //sr.setColor(Color.YELLOW);
                  //sr.line(center.x, center.y, nearest.x, nearest.y);
               }

               //outputPoints.add(corner);
               sr.setColor(Color.BLUE);
               //sr.line(center, corner);
               if (side < 0) {
                  outputPoints.add(corner);
                  //sr.setColor(Color.BLUE);
                 // sr.line(center, corner);
               }
            }
         } else {
         //   sr.setColor(Color.CYAN);
            //sr.line(center, corner);
         }
      }

      sr.end();

      if (!outputPoints.isEmpty()) {
         outputPoints.add(new Vector2(outputPoints.get(0)));
      }
   }

   private static class WallChainBuilder {
      public void newChain() {

      }
   }
}
