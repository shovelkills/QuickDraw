package nz.ac.auckland.se206;

import ai.djl.ModelException;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.dict.WordNotFoundException;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class GameSelectController extends SoundsController {

  public enum GameMode {
    HIDDEN_WORD,
    NORMAL,
    ZEN,
    PROFILE,
    BLITZ
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

  /** Sets the local game mode */
  public static void setLocalGameMode() {
    setCurrentGameMode(localGameMode);
  }

  // Define FXML fields
  @FXML private Button blitzButton;
  @FXML private Button definitionButton;
  @FXML private Button normalButton;
  @FXML private Button zenButton;
  @FXML private ChoiceBox<String> accuracyMenu;
  @FXML private ChoiceBox<String> wordsMenu;
  @FXML private ChoiceBox<String> timeMenu;
  @FXML private ChoiceBox<String> confidenceMenu;
  @FXML private Tooltip toolTip1;
  @FXML private Tooltip toolTip2;
  @FXML private Tooltip toolTip3;
  @FXML private Tooltip toolTip4;
  @FXML private VBox zenDescription;
  @FXML private VBox normalDescription;
  @FXML private VBox definitionDescription;
  @FXML private VBox blitzDescription;

  // Define arrays and hash maps
  private final HashMap<Difficulty, String> difficultyMap = new HashMap<Difficulty, String>();
  private ArrayList<Button> gameModes = new ArrayList<Button>();
  private ArrayList<Tooltip> toolTips = new ArrayList<Tooltip>();
  private ArrayList<ChoiceBox<String>> difficultyMenu = new ArrayList<ChoiceBox<String>>();

  /**
   * initialize method will be called upon starting the game to add all the difficulties into the
   * game select canvas so that the user can choose which difficulties they will play in their games
   */
  public void initialize() {
    Collections.addAll(toolTips, toolTip1, toolTip2, toolTip3, toolTip4);
    Collections.addAll(gameModes, definitionButton, normalButton, zenButton, blitzButton);
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
    updateGameModeStyle("normalButton");
    for (Tooltip toolTip : toolTips) {
      toolTip.setShowDelay(Duration.ZERO);
      toolTip.setWrapText(true);
      toolTip.setAutoFix(true);
    }

    CanvasController canvas = App.getCanvasController();
    try {
      // Set up the pregame
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
    // Start the game
    canvas.onStartGame(null);

    try {
      canvas.onRestartGame(null);
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
   * @param event takes in the click to start the game
   */
  @FXML
  private void onStartGame(ActionEvent event) {

    Task<Void> preGameTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // Set up the pre-game UI elements that are in common with restarting the game
            updateProgress(0, 1);
            CanvasController canvas = App.getCanvasController();
            Thread.sleep(100);
            // Update progress to half way and then set up the pre-game interface
            updateProgress(0.5, 1);
            canvas.setPreGameInterface();
            canvas.getGame().setIsGhostGame(true);
            Platform.runLater(
                () -> {
                  canvas.onStartGame(null);
                });
            // Wait for first cycle of game service
            Thread.sleep(1500);
            Platform.runLater(
                () -> {
                  try {
                    canvas.onRestartGame(null);
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                });
            updateProgress(1, 1);
            // Update progress completely
            Thread.sleep(300);
            // Wait for UI elements to be in place on restart
            canvas.getGame().setIsGhostGame(false);

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

      // Build the difficulty for all game modes except profile and zen
      DifficultyBuilder.setDifficulty(
          Users.getIndividualDifficulty("accuracyDifficulty"),
          Users.getIndividualDifficulty("wordsDifficulty"),
          Users.getIndividualDifficulty("timeDifficulty"),
          Users.getIndividualDifficulty("confidenceDifficulty"));
    }
    if (currentGameMode == GameMode.ZEN) {
      // Set up zen mode
      Badges.winBadge("Misc", "Play Zen Mode");
      Users.setGameDifficulty(
          Users.getIndividualDifficulty("accuracyDifficulty"),
          wordsMenu.getValue(),
          Users.getIndividualDifficulty("timeDifficulty"),
          Users.getIndividualDifficulty("confidenceDifficulty"));
      // Build the difficulty for zen mode
      DifficultyBuilder.setDifficulty("EASY", wordsMenu.getValue(), "-1", "-1");
    }
    // TODO add badge for Blitz game mode
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

  /**
   * onExitToMenu will take us back to the main menu from game select screen
   *
   * @param event takes in the click to start the game
   */
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
   * @param event takes in an JavaFX event
   */
  @FXML
  private void onSelectGameMode(ActionEvent event) {
    Button gameModeButton = (Button) event.getSource();
    // Loop through all the buttons
    for (Button button : gameModes) {
      if (button == gameModeButton) {
        button.setDisable(true);
      } else {
        button.setDisable(false);
      }
    }
    // See which button was pressed
    String buttonID = gameModeButton.getId();
    switch (buttonID) {
      case "normalButton":
        // Switch to normal game mode
        wordsMenu.setDisable(false);
        wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        updateGameModeStyle(buttonID);
        // Change the local game mode
        localGameMode = GameMode.NORMAL;
        break;
      case "zenButton":
        // Switch to zen game mode
        wordsMenu.setDisable(false);
        wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
        accuracyMenu.setDisable(true);
        accuracyMenu.setValue("N/A");
        confidenceMenu.setDisable(true);
        confidenceMenu.setValue("N/A");
        timeMenu.setDisable(true);
        timeMenu.setValue("N/A");
        updateGameModeStyle(buttonID);
        // Change the local game mode
        localGameMode = GameMode.ZEN;
        break;
      case "definitionButton":
        // Switch to hidden word game mode
        wordsMenu.setDisable(false);
        wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        updateGameModeStyle(buttonID);
        // Change the local game mode
        localGameMode = GameMode.HIDDEN_WORD;
        break;
      case "blitzButton":
        // Switch to blitz game mode
        wordsMenu.setDisable(false);
        wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        updateGameModeStyle(buttonID);
        localGameMode = GameMode.BLITZ;
        break;
      default:
        // Set the default game mode to normal
        localGameMode = GameMode.NORMAL;
        break;
    }
    // Update the local game mode
    setLocalGameMode();
  }

  /**
   * gameModeButtonSelect will update the styling for each class
   *
   * @param buttonID takes in the current button being switched
   */
  private void updateGameModeStyle(String buttonID) {
    // Clear all the buttons styles
    zenButton.getStyleClass().clear();
    normalButton.getStyleClass().clear();
    definitionButton.getStyleClass().clear();
    blitzButton.getStyleClass().clear();
    // Set the visibilities of all the buttons to false
    zenDescription.setVisible(false);
    normalDescription.setVisible(false);
    definitionDescription.setVisible(false);
    blitzDescription.setVisible(false);
    switch (buttonID) {
      case "zenButton":
        // Set zen to be selected
        zenButton.getStyleClass().add("zenButtonSelected");
        // Update all the other styling
        normalButton.getStyleClass().add("normalButton");
        definitionButton.getStyleClass().add("hiddenButton");
        blitzButton.getStyleClass().add("blitzButton");
        // Set the zen description's visibility on
        zenDescription.setVisible(true);
        break;
      case "normalButton":
        // Set normal to be selected
        normalButton.getStyleClass().add("normalButtonSelected");
        // Update all the other styling
        zenButton.getStyleClass().add("zenButton");
        definitionButton.getStyleClass().add("hiddenButton");
        blitzButton.getStyleClass().add("blitzButton");
        // Set the normal description's visibility on
        normalDescription.setVisible(true);
        break;
      case "definitionButton":
        // Set hidden to be selected
        definitionButton.getStyleClass().add("hiddenButtonSelected");
        // Update all the other styling
        zenButton.getStyleClass().add("zenButton");
        normalButton.getStyleClass().add("normalButton");
        blitzButton.getStyleClass().add("blitzButton");
        // Set the definition description's visibility on
        definitionDescription.setVisible(true);
        break;
      case "blitzButton":
        // Set blitz to be selected
        blitzButton.getStyleClass().add("blitzButtonSelected");
        // Update all the other styling
        zenButton.getStyleClass().add("zenButton");
        normalButton.getStyleClass().add("normalButton");
        definitionButton.getStyleClass().add("hiddenButton");
        // Set the blitz description's visibility on
        blitzDescription.setVisible(true);
        break;
      default:
        break;
    }
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
}
