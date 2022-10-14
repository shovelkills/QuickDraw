package nz.ac.auckland.se206;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class MenuController {

  private static final String IDLE_STYLE =
      "-fx-effect: dropshadow(gaussian, #fff8f5, 10, 1, 0, 0);";
  private static final String HOVER_STYLE =
      "-fx-scale-x: 1.2; -fx-scale-y: 1.2; -fx-effect: dropshadow(gaussian, #fff8f5, 20, 0.8, 0, 0);";
  private static final String MOUSE_DOWN_STYLE =
      "-fx-scale-x: 1.2; -fx-scale-y: 1.2; -fx-effect: dropshadow(gaussian, #fff8f5, 30, 0.8, 0, 0);";
  // Declare the menu objects
  @FXML private Canvas canvas;
  @FXML private ImageView profileImageView;
  @FXML private Label titleLabel;
  @FXML private Label usernameLabel;

  @FXML private Button graphButton;
  @FXML private Button startButton;
  @FXML private Button statsButton;
  @FXML private Button exitButton;
  @FXML private Button exitTipButton;
  @FXML private Button selectedUserButton;

  /**
   * This method alternates alternates colours for a label
   *
   * @param label the FXML label given
   * @param colour1 the first colour to alternate between
   * @param colour2 the second colour to alternate between
   */
  private Task<Void> backgroundTask =
      new Task<Void>() {

        @Override
        protected Void call() throws Exception {
          // Alternates colours
          alternateColours(titleLabel, Color.BLACK, Color.web("#979797"));
          return null;
        }
      };

  /**
   * JavaFX calls this method once the GUI elements are loaded. Here we load in the fonts and user's
   * images and start up the threads
   */
  public void initialize() {
    // Load in a new font and set it to the tile
    Font.loadFont("file:src/main/resources/fonts/Maybe-Next.ttf", 60);
    // Set default guest login label
    usernameLabel.setText("Guest");
    // Load in the guest image
    File guestImageFile = new File("src/main/resources/users/guest.png");
    Image guestImage = new Image(guestImageFile.toURI().toString());
    profileImageView.setImage(guestImage);
    Thread titleAnimation = new Thread(backgroundTask);
    // Start the thread
    titleAnimation.setDaemon(true);
    titleAnimation.start();
    // Load all the users from the user list
    Users.loadUsersFromList();
    // Instantiate a new guest for this session.
    Users.createNewGuest();
    Users.loadUser("Guest");
    // set up user profile selection button
    selectedUserButton.setOnMouseEntered(
        e -> {
          profileImageView.setStyle(HOVER_STYLE);
        });
    selectedUserButton.setOnMouseExited(
        e -> {
          profileImageView.setStyle(IDLE_STYLE);
        });
    selectedUserButton.setOnMousePressed(
        e -> {
          profileImageView.setStyle(MOUSE_DOWN_STYLE);
        });
  }

  /**
   * alternateColours will create an alternating colour timeline on our title
   *
   * @param label takes in a label, usually the title
   * @param colour1 a colour to oscillate between
   * @param colour2 another colour to oscillate between
   */
  private void alternateColours(Label label, Color colour1, Color colour2) {
    // Create a new timeline object
    Timeline timeline = new Timeline();
    // Loop three times
    for (int i = 0; i < 3; i++) {
      // Add a new keyframe accordingly
      timeline
          .getKeyFrames()
          .add(
              new KeyFrame(
                  Duration.seconds(2.5 * i),
                  new KeyValue(label.textFillProperty(), i != 1 ? colour1 : colour2)));
    }
    // Play the animation indefinitely
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.play();
  }

  /**
   * updateUserImage will change the user image based on switching
   *
   * @param image the player's image that is saved
   */
  public void updateUserImage(Image image) {
    profileImageView.setImage(image);
  }

  /**
   * updateUsernameLabel will change the user name label based on switching
   *
   * @param username the player's name
   */
  public void updateUsernameLabel(String username) {
    usernameLabel.setText(username);
  }

  /**
   * onStartGame will start up the game select screen
   *
   * @param event takes in an event to return back to the menu
   */
  @FXML
  private void onStartGame(ActionEvent event) {

    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();

    GameSelectController.setLocalGameMode();
    // Gets the difficulties that the user previously selected
    App.getGameSelectController().setUserDifficulties();
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAMESELECT));
  }

  /**
   * onMoveToStats will move us to the stats scene
   *
   * @param event takes in an event to return back to the menu
   * @throws IOException reading/writing exception
   */
  @FXML
  private void onMoveToStats(ActionEvent event) throws IOException {
    // Tell the stats controller to update values for the current user
    StatsController statsController = App.getStatsController();
    statsController.updateAllStats();
    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();

    // Wins badge that looks at stats
    Badges.winBadge("Misc", "View Stats Page");
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.STATS));
  }

  /**
   * onUserSelect will take us to the user selection screen
   *
   * @param event takes in an event to return back to the menu
   */
  @FXML
  private void onUserSelect(ActionEvent event) {
    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.USERSELECT));
  }

  /** onExitGame will let us leave the game after asking for confirmation */
  @FXML
  private void onExitGame() {
    // Create a pop up for confirming exit
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Exit Game");
    alert.setHeaderText("Are you sure you would like to exit?");
    alert.setResizable(false);
    alert.setContentText("Select OK or Cancel.");
    Optional<ButtonType> result = alert.showAndWait();
    // Check if the person presses OK
    if (result.get() == ButtonType.OK) {
      System.exit(0);
    } else if (result.get() == ButtonType.CANCEL) {
      // Do nothing if cancelled is pressed
      return;
    }
  }

  @FXML
  private void onGraphButton(ActionEvent event) {
    if (Users.getWordHistory().size() == 0) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("No game played yet");
      alert.setHeaderText("No games have been played yet so you cannot view data");
      alert.setResizable(false);
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK) {
        return;
      }
    }
    // Tells the controller to update the graph
    GraphController graphController = App.getGraphController();
    graphController.loadGraphData();
    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GRAPH));
  }
}
