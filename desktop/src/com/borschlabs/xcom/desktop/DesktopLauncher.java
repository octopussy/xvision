package com.borschlabs.xcom.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.borschlabs.xcom.XcomGame;

public class DesktopLauncher {
   public static void main(String[] arg) {
      LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
      config.width = 1024;
      config.height = 768;
      config.foregroundFPS = 60;
      config.backgroundFPS = 60;
      config.vSyncEnabled = false;

      new LwjglApplication(new XcomGame(), config);
   }
}
