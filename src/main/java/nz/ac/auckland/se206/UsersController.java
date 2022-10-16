package nz.ac.auckland.se206;

import ai.djl.ModelException;
import com.opencsv.exceptions.CsvException;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nz.ac.auckland.se206.ProfileBuilder.UserType;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class UsersController extends SoundsController {
  // Create an array to store the newly built profiles
  protected static ArrayList<ProfileBuilder> profiles = new ArrayList<ProfileBuilder>();
  // Get all the users
  protected static List<String> usersList = Users.getUserList();
  protected static ProfileBuilder currentlySelected = null;
  // Max profiles we are having
  private static int profileCap = 7;
  private static int editUserId = 0;

  /**
   * onSelectProfile will select a user profile and make that user the current selected user
   *
   * @param event Takes in the FXML action event
   * @param type Either a user profile or the guest profile
   */
  private static void onSelectProfile(Event event) {
    VBox image = null;
    int profileSelectedId;
    // Get the button pressed
    if (event.getSource().getClass().equals(profiles.get(0).deleteProfileButton.getClass())) {
      // guest was selected via creation or deletion
      profileSelectedId = profiles.size() - 2;
    } else {
      image = (VBox) event.getSource();
      String imageId = image.getId().toString();
      // Find out which profile was clicked
      profileSelectedId = (Integer.parseInt(String.valueOf(imageId.charAt(imageId.length() - 1))));
    }

    // Check if the image pressed was the add new player
    if (profileSelectedId == profiles.size() - 1) {
      // Check if the profile cap was reached
      if (profiles.size() == profileCap) {
        System.out.println("You cannot add anymore users!");
        return;
      }
      // Go to user Creation scene

      image.getScene().setCursor(Cursor.DEFAULT);
      image.getScene().setRoot(SceneManager.getUiRoot(AppUi.USERCREATE));
      return;
    }
    // Update the current user in the usersList and main menu
    selectUser(profileSelectedId);
  }

  /**
   * selectUser will select a user to play as
   *
   * @param number the index number for the profile
   */
  private static void selectUser(int userId) {
    // Once valid profile ID is obtained, set it to the currently selected profile
    currentlySelected = profiles.get(userId);

    // Reset all profile visual styles
    for (ProfileBuilder profile : profiles) {
      // Reset all profile views to deselected
      profile.setSelected(false);
    }
    // Update selected user's visual style
    profiles.get(userId).setSelected(true);

    String username;
    // Get the user's name based on the number position of the profile
    if (userId == profiles.size() - 2) {
      username = "Guest";
    } else {
      username = usersList.get(userId);
    }

    // Load in the user
    Users.loadUser(username);
    // Update the menu page
    App.getMenuController().updateUserImage(profiles.get(userId).imageView.getImage());
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
      int userId = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
      // Delete that profile
      profiles.get(userId).deleteProfile();
      profiles.remove(userId);
      ProfileBuilder.updateId();
      // Update counter position
      ProfileBuilder.decrementCounter();
      // Select the Guest Profile
      onSelectProfile(event);
      // Find that user in the list
      String username = usersList.get(userId);
      // Delete the user in the JSON file
      Users.deleteUser(username);
      Users.deleteProfilePicture(username);

    } else if (result.get() == ButtonType.CANCEL) {
      // Do nothing if no is pressed
      return;
    }
  }

  /**
   * onEditSticker will edit the user's sticker
   *
   * @param e takes in a JavaFX event
   * @throws IOException throws reader writer exception
   * @throws CsvException throws cant read spreadsheet exception
   * @throws URISyntaxException throws URI exception
   * @throws ModelException throws model exception
   */
  private static void onEditSticker(Event event)
      throws IOException, CsvException, URISyntaxException, ModelException {
    Button button = (Button) event.getSource();
    String string = button.getId().toString();
    UserCreationController.onCreateImage(event);
    // Get the index number from the button
    editUserId = (Integer.parseInt(String.valueOf(string.charAt(string.length() - 1))));
  }

  /**
   * updateImage will update the player's drawn image into their profile picture
   *
   * @throws FileNotFoundException if the file was not found
   */
  public static void updateImage() throws FileNotFoundException {
    // creating the image object
    String dir = Users.folderDirectory + "/src/main/resources/images/tempImage.png";
    InputStream stream = new FileInputStream(dir);
    Image image = new Image(stream);
    // Set the new image
    ProfileBuilder currentProfile = profiles.get(editUserId);
    currentProfile.imageView.setImage(image);
    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
    // Save the user's image
    Users.saveProfilePicture(bufferedImage);
    App.getMenuController().updateUserImage(image);
  }

  /**
   * addEvents will add the events to the image and buttons
   *
   * @param image new profile's image
   * @param deleteButton new profile's delete button
   * @param number new profile's slot
   */
  protected static void addEvents(
      VBox imageBox, Button deleteButton, Button stickerButton, int profileId) {
    // Add the event handler to the image
    imageBox.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          // Add select profile event
          onSelectProfile(e);
        });
    // Add the event handler to the delete profile button
    deleteButton.addEventHandler(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          // Add delete event
          onDeleteProfile(e);
        });

    stickerButton.addEventFilter(
        MouseEvent.MOUSE_CLICKED,
        e -> {
          // Add edit sticker event
          try {
            onEditSticker(e);
          } catch (IOException | CsvException | URISyntaxException | ModelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
          }
        });
    selectUser(profileId);
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
      profile.userImageBox.addEventHandler(
          MouseEvent.MOUSE_CLICKED,
          e -> {
            onSelectProfile(e);
            VBox source = (VBox) e.getSource();
            if (source.getScene() != null) {
              source.getScene().setCursor(Cursor.DEFAULT);
            }
          });
      // Add delete profile event
      profile.deleteProfileButton.addEventHandler(
          ActionEvent.ACTION,
          e -> {
            onDeleteProfile(e);
          });
      // Add edit image sticker event
      profile.stickerButton.addEventHandler(
          ActionEvent.ACTION,
          e -> {
            try {
              onEditSticker(e);
            } catch (IOException | CsvException | URISyntaxException | ModelException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
          });
    }
    // Default select guest profile on boot
    selectUser(profiles.size() - 2);
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
