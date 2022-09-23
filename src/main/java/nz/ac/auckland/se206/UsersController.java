package nz.ac.auckland.se206;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import nz.ac.auckland.se206.ProfileBuilder.UserType;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UsersController {
  // Declare the user grid from the FXML
  @FXML
  private HBox userHbox;

  // Get all the users
  private List<String> usersList = Users.getUserList();
  // Create an array to store the newly built profiles
  private ArrayList<ProfileBuilder> profiles = new ArrayList<ProfileBuilder>();
  // Max profiles we are having
  private int profileAmount = usersList.size();
  private ProfileBuilder guest;
  private File guestImageFile = new File("src/main/resources/users/guest.png");
  private Image guestImage = new Image(guestImageFile.toURI().toString());

  /**
   * The initialize method will initialise all the FXML needed for the Users.fxml scene Here it
   * loads in the profile selection GUI
   */
  public void initialize() {
    // Load default image and user profile image
    ProfileBuilder.setHbox(userHbox);
    // Add all the declared fields into the array lists

    for (int i = 0; i < profileAmount; i++) {
      profiles.add(new ProfileBuilder(UserType.PLAYER));
    }
    // Add in guest
    profiles.add(new ProfileBuilder(UserType.GUEST));
    profiles.add(new ProfileBuilder(UserType.ADD));
    guest = profiles.get(profiles.size() - 2);
    // Load in previously made users
    for (int i = 0; i < usersList.size(); i++) {
      // ~ means that there was a player that was recently deleted in that slot
      if (usersList.get(i).contains("~")) {
        continue;
      }
    }

    for (ProfileBuilder profile : profiles) {
      profile.selectProfileButton.setOnAction(e -> {
        onSelectProfile(e);
      });
    }

  }



  /**
   * onSelectProfile will select a user profile and make that user the current selected user
   * 
   * @param event Takes in the FXML action event
   * @param type Either a user profile or the guest profile
   */
  @FXML
  private void onSelectProfile(ActionEvent event) {
    // Initialise a variable
    String username;
    // Get the button pressed

    Button button = (Button) event.getSource();
    String string = button.getId().toString();

    // Find out which profile was clicked
    int number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
    // Check if the guest profile was selected

    // Loop through the profiles to say they weren't selected
    for (ProfileBuilder profile : profiles) {
      if (profile.type == UserType.ADD) {
        break;
      }
      profile.userSelectedLabel.setVisible(false);
      profile.selectProfileButton.setDisable(false);

    }
    // Select the profile that was chosen
    profiles.get(number).userSelectedLabel.setVisible(true);
    profiles.get(number).selectProfileButton.setDisable(true);

    // Get the user's name based on the number position of the profile
    if (number == profiles.size() - 2) {
      username = "Guest";
    } else {
      username = usersList.get(number);
    }

    // Load in the user
    Users.loadUser(username);
    // Update the menu page
    MenuController.updateUser(profiles.get(number).imageView.getImage());
    // TODO Update the Game page

  }

  /**
   * onCreateProfile method will create a new user profile based on the profile position
   *
   * @param event Takes in the FXML action event
   */
  @FXML
  private void onCreateProfile(Event event) {
    String string = null;

    // Get the button pressed / text field entered
    if (event.getEventType().equals(ActionEvent.ACTION)) {
      Button button = (Button) event.getSource();
      string = button.getId().toString();
    } else {
      TextField textfield = (TextField) event.getSource();
      string = textfield.getId().toString();
    }
    // Get the profile number
    int number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
    // Get the username inputted
    String username = profiles.get(number).userNameInput.getText();
    // Check if it was a valid username
    // Store which index it was created in
    ProfileBuilder.recentCreationIndex = number;
    if (username == null || username.equals("") || !Users.createUser(username)) {
      // TODO Add an alert that will say invalid username in GUI
      return;
    }

    // Load the profile FXML
    profiles.get(number).loadProfiles(username);
    usersList = Users.getUserList();
    // Select the profile
    // onSelectProfile(event);
  }

  /**
   * onDeleteProfile will delete a profile in a given index
   * 
   * @param event Takes in the FXML action event
   */
  @FXML
  private void onDeleteProfile(ActionEvent event) {
    // TODO ASK FOR CONFIRMATION ALERT
    // Find out which button was pressed
    Button button = (Button) event.getSource();
    String string = button.getId().toString();
    // Get the index number from the button
    int number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
    // Find that user in the list
    String username = usersList.get(number);
    // Delete that profile
    profiles.get(number).deleteProfile();
    // Select the Guest Profile
    onSelectProfile(event);
    // Delete the user in the JSON file
    Users.deleteUser(username);

  }

  /**
   * onExitSelection will leave the selection page back to the main menu
   * 
   * @param event Takes in the FXML action event
   */
  @FXML
  private void onExitSelection(ActionEvent event) {
    // Get the scene currently in
    Button button = (Button) event.getSource();
    Scene sceneButtonIsIn = button.getScene();
    // for (int i = 0; i < usersList.size(); i++) {
    // profiles.get(i).userNameInput.clear();
    // }
    // Move to the next scene
    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }

  /**
   * This will update all the guest information for the Guest Profile
   */
  private void updateGuestProfile() {
    // TODO make cleaner (possibly make static in ProfileBuilder.java)
    // Set up all the guest profile FXML accordingly
    guest.setUserName("Guest");
    guest.deleteProfileButton.setVisible(true);
    guest.deleteProfileButton.setDisable(true);
    guest.selectProfileButton.setText("Select");
    guest.selectProfileButton.setVisible(false);
    guest.userNameInput.setVisible(false);
    guest.userSelectedLabel.setVisible(true);
    guest.imageView.setImage(guestImage);

  }
}
