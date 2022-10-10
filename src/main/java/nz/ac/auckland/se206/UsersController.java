package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nz.ac.auckland.se206.ProfileBuilder.UserType;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UsersController {
  // Create an array to store the newly built profiles
  protected static ArrayList<ProfileBuilder> profiles = new ArrayList<ProfileBuilder>();
  // Get all the users
  protected static List<String> usersList = Users.getUserList();
  protected static ProfileBuilder currentlySelected = null;
  // Max profiles we are having
  private static int profileCap = 7;

  /**
   * onSelectProfile will select a user profile and make that user the current selected user
   *
   * @param event Takes in the FXML action event
   * @param type Either a user profile or the guest profile
   */
  private static void onSelectProfile(Event event) {
    VBox image = null;
    int number;
    // Get the button pressed
    if (event.getSource().getClass().equals(profiles.get(0).deleteProfileButton.getClass())) {
      // guest was selected via creation or deletion
      number = profiles.size() - 2;
    } else {
      image = (VBox) event.getSource();
      String string = image.getId().toString();
      // Find out which profile was clicked
      number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
    }

    // Loop through the profiles to say they weren't selected
    for (ProfileBuilder profile : profiles) {
      // Check
      if (profile.type == UserType.ADD) {
        // Reset the cursor
        profile.userImageVBox.getScene().setCursor(Cursor.DEFAULT);
        break;
      }
      if (profile.userSelectedLabel.isVisible()) {
        // get the currently selected profile
        currentlySelected = profile;
      }
      // Turn off selection for all
      profile.userSelectedLabel.setVisible(false);
    }
    // Check if the image pressed was the add new player
    if (number == profiles.size() - 1) {
      // Check if the profile cap was reached
      if (profiles.size() == profileCap) {
        System.out.println("You cannot add anymore users!");
        currentlySelected.userSelectedLabel.setVisible(true);
        return;
      }
      // Go to user Creation scene
      image.getScene().setRoot(SceneManager.getUiRoot(AppUi.USERCREATE));
      return;
    }
    selectUser(number);
  }

  /**
   * selectUser will select a user to play as
   *
   * @param number the index number for the profile
   */
  private static void selectUser(int number) {
    // Initialise a variable
    String username;
    // Select the profile that was chosen
    profiles.get(number).userSelectedLabel.setVisible(true);

    // Get the user's name based on the number position of the profile
    if (number == profiles.size() - 2) {
      username = "Guest";
    } else {
      username = usersList.get(number);
    }

    // Load in the user
    Users.loadUser(username);
    // Update the menu page
    App.getMenuController().updateUserImage(profiles.get(number).imageView.getImage());
    App.getMenuController().updateUsernameLabel(username);
  }

  /**
   * onDeleteProfile will delete a profile in a given index
   *
   * @param event Takes in the FXML action event
   */
  private static void onDeleteProfile(Event event) {
    // Create a new alert
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    // Set up the alert accordingly
    alert.setTitle("Delete Profile");
    alert.setHeaderText("Would you like to delete this profile?");
    alert.setResizable(false);
    alert.setContentText("Select OK or Cancel.");

    // Show the alert
    Optional<ButtonType> result = alert.showAndWait();
    // Check if the person presses yes
    if (result.get() == ButtonType.OK) {
      // Find out which button was pressed
      Button button = (Button) event.getSource();
      String string = button.getId().toString();
      // Get the index number from the button
      int number = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
      // Delete that profile
      profiles.get(number).deleteProfile();
      profiles.remove(number);
      ProfileBuilder.updateId();
      // Update counter position
      ProfileBuilder.decrementCounter();
      // Select the Guest Profile
      onSelectProfile(event);
      // Find that user in the list
      String username = usersList.get(number);
      // Delete the user in the JSON file
      Users.deleteUser(username);
      Users.deleteProfilePicture(username);

    } else if (result.get() == ButtonType.CANCEL) {
      // Do nothing if no is pressed
      return;
    }
  }

  /**
   * addEvents will add the events to the image and buttons
   *
   * @param image new profile's image
   * @param deleteButton new profile's delete button
   * @param number new profile's slot
   */
  protected static void addEvents(VBox imageBox, Button deleteButton, int number) {
    // Add the event handler to the image
    imageBox.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          // Add select profile event
          onSelectProfile(e);
        });
    // ADd the event handler to the delete profile button
    deleteButton.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          // Add delete event
          onDeleteProfile(e);
        });
    selectUser(number);
  }

  // Declare the user grid from the FXML
  @FXML private HBox userHbox;
  @FXML private Button returnButton;
  // profile amount to load in
  private int profileAmount = usersList.size();

  /**
   * The initialize method will initialise all the FXML needed for the Users.fxml scene Here it
   * loads in the profile selection GUI
   */
  public void initialize() {
    // Load default image and user profile image
    ProfileBuilder.setHbox(userHbox);
    // Add all the declared fields into the array lists

    // Load in players
    for (int i = 0; i < profileAmount; i++) {
      profiles.add(new ProfileBuilder(UserType.PLAYER));
    }
    // Add in guest
    profiles.add(new ProfileBuilder(UserType.GUEST));
    profiles.add(new ProfileBuilder(UserType.ADD));

    // Add event handlers to the profiles
    for (ProfileBuilder profile : profiles) {
      // Add select profile event
      profile.userImageVBox.addEventHandler(
          MouseEvent.MOUSE_CLICKED,
          e -> {
            onSelectProfile(e);
          });
      // Add delete profile event
      profile.deleteProfileButton.addEventHandler(
          ActionEvent.ACTION,
          e -> {
            onDeleteProfile(e);
          });
    }
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

    sceneButtonIsIn.setRoot(SceneManager.getUiRoot(AppUi.MAIN_MENU));
  }
}
