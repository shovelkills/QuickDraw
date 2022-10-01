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

  // Define FXML fields
  @FXML private ChoiceBox<String> accuracyMenu;
  @FXML private ChoiceBox<String> wordsMenu;
  @FXML private ChoiceBox<String> timeMenu;
  @FXML private ChoiceBox<String> confidenceMenu;
  private boolean started = false;

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
      // Set the default values to easy
      menu.setValue(difficultyMap.get(Difficulty.E));
    }
  }

  @FXML
  private void onStartGame(ActionEvent event)
      throws IOException, CsvException, URISyntaxException, ModelException {
    DifficultyBuilder.difficultySetter(
        accuracyMenu.getValue(),
        wordsMenu.getValue(),
        timeMenu.getValue(),
        confidenceMenu.getValue());
    started = true;

    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAME));
  }
}
