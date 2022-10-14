package nz.ac.auckland.se206;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class BadgeController {

  @FXML private Button menuButton;

  @FXML private VBox badgesListBox;

  public void initilize() throws FileNotFoundException {
    loadBadges();
  }

  /**
   * Loads which badges that the user has won and also gets the badge pictures
   *
   * @throws FileNotFoundException Error if the file location of the badge isn't found
   */
  public void loadBadges() throws FileNotFoundException {
    badgesListBox.getChildren().clear();
    for (Entry<String, Map<String, Boolean>> category : Users.getBadges().entrySet()) {
      // Creates new JavaFX to store the badges into
      HBox categoryBox = new HBox();
      Label categoryLabel = new Label();
      // Sets the category of the badges
      categoryLabel.setText(category.getKey());
      badgesListBox.getChildren().add(categoryLabel);
      // Get each individual badge from the category
      for (Entry<String, Boolean> badges : category.getValue().entrySet()) {
        // Gets the badge image
        String dir = Users.folderDirectory + "/src/main/resources/badges/profile5.png";
        InputStream stream = new FileInputStream(dir);
        Image image = new Image(stream);
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        String nameOfBadge;
        // Swaps the letter of the difficulty to the word. The default is the word as it stored as
        switch (badges.getKey()) {
          case "E":
            nameOfBadge = "Easy";
            break;
          case "M":
            nameOfBadge = "Medium";
            break;
          case "H":
            nameOfBadge = "Hard";
            break;
          case "MS":
            nameOfBadge = "Master";
            break;
          default:
            nameOfBadge = badges.getKey();
            break;
        }

        // Checks if the user has won the badge
        if (!badges.getValue()) {
          imageView.setOpacity(0.25);
        }
        // Creates a new VBox to store the badge and label
        VBox vBox = new VBox();
        vBox.getChildren().add(imageView);
        Label badgeLabel = new Label();
        badgeLabel.setText(nameOfBadge);
        // Adds the label and the vBox to the overall category
        vBox.getChildren().add(badgeLabel);
        categoryBox.getChildren().add(vBox);
      }
      // Adds the category to the overall badgelist
      badgesListBox.getChildren().add(categoryBox);
    }
  }

  @FXML
  private void onMenu(ActionEvent event) {
    // Get the current scene
    Button backButton = (Button) event.getSource();
    Scene currentScene = backButton.getScene();
    // Move back to main menu
    currentScene.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }
}
