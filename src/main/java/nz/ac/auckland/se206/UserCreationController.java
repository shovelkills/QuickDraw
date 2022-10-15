package nz.ac.auckland.se206;

import ai.djl.ModelException;
import com.opencsv.exceptions.CsvException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import nz.ac.auckland.se206.GameSelectController.GameMode;
import nz.ac.auckland.se206.ProfileBuilder.UserType;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UserCreationController extends SoundsController {

  // Initialise FXML items
  @FXML private Button createButton;
  @FXML private TextField usernameField;
  @FXML private ImageView editImageView;
  @FXML private VBox userImageBox;
  @FXML private ImageView userImage;
  @FXML private ImageView imageOption0;
  @FXML private ImageView imageOption1;
  @FXML private ImageView imageOption2;
  @FXML private ImageView imageOption3;
  @FXML private ImageView imageOption4;
  @FXML private ImageView imageOption5;

  // Maximum length for a new username
  private static final int MAX_USERNAME_LENGTH = 12;

  // Initliase array for image options
  private ArrayList<ImageView> imageOptions = new ArrayList<ImageView>();

  private String editImageViewDefault =
      "-fx-scale-y: 1.1; -fx-scale-x: 1.1; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, #00e8e8, 20, 0.8, 0, 0);";
  private String editImageViewPressed =
      "-fx-scale-y: 1.0; -fx-scale-x: 1.0; -fx-effect: dropshadow(gaussian, #00e8e8, 30, 0.8, 0, 0);";

  /**
   * initialize will be called on start up
   *
   * @throws FileNotFoundException
   */
  public void initialize() throws FileNotFoundException {
    // Set user image edit overlay from file
    String dir = Users.folderDirectory + "/src/main/resources/images/edit.png";
    InputStream stream = new FileInputStream(dir);
    Image editImage = new Image(stream);
    editImageView.setImage(editImage);
    editImageView.setVisible(false);
    // Add all the image options into the array
    Collections.addAll(
        imageOptions,
        imageOption0,
        imageOption1,
        imageOption2,
        imageOption3,
        imageOption4,
        imageOption5);
    // Loop through them and set event handlers onto them
    for (ImageView option : imageOptions) {
      option.addEventHandler(
          MouseEvent.MOUSE_CLICKED,
          e -> {
            onSetImage(e);
          });
    }
    userImageBox.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          try {
            onCreateImage(e);
          } catch (IOException | CsvException | URISyntaxException | ModelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        });
    // Hover shows "edit" overlay
    userImageBox.addEventHandler(
        MouseEvent.MOUSE_ENTERED,
        e -> {
          editImageView.setVisible(true);
        });
    // Exit hides "edit" overlay
    userImageBox.addEventHandler(
        MouseEvent.MOUSE_EXITED,
        e -> {
          editImageView.setVisible(false);
        });
    userImageBox.addEventHandler(
        MouseEvent.MOUSE_PRESSED,
        e -> {
          editImageView.setStyle(editImageViewPressed);
        });
    userImageBox.addEventHandler(
        MouseEvent.MOUSE_RELEASED,
        e -> {
          editImageView.setStyle(editImageViewDefault);
        });
  }

  /**
   * updateImage will update the player's drawn image into their profile picture
   *
   * @throws FileNotFoundException if the file was not found
   */
  public void updateImage() throws FileNotFoundException {
    // creating the image object
    String dir = Users.folderDirectory + "/src/main/resources/images/tempImage.png";
    InputStream stream = new FileInputStream(dir);
    Image image = new Image(stream);
    // Set the new image
    userImage.setImage(image);
  }

  /**
   * onCreateImage will allow the user to create their own image via drawing It will load the
   * profile picture creation scene
   *
   * @param event takes in a mouse event from FXML clicking
   * @throws IOException reading/writing exception
   * @throws CsvException reading spreadsheet exceptions
   * @throws URISyntaxException converting to link exception
   * @throws ModelException doodle prediction exception
   */
  private void onCreateImage(MouseEvent event)
      throws IOException, CsvException, URISyntaxException, ModelException {
    GameSelectController.setCurrentGameMode(GameMode.PROFILE);
    VBox image = (VBox) event.getSource();
    Scene sceneButtonIsIn = image.getScene();
    Task<Void> preDrawTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            // Set up the pre-game UI elements that are in common with restarting the game
            updateProgress(0, 1);
            sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.LOADING));
            System.out.println("Loading");
            App.getCanvasController().setPreGameInterface();
            updateProgress(1, 1);
            Thread.sleep(50);
            return null;
          }
        };
    // Find progress bar on loading screen
    ProgressBar progressBar = App.getLoadingController().getProgressBar();
    progressBar.progressProperty().unbind();
    // Bind progress bar
    progressBar.progressProperty().bind(preDrawTask.progressProperty());
    // On finish loading move from loading screen to game screen
    preDrawTask.setOnSucceeded(
        e -> {
          progressBar.progressProperty().unbind();
          sceneButtonIsIn.setCursor(Cursor.DEFAULT);
          sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.GAME));
        });
    Thread preGameThread = new Thread(preDrawTask);
    // Allow the task to be cancelled on closing of application
    preGameThread.setDaemon(true);
    preGameThread.start();
  }

  /** onSetImage will set the users image to the one clicked */
  @FXML
  private void onSetImage(Event e) {
    // Get the image selected
    ImageView image = (ImageView) e.getSource();

    // Set the user image to the one they picked
    userImage.setImage(image.getImage());
  }

  /**
   * onCreateProfile method will create a new user profile based on the profile position
   *
   * @param event Takes in the FXML action event
   */
  @FXML
  private void onCreateProfile(Event event) {

    // Get the username inputted
    String username = usernameField.getText();
    // Check if it was a valid username
    Users.userImage = userImage.getImage();
    if (username == null || username.equals("")) {
      // Set up null/empty username error alert
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Username cannot be blank");
      alert.setHeaderText("Username cannot be blank!");
      // Show the error
      alert.showAndWait();
      return;
    }
    // Check length of username against maximum length and give alert error if username is too long.
    if (username.length() > MAX_USERNAME_LENGTH) {
      // Set up username too long error alert
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Username too long");
      alert.setHeaderText(
          "Username too long! Maximum length is " + MAX_USERNAME_LENGTH + "characters.");
      // Show the error
      alert.showAndWait();
      return;
    }
    // Check if username is already taken or contains special characters
    if (!Users.createUser(username)) {
      // Set up username taken error alert
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Username not available");
      alert.setHeaderText("That username is not available.");
      // Show the error
      alert.showAndWait();
      return;
    }
    // Get the third to last place in profiles
    int index = UsersController.profiles.size() - 2;
    // Add in the new user
    UsersController.profiles.add(index, new ProfileBuilder(UserType.PLAYER));
    // Store the profiles in an arraylist temporary variable for readability
    ArrayList<ProfileBuilder> profiles = UsersController.profiles;
    // Grab the guest and add profiles
    ProfileBuilder guest = profiles.get(profiles.size() - 2);
    ProfileBuilder add = profiles.get(profiles.size() - 1);
    // Move the guest and add profiles
    guest.vbox.toFront();
    add.vbox.toFront();
    // Update all the IDS after shift
    ProfileBuilder.updateId();
    // Grab the new user
    ProfileBuilder newUser = profiles.get(index);
    // Set up new events to the new user
    UsersController.addEvents(newUser.userImageBox, newUser.deleteProfileButton, index);
    // Select the new user
    UsersController.currentlySelected = newUser;
    onExitSelection(event);
  }

  /**
   * onExitSelection will leave the profile creation scene
   *
   * @param event Takes in Mouse Pressed event
   */
  @FXML
  private void onExitSelection(Event event) {
    // Set the selected label to visible
    UsersController.currentlySelected.userSelectedLabel.setVisible(true);
    // Clear the username field and the user image
    usernameField.clear();
    userImage.setImage(ProfileBuilder.getUserImage());
    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    sceneButtonIsIn.setCursor(Cursor.DEFAULT);
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.USERSELECT));
  }
}
