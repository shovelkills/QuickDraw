package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.Collections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import nz.ac.auckland.se206.ProfileBuilder.UserType;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UserCreationController {

  // Initialise FXML items
  @FXML private Button createButton;
  @FXML private TextField usernameField;
  @FXML private ImageView userImage;
  @FXML private ImageView imageOption0;
  @FXML private ImageView imageOption1;
  @FXML private ImageView imageOption2;
  @FXML private ImageView imageOption3;
  @FXML private ImageView imageOption4;
  @FXML private ImageView imageOption5;
  // Initliase array for image options
  private ArrayList<ImageView> imageOptions = new ArrayList<ImageView>();

  /** initialize will be called on start up */
  public void initialize() {
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
    // Store which index it was created in
    Users.userImage = userImage.getImage();
    if (username == null || username.equals("") || !Users.createUser(username)) {
      // TODO Add an alert that will say invalid username in GUI
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
    UsersController.addEvents(newUser.imageView, newUser.deleteProfileButton, index);
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
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.USERSELECT));
  }
}
