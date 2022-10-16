package nz.ac.auckland.se206;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class BadgeController extends SoundsController {

  // Count the number of badges
  private static int badgeCounter = 0;

  @FXML private Button menuButton;

  @FXML private VBox badgesListBox;
  @FXML private Label badgeTitleLabel;

  private ArrayList<String> badgeStringArray = new ArrayList<String>();

  /**
   * initialize will load in all the badges
   *
   * @throws FileNotFoundException if a badge is not found throw an exception
   */
  public void initialize() throws FileNotFoundException {
    // Initialise all the tool tip texts
    // First 3 texts refer to the accuracy badges
    // Next 4 refer to the words badges
    // Next 4 refer to the time badges
    // Next 4 refer to the confidence badges
    // Next 4 refer to having all the difficulty badges
    // Next 3 refer to time ticking down badges
    // Next 3 refer to wins and consecutive wins badges
    // Last 4 are for misc badges
    Collections.addAll(
        badgeStringArray,
        "Win the game with your word being in the top 3 guesses!",
        "Win a game with your word being in the top 2 guesses!",
        "Win a game with your word being the top guess!",
        "Win a game on difficulty Easy for Words!",
        "Win a game on difficulty Medium for Words!",
        "Win a game on difficulty Hard for Words!",
        "Win a game on difficulty Master for Words!",
        "Win a game on difficulty Easy for Time!",
        "Win a game on difficulty Medium for Time!",
        "Win a game on difficulty Hard for Time!",
        "Win a game on difficulty Master for Time!",
        "Win a game on difficulty Easy for Confidence!",
        "Win a game on difficulty Medium for Confidence!",
        "Win a game on difficulty Hard for Confidence!",
        "Win a game on difficulty Master for Confidence!",
        "Have all the Easy difficulty badges",
        "Have all the Medium difficulty badges",
        "Have all the Hard difficulty badges",
        "Have all the Master difficulty badges",
        "Win a game in the last 30 seconds",
        "Win a game in the last 10 seconds",
        "Win a game in the last second!",
        "Win a game!",
        "Win 2 games in a row!",
        "Win 5 games in a row!",
        "Draw your own profile picture!",
        "Play Zen Mode",
        "View the Stats Page",
        "View the Badges Page");

    // this will load all the types of badges in
    loadBadges();
  }

  /**
   * Loads which badges that the user has won and also gets the badge pictures
   *
   * @throws FileNotFoundException Error if the file location of the badge isn't found
   */
  public void loadBadges() throws FileNotFoundException {
    badgeTitleLabel.setText("Badges For: " + Users.getUserName());
    badgesListBox.getChildren().clear();
    for (Entry<String, Map<String, Boolean>> category : Users.getBadges().entrySet()) {
      // Creates new JavaFX to store the badges into
      HBox categoryBox = new HBox();
      categoryBox.setAlignment(Pos.BOTTOM_CENTER);
      categoryBox.setPadding(new Insets(0, 0, 20, 0));
      Label categoryLabel = new Label();
      // Sets the category of the badges
      categoryLabel.setText(category.getKey());
      categoryLabel.setStyle("-fx-font-family: 'Maybe Next'; -fx-font-size: 32;");
      categoryLabel.setTextFill(Color.web("#a55a29"));
      badgesListBox.getChildren().add(categoryLabel);
      // Get each individual badge from the category
      for (Entry<String, Boolean> badges : category.getValue().entrySet()) {
        // Checks if the user has won the badge
        String badgeImageLocation = badges.getKey();
        if (!badges.getValue()) {
          if (badges.getKey().equals("E")
              || badges.getKey().equals("M")
              || badges.getKey().equals("H")
              || badges.getKey().equals("MS")) {
            badgeImageLocation = "Null";
          } else {
            badgeImageLocation = badgeImageLocation + "_Null";
          }
        }
        // Gets the badge image from the file location
        String dir =
            Users.folderDirectory
                + "/src/main/resources/images/badges/"
                + category.getKey().toString().replaceAll("\\s", "_")
                + "/"
                + badgeImageLocation.replaceAll("\\s", "_")
                + ".png";
        InputStream stream = new FileInputStream(dir);
        Image image = new Image(stream);
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        Tooltip toolTip = updateToolTip(badgeCounter);

        String nameOfBadge;
        // Swaps the letter of the difficulty to the word. The default is the word as it stored as
        switch (badges.getKey()) {
          case "E":
            // Set up easy badge
            nameOfBadge = "Easy";
            break;
          case "M":
            // Set up medium badge
            nameOfBadge = "Medium";
            break;
          case "H":
            // Set up hard badge
            nameOfBadge = "Hard";
            break;
          case "MS":
            // Set up master badge
            nameOfBadge = "Master";
            break;
          default:
            nameOfBadge = badges.getKey();
            break;
        }

        // Creates a new VBox to store the badge and label
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setPrefHeight(200);
        vbox.setMinWidth(180);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().add(imageView);
        // Create the new label for the badge
        Label badgeLabel = new Label();
        badgeLabel.setText(nameOfBadge);
        badgeLabel.setStyle("-fx-font-family: 'Maybe Next'; -fx-font-size: 20;");
        badgeLabel.setTextFill(Color.web("#a55a29"));
        badgeLabel.setPadding(new Insets(5, 0, 0, 0));
        // Adds the label and the vBox to the overall category
        vbox.getChildren().add(badgeLabel);
        categoryBox.getChildren().add(vbox);
        // Add in the tool tip
        Tooltip.install(imageView, toolTip);
        // Increment the badge counter
        badgeCounter++;
      }
      // Adds the category to the overall badgelist
      badgesListBox.getChildren().add(categoryBox);
    }
    // Reset the badge counter
    badgeCounter = 0;
  }

  /**
   * updateToolTip will create a tool tip and update the image
   *
   * @param counter takes in the badge position
   * @return the tool tip with respective text
   */
  private Tooltip updateToolTip(int counter) {
    Tooltip toolTip = new Tooltip();
    // set show duration to 0
    toolTip.setShowDelay(Duration.ZERO);
    // Set the designated text
    toolTip.setText(badgeStringArray.get(counter));
    toolTip.getStyleClass().add("toolTip");
    return toolTip;
  }

  /**
   * onMenu will take the user back to the main menu
   *
   * @param event takes in a JavaFX element
   */
  @FXML
  private void onMenuReturn(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }
}
