package nz.ac.auckland.se206;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class StatsController {
  // Declare all the FXML fields
  @FXML private Button backButton;
  @FXML private Button statsTabButton;
  @FXML private Button wordsTabButton;
  @FXML private Label titleLabel;
  @FXML private Label winsLabel;
  @FXML private Label lossesLabel;
  @FXML private Label fastestWordLabel;
  @FXML private Label fastestTimeLabel;
  @FXML private GridPane statsGridPane;
  @FXML private VBox wordsListPaneBox;
  @FXML private VBox wordsListBox;

  /** JavaFX calls this method once the GUI elements are loaded. */
  public void initialize() {
    // Updates all the stats
    updateAllStats();
  }

  /**
   * Activates when the words list tab button is pressed. Refreshes user stats display for the
   * current user.
   */
  @FXML
  private void onStatsTab() {
    // Set tab title
    titleLabel.setText("Stats for: " + Users.getUserName());
    // Set style of elements to indicate tab is selected
    wordsListPaneBox.setVisible(false);
    statsGridPane.setVisible(true);
    statsTabButton.setStyle(
        "-fx-background-color: linear-gradient(to right, #FFCAE9, #E7AEE8); -fx-text-fill: #791E94;");
    wordsTabButton.setStyle(".tabButton");
  }

  /**
   * on WordsTab Activates when the words list tab button is pressed. Refreshes the displayed word
   * list for the current user.
   */
  @FXML
  private void onWordsTab() {
    // Set tab title
    titleLabel.setText("Word History for: " + Users.getUserName());
    // Set style of elements to indicate tab is selected
    statsGridPane.setVisible(false);
    wordsListPaneBox.setVisible(true);
    wordsTabButton.setStyle(
        "-fx-background-color: linear-gradient(to right, #FFCAE9, #E7AEE8); -fx-text-fill: #791E94;");
    statsTabButton.setStyle(".tabButton");
  }

  /**
   * Updates the word history list for the current user and displays them in the word list
   * scrollpane
   */
  private void updateWordList() {
    // Clear the word list VBox and reset the index counter
    wordsListBox.getChildren().clear();
    // Show special message if no games have been played
    if (Users.getWordHistory().size() == 0) {
      Label wordLabel = new Label();
      wordLabel.setText(
          "Once you've played Quick Draw, every word you have drawn will be shown here!");
      wordLabel.setMaxWidth(Integer.MAX_VALUE);
      wordLabel.alignmentProperty().set(Pos.CENTER);
      Font font = Font.loadFont("file:src/main/resources/fonts/somethingwild-Regular.ttf", 42);
      wordLabel.setFont(font);
      wordLabel.setStyle("-fx-background-color: #FFCAE9");
      wordsListBox.getChildren().add(wordLabel);
      return;
    }
    int index = 0;
    // Iterate through all words encountered by the current user
    for (String word : Users.getWordHistory()) {
      // Create the label to be used for this word in the history and set the label text accordingly
      Label wordLabel = new Label();
      wordLabel.setText(word);

      // Set every second label background to a contrasting colour for readability
      if (index % 2 == 0) {
        wordLabel.setStyle("-fx-background-color: #FFCAE9");
      }

      // Set label formatting properties
      wordLabel.setMaxWidth(Integer.MAX_VALUE);
      wordLabel.alignmentProperty().set(Pos.CENTER);
      Font font = Font.loadFont("file:src/main/resources/fonts/somethingwild-Regular.ttf", 42);
      wordLabel.setFont(font);

      // Add labels to the VBox container within the scrollpane
      wordsListBox.getChildren().add(wordLabel);
      index++;
    }
  }

  /**
   * Updates the displayed values for the stats tab of the stats scene by getting them from the
   * current user
   */
  private void updateUserStats() {
    // Show wins and losses totals (can be 0)
    winsLabel.setText(Integer.toString(Users.getWins()));
    lossesLabel.setText(Integer.toString(Users.getLosses()));
    // Show special labels if the user has no saved games
    if (Users.getFastestTime() == -1) {
      fastestWordLabel.setText("No drawings yet!");
      fastestTimeLabel.setText("So nothing to show!");
    } else {
      // update stats according to current user variables
      fastestWordLabel.setText(Users.getFastestWord());
      fastestTimeLabel.setText(
          "drawn in " + Integer.toString(Users.getFastestTime()) + " seconds!");
    }
  }

  /**
   * Takes the user back to the main menu
   *
   * @param event The Button press event
   */
  @FXML
  private void onBackButton(ActionEvent event) {
    // Get the button pressed
    Button buttonPressed = (Button) event.getSource();
    Scene currentScene = buttonPressed.getScene();
    // Travel back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }

  /**
   * Used to "reset" the entire stats window for the user when transitioning to the stats scene.
   * Updates all stats and resets open tab to the stats tab.
   */
  public void updateAllStats() {
    // Update all stats: the users stats, their words, and the user
    updateUserStats();
    updateWordList();
    onStatsTab();
  }
}
