package com.borschtlabs.gytm.dev;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopDevLauncher {
   public static void main(String[] arg) {
      LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
      config.width = 1024;
      config.height = 768;
      config.foregroundFPS = 60;
      config.backgroundFPS = 60;
      config.vSyncEnabled = false;

      new LwjglApplication(new DevGame(), config);
   }
}
