package nz.ac.auckland.se206;

import ai.djl.ModelException;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.dict.WordNotFoundException;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class GameSelectController {

  public enum GameMode {
    HIDDEN_WORD,
    NORMAL,
    ZEN,
    PROFILE,
  }

  private static GameMode currentGameMode = GameMode.NORMAL;
  // Local game mode will remember the last game played (not including profile)
  private static GameMode localGameMode = GameMode.NORMAL;

  public static GameMode getCurrentGameMode() {
    return currentGameMode;
  }

  public static void setCurrentGameMode(GameMode currentGameMode) {
    GameSelectController.currentGameMode = currentGameMode;
  }

  // Define FXML fields
  @FXML private ChoiceBox<String> accuracyMenu;
  @FXML private ChoiceBox<String> wordsMenu;
  @FXML private ChoiceBox<String> timeMenu;
  @FXML private ChoiceBox<String> confidenceMenu;
  @FXML private Button definitionButton;
  @FXML private Button normalButton;
  @FXML private Button zenButton;
  @FXML private Tooltip toolTip1;
  @FXML private Tooltip toolTip2;
  @FXML private Tooltip toolTip3;
  @FXML private Tooltip toolTip4;

  private ArrayList<Button> gameModes = new ArrayList<Button>();
  private ArrayList<Tooltip> toolTips = new ArrayList<Tooltip>();

  private final HashMap<Difficulty, String> difficultyMap = new HashMap<Difficulty, String>();
  private ArrayList<ChoiceBox<String>> difficultyMenu = new ArrayList<ChoiceBox<String>>();
  // Task for alternating colour of the title and word label concurrently

  public void initialize() {
    Collections.addAll(toolTips, toolTip1, toolTip2, toolTip3, toolTip4);
    Collections.addAll(gameModes, definitionButton, normalButton, zenButton);
    difficultyMap.put(Difficulty.E, "EASY");
    difficultyMap.put(Difficulty.M, "MEDIUM");
    difficultyMap.put(Difficulty.H, "HARD");
    difficultyMap.put(Difficulty.MS, "MASTER");

    Collections.addAll(difficultyMenu, accuracyMenu, wordsMenu, timeMenu, confidenceMenu);
    for (ChoiceBox<String> menu : difficultyMenu) {
      // Populate difficulty dropdown
      menu.getItems().add(difficultyMap.get(Difficulty.E));
      menu.getItems().add(difficultyMap.get(Difficulty.M));
      menu.getItems().add(difficultyMap.get(Difficulty.H));
      if (menu != accuracyMenu) {
        menu.getItems().add(difficultyMap.get(Difficulty.MS));
      }
    }
    // Sets the menus to the defaults
    // Set the local game mode
    setLocalGameMode();
    for (Tooltip toolTip : toolTips) {
      toolTip.setShowDelay(Duration.ZERO);
      toolTip.setWrapText(true);
      toolTip.setAutoFix(true);
    }

    CanvasController canvas = App.getCanvasController();
    try {
      canvas.setPreGameInterface();
    } catch (IOException
        | CsvException
        | URISyntaxException
        | ModelException
        | WordNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    canvas.getGame().setIsGhostGame(true);

    canvas.onStartGame();

    try {
      canvas.onRestartGame();
    } catch (IOException
        | URISyntaxException
        | CsvException
        | ModelException
        | WordNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // Wait for UI elements to be in place on restart
    canvas.getGame().setIsGhostGame(false);
  }

  /**
   * onStartGame will start the game based on the given settings
   *
   * @param event
   * @throws IOException
   * @throws CsvException
   * @throws URISyntaxException
   * @throws ModelException
   * @throws InterruptedException
   */
  @FXML
  private void onStartGame(ActionEvent event)
      throws IOException, CsvException, URISyntaxException, ModelException, InterruptedException {

    Task<Void> preGameTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // Set up the pre-game UI elements that are in common with restarting the game
            updateProgress(0, 1);
            CanvasController canvas = App.getCanvasController();
            Thread.sleep(100);
            updateProgress(0.5, 1);
            canvas.setPreGameInterface();
            updateProgress(1, 1);
            Thread.sleep(100);

            return null;
          }
        };
    // Find progress bar on loading screen
    ProgressBar progressBar = App.getLoadingController().getProgressBar();
    progressBar.progressProperty().unbind();
    // Bind progress bar
    progressBar.progressProperty().bind(preGameTask.progressProperty());

    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();

    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.LOADING));
    if (currentGameMode != GameMode.ZEN) {
      // Sets the game difficulty to the user
      Users.setGameDifficulty(
          accuracyMenu.getValue(),
          wordsMenu.getValue(),
          timeMenu.getValue(),
          confidenceMenu.getValue());

      DifficultyBuilder.setDifficulty(
          Users.getIndividualDifficulty("accuracyDifficulty"),
          Users.getIndividualDifficulty("wordsDifficulty"),
          Users.getIndividualDifficulty("timeDifficulty"),
          Users.getIndividualDifficulty("confidenceDifficulty"));
    }
    if (currentGameMode == GameMode.ZEN) {
      Badges.winBadge("Misc", "Play Zen Mode");
    }
    preGameTask.setOnSucceeded(
        e -> {
          progressBar.progressProperty().unbind();
          // Move to the next scene
          sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAME));
        });
    Thread preGameThread = new Thread(preGameTask);
    // Allow the task to be cancelled on closing of application
    preGameThread.setDaemon(true);
    preGameThread.start();
  }

  @FXML
  private void onExitToMenu(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }

  /**
   * onSelectGameMode will change the current GameMode
   *
   * @param event
   */
  @FXML
  private void onSelectGameMode(ActionEvent event) {
    Button gameModeButton = (Button) event.getSource();
    for (Button button : gameModes) {
      if (button == gameModeButton) {
        button.setDisable(true);
      } else {
        button.setDisable(false);
      }
    }
    switch (gameModeButton.getText()) {
      case "Normal":
        // Switch to normal game mode
        wordsMenu.setDisable(false);
        wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        // Change the local game mode
        localGameMode = GameMode.NORMAL;
        break;
      case "Zen":
        // Switch to zen game mode
        wordsMenu.setDisable(true);
        wordsMenu.setValue("HARD");
        accuracyMenu.setDisable(true);
        accuracyMenu.setValue("N/A");
        confidenceMenu.setDisable(true);
        confidenceMenu.setValue("N/A");
        timeMenu.setDisable(true);
        timeMenu.setValue("N/A");
        DifficultyBuilder.setDifficulty("-1", wordsMenu.getValue(), "-1", "-1");
        // Change the local game mode
        localGameMode = GameMode.ZEN;
        break;
      case "Hidden Word":
        // Switch to hidden word game mode
        wordsMenu.setDisable(false);
        wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        // Change the local game mode
        localGameMode = GameMode.HIDDEN_WORD;
        break;
      default:
        // Set the default game mode to normal
        localGameMode = GameMode.NORMAL;
        break;
    }
    setLocalGameMode();
  }

  /** Sets the difficulties of the choice menus of the users previous difficulties selected */
  public void setUserDifficulties() {
    // Check if the current game mode is zen
    if (getCurrentGameMode() == GameMode.ZEN) {
      // set all the menus to N/A
      accuracyMenu.setValue("N/A");
      confidenceMenu.setValue("N/A");
      timeMenu.setValue("N/A");
    } else {
      // Otherwise load in the user's difficulties
      accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
      confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
      timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
    }
    // For the word menu set the words difficulty
    wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
  }

  /** Sets the local game mode */
  public static void setLocalGameMode() {
    setCurrentGameMode(localGameMode);
  }
}
