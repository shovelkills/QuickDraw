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
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class GameSelectController {

  public enum GameMode {
    NORMAL,
    ZEN,
    DEFINITION
  }

  protected static GameMode currentGameMode = GameMode.NORMAL;

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
  private boolean started = false;

  private ArrayList<Button> gameModes = new ArrayList<Button>();

  private final HashMap<Difficulty, String> difficultyMap = new HashMap<Difficulty, String>();
  private ArrayList<ChoiceBox<String>> difficultyMenu = new ArrayList<ChoiceBox<String>>();
  // Task for alternating colour of the title and word label concurrently
  private Task<Void> preGameTask =
      new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          // Set up the pre-game UI elements that are in common with restarting the game
          while (true) {
            if (started) {
              App.getCanvasController().setPreGameInterface();
              started = false;
            }
            Thread.sleep(1);
          }
        }
      };

  public void initialize() {
    Thread preGameThread = new Thread(preGameTask);
    // Allow the task to be cancelled on closing of application
    preGameThread.setDaemon(true);
    preGameThread.start();
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

  }

  /**
   * onStartGame will start the game based on the given settings
   *
   * @param event
   * @throws IOException
   * @throws CsvException
   * @throws URISyntaxException
   * @throws ModelException
   */
  @FXML
  private void onStartGame(ActionEvent event)
      throws IOException, CsvException, URISyntaxException, ModelException {
    if (currentGameMode != GameMode.ZEN) {
      // Sets the game difficulty to the user
      Users.setGameDifficulty(
          accuracyMenu.getValue(),
          wordsMenu.getValue(),
          timeMenu.getValue(),
          confidenceMenu.getValue());

      DifficultyBuilder.difficultySetter(
          Users.getIndividualDifficulty("accuracyDifficulty"),
          Users.getIndividualDifficulty("wordsDifficulty"),
          Users.getIndividualDifficulty("timeDifficulty"),
          Users.getIndividualDifficulty("confidenceDifficulty"));
    }
    started = true;

    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAME));
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
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        setCurrentGameMode(GameMode.NORMAL);
        break;
      case "Zen":
        // Switch to zen game mode
        accuracyMenu.setDisable(true);
        accuracyMenu.setValue("N/A");
        confidenceMenu.setDisable(true);
        confidenceMenu.setValue("N/A");
        timeMenu.setDisable(true);
        timeMenu.setValue("N/A");
        DifficultyBuilder.difficultySetter("-1", wordsMenu.getValue(), "-1", "-1");
        setCurrentGameMode(GameMode.ZEN);
        break;
      case "Definition":
        // Switch to hidden word game mode
        accuracyMenu.setDisable(false);
        accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
        confidenceMenu.setDisable(false);
        confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
        timeMenu.setDisable(false);
        timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
        setCurrentGameMode(GameMode.DEFINITION);
        break;
      default:
        // Set the default game mode to normal
        setCurrentGameMode(GameMode.NORMAL);
        break;
    }
  }

  /** Sets the difficulties of the choice menus of the users previous difficulties selected */
  public void setUserDifficulties() {
    if (getCurrentGameMode() == GameMode.ZEN) {
      accuracyMenu.setValue("N/A");
      confidenceMenu.setValue("N/A");
      timeMenu.setValue("N/A");
    } else {
      accuracyMenu.setValue(Users.getIndividualDifficulty("accuracyDifficulty"));
      confidenceMenu.setValue(Users.getIndividualDifficulty("confidenceDifficulty"));
      timeMenu.setValue(Users.getIndividualDifficulty("timeDifficulty"));
    }
    wordsMenu.setValue(Users.getIndividualDifficulty("wordsDifficulty"));
  }
}
