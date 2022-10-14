package nz.ac.auckland.se206;

import java.awt.Toolkit;
import java.util.HashMap;
import javafx.scene.Parent;

public class SceneManager {
  public enum AppUi {
    MAIN_MENU,
    GAME,
    GAMESELECT,
    GRAPH,
    BADGES,
    LOADING,
    STATS,
    USERSELECT,
    USERCREATE
  }

  private static final double WIDTH_PADDING = 200;
  private static final double HEIGHT_PADDING = 150;

  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();

  public static void addUi(AppUi appUi, Parent uiRoot) {
    sceneMap.put(appUi, uiRoot);
  }

  public static Parent getUiRoot(AppUi appUi) {
    return sceneMap.get(appUi);
  }

  public static double getMaxWindowedWidth() {
    return Toolkit.getDefaultToolkit().getScreenSize().getWidth() - WIDTH_PADDING;
  }

  public static double getMaxWindowedHeight() {
    return Toolkit.getDefaultToolkit().getScreenSize().getHeight() - HEIGHT_PADDING;
  }
}
