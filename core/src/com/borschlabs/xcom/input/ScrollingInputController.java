package com.borschlabs.xcom.input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * @author octopussy
 */
public class ScrollingInputController extends GestureDetector {

   private static final float MIN_ZOOM = 0.15f;
   private static final float MAX_ZOOM = 5f;

   private final OrthographicCamera camera;

   public ScrollingInputController(OrthographicCamera camera) {
      super(new GListener(camera));
      this.camera = camera;
   }

   @Override
   public boolean scrolled(int amount) {
      camera.zoom += amount * 0.1f;

      camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);
      return false;
   }

   private static class GListener extends GestureDetector.GestureAdapter {
      private final OrthographicCamera camera;

      private float startZoom;

      GListener(OrthographicCamera camera) {
         this.camera = camera;
      }

      @Override
      public boolean touchDown(float x, float y, int pointer, int button) {
         if (pointer == 1) {
            startZoom = camera.zoom;
         }
         return false;
      }

      @Override
      public boolean pan(float x, float y, float deltaX, float deltaY) {
         Vector3 centerScr = new Vector3();
         camera.translate(camera.unproject(new Vector3().add(-deltaX, -deltaY, 0)).sub(camera.unproject(centerScr)));
         return true;
      }

      @Override
      public boolean panStop(float x, float y, int pointer, int button) {
         return false;
      }

      @Override
      public boolean zoom(float initialDistance, float distance) {
         camera.zoom = startZoom * initialDistance / distance;

         camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);
         return true;
      }

      @Override
      public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
         return false;
      }
   }
}
