package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {
  private static BadgeController badgeController;
  private static StatsController statsController;
  private static MenuController menuController;
  private static UsersController usersController;
  private static UserCreationController userCreationController;
  private static GameSelectController gameSelectController;
  private static CanvasController canvasController;
  private static LoadingController loadingController;
  private static GraphController graphController;

  public static void main(final String[] args) {
    launch();
  }

  /**
   * Returns the node associated to the input file. The method expects that the file is located in
   * "src/main/resources/fxml".
   *
   * @param fxml The name of the FXML file (without extension).
   * @return The node of the input file.
   * @throws IOException If the file is not found.
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    // Retrieve and store a reference to controllers needed by other classes
    if (fxml == "stats") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      statsController = loader.getController();
      // return the root for switching scenes
      return root;
    }
    if (fxml == "menu") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      menuController = loader.getController();
      // return the root for switching scenes
      return root;
    }
    if (fxml == "users") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      usersController = loader.getController();
      // return the root for switching scenes
      return root;
    }
    if (fxml == "gameselect") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      gameSelectController = loader.getController();
      // return the root for switching scenes
      return root;
    }

    if (fxml == "loading") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      loadingController = loader.getController();
      // return the root for switching scenes
      return root;
    }
    if (fxml == "canvas") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      canvasController = loader.getController();
      // return the root for switching scenes
      return root;
    }
    if (fxml == "usercreation") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      userCreationController = loader.getController();
      // return the root for switching scenes
      return root;
    }
    if (fxml == "graph") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      graphController = loader.getController();
      // return the root for switching scenes
      return root;
    }

    if (fxml == "badges") {
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      Parent root = (Parent) loader.load();
      badgeController = loader.getController();
      // return the root for switching scenes
      return root;
    }

    // For other FXMLs just return the root
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  public static CanvasController getCanvasController() {
    return canvasController;
  }

  public static MenuController getMenuController() {
    return menuController;
  }

  public static StatsController getStatsController() {
    return statsController;
  }

  public static GameSelectController getGameSelectController() {
    return gameSelectController;
  }

  public static UserCreationController getCreationController() {
    return userCreationController;
  }

  public static UsersController getUsersController() {
    return usersController;
  }

  public static LoadingController getLoadingController() {
    return loadingController;
  }

  public static GraphController getGraphController() {
    return graphController;
  }

  public static BadgeController getBadgeController() {
    return badgeController;
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "Menu" scene.
   *
   * @param stage The primary stage of the application.
   * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
   */
  @Override
  public void start(final Stage stage) throws IOException {
    // Add all the scenes to the scene manager
    SceneManager.addUi(AppUi.MAIN_MENU, loadFxml("menu"));
    SceneManager.addUi(AppUi.GAME, loadFxml("canvas"));
    SceneManager.addUi(AppUi.GAMESELECT, loadFxml("gameselect"));
    SceneManager.addUi(AppUi.GRAPH, loadFxml("graph"));
    SceneManager.addUi(AppUi.BADGES, loadFxml("badges"));
    SceneManager.addUi(AppUi.LOADING, loadFxml("loading"));
    SceneManager.addUi(AppUi.USERSELECT, loadFxml("users"));
    SceneManager.addUi(AppUi.USERCREATE, loadFxml("usercreation"));
    SceneManager.addUi(AppUi.STATS, loadFxml("stats"));
    // Creates a new scene that starts on the main menu in windowed mode
    final Scene scene =
        new Scene(
            SceneManager.getUiRoot(AppUi.MAIN_MENU),
            SceneManager.getMaxWindowedWidth(),
            SceneManager.getMaxWindowedHeight());
    // Adds the css formatting to the scenes
    scene.getStylesheets().add("/css/menu.css");
    scene.getStylesheets().add("/css/canvas.css");
    scene.getStylesheets().add("/css/creation.css");
    scene.getStylesheets().add("/css/stats.css");
    scene.getStylesheets().add("/css/gameselect.css");
    scene.getStylesheets().add("/css/userselection.css");

    // Set the scene and then show
    stage.setTitle("Draw Game");
    stage.setScene(scene);
    stage.show();
  }
}
