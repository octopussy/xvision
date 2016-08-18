package com.borschlabs.xcom.input;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenAccessor;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Quad;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * @author octopussy
 */
public class InputController extends GestureDetector {

   private static final float MIN_ZOOM = 0.01f;
   private static final float MAX_ZOOM = 0.05f;

   private final OrthographicCamera camera;

   private TweenManager tweenManager;

   public InputController(OrthographicCamera camera) {
      super(new GListener(camera));
      this.camera = camera;
      camera.zoom = MIN_ZOOM;

      tweenManager = new TweenManager();
      Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
   }

   public void update(float deltaTime) {
      tweenManager.update(deltaTime);
   }

   @Override
   public boolean scrolled(int amount) {
      tweenManager.killAll();

      Tween.to(camera, CameraAccessor.TYPE_ZOOM, 1)
         .target(camera.zoom + 0.01f * amount)
         .ease(Quad.OUT)
         .start(tweenManager);

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

   private static class CameraAccessor implements TweenAccessor<OrthographicCamera> {

      final static int TYPE_ZOOM = 1;

      @Override
      public int getValues(OrthographicCamera target, int tweenType, float[] returnValues) {
         switch (tweenType) {
            case TYPE_ZOOM:
               returnValues[0] = target.zoom;
               return 1;
            default:
               returnValues[0] = 0;
               return 1;
         }
      }

      @Override
      public void setValues(OrthographicCamera target, int tweenType, float[] newValues) {
         switch (tweenType) {
            case TYPE_ZOOM:
               target.zoom = newValues[0];
               target.zoom = MathUtils.clamp(target.zoom, MIN_ZOOM, MAX_ZOOM);
               break;
            default: break;
         }
      }
   }
}
