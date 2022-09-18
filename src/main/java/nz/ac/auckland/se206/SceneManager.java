package nz.ac.auckland.se206;

import java.awt.Toolkit;
import java.util.HashMap;
import javafx.scene.Parent;

public class SceneManager {
  public enum AppUi {
    MAIN_MENU,
    GAME
  }
  
  private static final double widthPadding = 200;
  private static final double heightPadding = 150;

  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();

  public static void addUi(AppUi appUi, Parent uiRoot) {
    sceneMap.put(appUi, uiRoot);
  }

  public static Parent getUiRoot(AppUi appUi) {
    return sceneMap.get(appUi);
  }
  
  public static double getMaxWindowWidth() {
	  return Toolkit.getDefaultToolkit().getScreenSize().getWidth() - widthPadding;
  }
  
  public static double getMaxWindowHeight() {
	  return Toolkit.getDefaultToolkit().getScreenSize().getHeight() - heightPadding;
  }
}