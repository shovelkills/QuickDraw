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

    for (int i = 0; i < profileAmount - 1; i++) {
      profiles.add(new ProfileBuilder(0, i));
    }
    // Add in guest
    profiles.add(new ProfileBuilder(0, profileAmount - 1));
    guest = profiles.get(profileAmount - 1);
    // Load in previously made users
    for (int i = 0; i < usersList.size(); i++) {
      // ~ means that there was a player that was recently deleted in that slot
      if (usersList.get(i).contains("~")) {
        continue;
      }
    }
  }

  /**
   * onUserButtonPress handles the action event when pressing the create profile / select profile
   * button It will either create a new user profile or select it as the current user
   * 
   * @param event Takes in the FXML action event
   */
  @FXML
  private void onUserButtonPress(ActionEvent event) {
    // Get the button that was pressed and what it said
    Button button = (Button) event.getSource();
    String buttonString = button.getText();
    // If the button was select character
    if (buttonString.equals("Select")) {
      // Select that profile as current user
      onSelectProfile(event, null);
    }
    // If the button was create profile
    else if (buttonString.equals("Create Profile")) {
      // Create a new user profile
      onCreateProfile(event);
    }
  }

  /**
   * onSelectProfile will select a user profile and make that user the current selected user
   * 
   * @param event Takes in the FXML action event
   * @param type Either a user profile or the guest profile
   */
  private void onSelectProfile(Event event, String type) {
    // Initialise a variable
    String username;
    String string = null;

    // Get the button pressed / text field entered
    if (event.getEventType().equals(ActionEvent.ACTION)) {
      Button button = (Button) event.getSource();
      string = button.getId().toString();
    } else {
      TextField textfield = (TextField) event.getSource();
      string = textfield.getId().toString();
    }
    // Find out which profile was clicked
    int number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
    // Check if the guest profile was selected
    if (type != null) {
      number = profileAmount - 1;
    }
    // Loop through the profiles to say they weren't selected
    for (ProfileBuilder profile : profiles) {
      profile.userSelectedLabel.setVisible(false);
      profile.createProfileButton.setVisible(true);

    }
    // Select the profile that was chosen
    profiles.get(number).userSelectedLabel.setVisible(true);
    profiles.get(number).createProfileButton.setVisible(false);

    // Get the user's name based on the number position of the profile
    if (number == profileAmount - 1) {
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
    onSelectProfile(event, null);
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
    onSelectProfile(event, "Deletion");
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
    for (int i = 0; i < usersList.size(); i++) {
      profiles.get(i).userNameInput.clear();
    }
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
    guest.createProfileButton.setText("Select");
    guest.createProfileButton.setVisible(false);
    guest.userNameInput.setVisible(false);
    guest.userSelectedLabel.setVisible(true);
    guest.imageView.setImage(guestImage);

  }
}
