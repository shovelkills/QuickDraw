package nz.ac.auckland.se206;

import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProfileBuilder {

  // Declare private static objects
  private static HBox hbox;
  // Load in the images
  private static File defaultImageFile = new File("src/main/resources/users/add.png");
  private static Image defaultImage = new Image(defaultImageFile.toURI().toString());
  private static File userImageFile = new File("src/main/resources/users/happy.png");
  private static Image userImage = new Image(userImageFile.toURI().toString());
  private static File guestImageFile = new File("src/main/resources/users/guest.png");
  private static Image guestImage = new Image(guestImageFile.toURI().toString());
  protected static int recentCreationIndex;
  private static int counter = 0;

  /**
   * setGrid sets the grid we will be using
   * 
   * @param grid FXML GridPane object
   */
  public static void setHbox(HBox hbox) {
    hbox.setPrefSize(2000, 800);
    hbox.setSpacing(100);
    ProfileBuilder.hbox = hbox;
  }

  public enum UserType {
    PLAYER, GUEST, ADD
  }

  // Declare all fields that a user profile will have
  protected ImageView imageView;
  protected Label userNameLabel;
  protected TextField userNameInput;
  protected Button selectProfileButton;
  protected Label userSelectedLabel;
  protected Button deleteProfileButton;
  protected VBox vbox;
  protected UserType type;


  /**
   * ProfileBuilder constructor will set up a profile inside the grid
   * 
   * @param row the row number in the grid
   * @param col the column number in the grid
   */
  public ProfileBuilder(UserType type) {
    this.type = type;
    createVertBox();
    createImageView();
    createUserNameLabel();
    createSelectProfileButton();
    createSelectedLabel();
    createDeleteProfileButton();


    // Set up profiles according to their type
    switch (this.type) {
      // Set up according to player type
      case PLAYER:
        imageView.setImage(userImage);
        userNameLabel.setText(Users.getUserList().get(counter));
        break;
      // Set up according to guest type
      case GUEST:
        imageView.setImage(guestImage);
        userNameLabel.setText("Guest");
        userSelectedLabel.setVisible(true);
        deleteProfileButton.setVisible(false);
        selectProfileButton.setDisable(true);
        break;
      // Set up according to add new player type
      case ADD:
        imageView.setImage(defaultImage);
        userNameLabel.setText("Add New Player");
        deleteProfileButton.setVisible(false);
        selectProfileButton.setVisible(false);
        break;
    }
    counter++;
  }

  private void createVertBox() {
    vbox = new VBox();
    hbox.getChildren().add(vbox);
    vbox.setAlignment(Pos.CENTER);
  }



  /**
   * createImageView will set up the image in our profile
   */
  private void createImageView() {
    // Creates new Image View
    imageView = new ImageView();
    vbox.getChildren().add(imageView);
    // Constrains the image view in the grid

    // Binds the height and width of the image in the grid
    imageView.setPreserveRatio(true);
    imageView.fitHeightProperty().bind(hbox.heightProperty().divide(7));
    imageView.fitWidthProperty().bind(hbox.widthProperty().divide(7));

  }

  /**
   * createUserNameLabel will set up the username label to be displayed
   */
  private void createUserNameLabel() {
    // Create a new label
    userNameLabel = new Label();
    // Add the label into the grid
    vbox.getChildren().add(userNameLabel);
    // Turn on the visibility initially
    userNameLabel.setAlignment(Pos.BOTTOM_CENTER);
    userNameLabel.setVisible(true);
  }

  /**
   * createSelectProfileButton creates the creation/selection profile button
   */
  private void createSelectProfileButton() {
    selectProfileButton = new Button();
    vbox.getChildren().add(selectProfileButton);
    selectProfileButton.setText("Select Profile");
    selectProfileButton.setAlignment(Pos.BOTTOM_CENTER);
    // Set the ID
    selectProfileButton.setId(String.format("selectProfileButton%d", counter));
  }

  /**
   * createSelectedLabel creates the "selected" label to indicate which user is selected
   */
  private void createSelectedLabel() {
    userSelectedLabel = new Label();
    vbox.getChildren().add(userSelectedLabel);
    userSelectedLabel.setAlignment(Pos.BOTTOM_CENTER);
    // Set the visibility off and the text to selected
    userSelectedLabel.setText("Selected");
    userSelectedLabel.setVisible(false);

  }

  /**
   * createDeleteProfileButton will create the delete button for users to delete their profile
   */
  private void createDeleteProfileButton() {
    deleteProfileButton = new Button();
    vbox.getChildren().add(deleteProfileButton);
    deleteProfileButton.setText("Delete Profile");
    deleteProfileButton.setAlignment(Pos.BOTTOM_CENTER);
    // Set the ID
    deleteProfileButton.setId(String.format("deleteProfileButton%d", counter));
  }

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
   * loadProfiles will update the FXML for the user being selected
   * 
   * @param userName
   */
  public void loadProfiles(String userName) {
    // Update the image and name in the main menu
    setUserImage(userImage);
    setUserName(userName);

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


  }

}
