package nz.ac.auckland.se206;

import java.io.File;
import java.util.ArrayList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.ProfileBuilder.UserType;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UserCreationController {

  @FXML
  private Button createButton;
  @FXML
  private TextField usernameField;
  @FXML
  private ImageView createImageView;



  public void initialize() {
    File createImageFile = new File("src/main/resources/users/create.png");
    Image createImage = new Image(createImageFile.toURI().toString());
    createImageView.setImage(createImage);
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
    // Grab the guest, new user, and add profiles
    ProfileBuilder guest = profiles.get(profiles.size() - 2);
    ProfileBuilder add = profiles.get(profiles.size() - 1);
    ProfileBuilder newUser = profiles.get(index);
    // Move the guest and add profiles
    guest.vbox.toFront();
    add.vbox.toFront();
    // Update all the IDS after shift
    ProfileBuilder.updateID();
    // Set up new events to the new user
    UsersController.addEvents(newUser.imageView, newUser.deleteProfileButton, index);
    // Select the new user
    UsersController.currentlySelected = newUser;

    // Select the profile
    // onSelectProfile(event);

    onExitSelection(event);
  }

  /**
   * onExitSelection will leave the profile creation scene
   * 
   * @param event Takes in Mouse Pressed event
   */
  @FXML
  private void onExitSelection(Event event) {
    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    // Set the selected label to visible
    UsersController.currentlySelected.userSelectedLabel.setVisible(true);
    // Clear the username field
    usernameField.clear();
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.USERSELECT));
  }

}
