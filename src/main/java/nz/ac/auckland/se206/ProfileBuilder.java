package nz.ac.auckland.se206;

import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ProfileBuilder {
  // Declare all fields that a user profile will have
  protected ImageView imageView;
  protected Label userNameLabel;
  protected TextField userNameInput;
  protected Button createProfileButton;
  protected Label userSelectedLabel;
  protected Button deleteProfileButton;

  // Declare private variables
  private int row;
  private int col;
  // Declare private static objects
  private static HBox hbox;
  // Load in the images
  private static File defaultImageFile = new File("src/main/resources/users/default.png");
  private static Image defaultImage = new Image(defaultImageFile.toURI().toString());
  private static File userImageFile = new File("src/main/resources/users/happy.png");
  private static Image userImage = new Image(userImageFile.toURI().toString());
  protected static int recentCreationIndex;

  /**
   * ProfileBuilder constructor will set up a profile inside the grid
   * 
   * @param row the row number in the grid
   * @param col the column number in the grid
   */
  public ProfileBuilder(int row, int col) {
    // Stores the row and column
    this.row = row;
    this.col = col;
    // Creates all the FXML needed in that grid position
    createImageView();
    // createUserNameLabel();
    // createUserNameInput();
    // createProfileButton();
    // createSelectedLabel();
    // createDeleteProfileButton();
  }

  /**
   * setGrid sets the grid we will be using
   * 
   * @param grid FXML GridPane object
   */
  public static void setHbox(HBox hbox) {
    ProfileBuilder.hbox = hbox;
    hbox.setSpacing(100);
  }

  /**
   * createImageView will set up the image in our profile
   */
  private void createImageView() {
    // Creates new Image View
    imageView = new ImageView();
    hbox.getChildren().add(imageView);
    // Constrains the image view in the grid

    // Binds the height and width of the image in the grid
    imageView.fitHeightProperty().bind(hbox.heightProperty().add(50).divide(7));
    imageView.fitWidthProperty().bind(hbox.widthProperty().subtract(100).divide(7));

    // Loads in the default image
    imageView.setImage(defaultImage);
  }

  /**
   * createUserNameLabel will set up the username label to be displayed
   */
  private void createUserNameLabel() {
    // Create a new label
    userNameLabel = new Label();
    // Add the label into the grid
    hbox.getChildren().add(userNameLabel);
    // Turn on the visibility initially
    userNameLabel.setAlignment(Pos.BOTTOM_CENTER);
    userNameLabel.setVisible(true);
    userNameLabel.setText("Test");
  }

  // /**
  // * createProfile Button creates the creation/selection profile button
  // */
  // private void createProfileButton() {
  // createProfileButton = new Button();
  // grid.add(createProfileButton, col, row + 1, 1, 1);
  // GridPane.setConstraints(createProfileButton, col, row + 1, 1, 1, HPos.CENTER, VPos.TOP,
  // Priority.NEVER, Priority.NEVER, new Insets(50, 0, 0, 0));
  // createProfileButton.setText("Create Profile");
  // // Set the ID
  // createProfileButton.setId(String.format("createProfileButton%d", col));
  // }
  //
  // /**
  // * createSelectedLabel creates the "selected" label to indicate which user is selected
  // */
  // private void createSelectedLabel() {
  // userSelectedLabel = new Label();
  // grid.add(userSelectedLabel, col, row);
  // GridPane.setConstraints(userSelectedLabel, col, row + 1, 1, 1, HPos.CENTER, VPos.BOTTOM,
  // Priority.NEVER, Priority.NEVER, new Insets(0, 0, 60, 0));
  // userSelectedLabel.setVisible(true);
  // // Set the visibility off and the text to selected
  // userSelectedLabel.setText("Selected");
  // userSelectedLabel.setVisible(false);
  //
  // }
  //
  // /**
  // * createDeleteProfileButton will create the delete button for users to delete their profile
  // */
  // private void createDeleteProfileButton() {
  // deleteProfileButton = new Button();
  // grid.add(deleteProfileButton, col, row + 1, 1, 1);
  // GridPane.setConstraints(deleteProfileButton, col, row + 1, 1, 1, HPos.CENTER, VPos.BOTTOM,
  // Priority.NEVER, Priority.NEVER, new Insets(0, 0, 20, 0));
  // deleteProfileButton.setText("Delete Profile");
  // deleteProfileButton.setDisable(true);
  // // Set the ID
  // deleteProfileButton.setId(String.format("deleteProfileButton%d", col));
  // }

  /**
   * Sets the profile image for user
   * 
   * @param userImage an image
   */
  public void setUserImage(Image userImage) {
    this.imageView.setImage(userImage);
  }

  /**
   * sets the profile's user name
   * 
   * @param userName a string text of the username
   */
  public void setUserName(String userName) {
    this.userNameLabel.setText(userName);
  }

  /**
   * switchButtonText will switch the button from createProfile to Select or vice versa
   */
  public void switchButtonText() {
    if (this.createProfileButton.getText().equals("Create Profile")) {
      this.createProfileButton.setText("Select");
    } else if (this.createProfileButton.getText().equals("Select")) {
      this.createProfileButton.setText("Create Profile");
    }
  }

  /**
   * loadProfiles will update the FXML for the user being selected
   * 
   * @param userName
   */
  public void loadProfiles(String userName) {
    // Update the image and name in the main menu
    setUserImage(userImage);
    setUserName(userName);
    switchButtonText();
    // Update the FXML
    userNameInput.setText(null);
    userNameInput.setVisible(false);
    deleteProfileButton.setVisible(true);
    deleteProfileButton.setDisable(false);
  }

  /**
   * deleteProfile will delete the user selected for deletion
   */
  public void deleteProfile() {
    // Set the profile back to default
    setUserImage(defaultImage);
    // Remove the user name
    setUserName(null);
    // Allow for creation again
    userNameInput.setVisible(true);
    deleteProfileButton.setDisable(true);
    switchButtonText();

  }

}
