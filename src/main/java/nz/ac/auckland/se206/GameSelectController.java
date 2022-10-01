package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import nz.ac.auckland.se206.words.CategorySelector.Difficulty;

public class GameSelectController {

  // Define FXML fields
  @FXML
  private ChoiceBox<String> accuracyMenu;
  @FXML
  private ChoiceBox<String> wordsMenu;
  @FXML
  private ChoiceBox<String> timeMenu;
  @FXML
  private ChoiceBox<String> confidenceMenu;

  private final HashMap<Difficulty, String> difficultyMap = new HashMap<Difficulty, String>();
  private ArrayList<ChoiceBox<String>> difficultyMenu = new ArrayList<ChoiceBox<String>>();

  public void initialize() {
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
      menu.getItems().add(difficultyMap.get(Difficulty.MS));
      // Set the default values to easy
      menu.setValue(difficultyMap.get(Difficulty.E));

    }
  }

  @FXML
  private void onStartGame(ActionEvent event) {
    DifficultyBuilder difficulty = new DifficultyBuilder(accuracyMenu.getValue(),
        wordsMenu.getValue(), timeMenu.getValue(), confidenceMenu.getValue());
  }
}
