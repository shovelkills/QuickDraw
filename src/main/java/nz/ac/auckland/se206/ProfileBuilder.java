package nz.ac.auckland.se206;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProfileBuilder {

  // Declare private static objects
  private static HBox hbox;
  // Load in the images for user profiles
  private static File addImageFile = new File("src/main/resources/users/add.png");
  protected static Image addImage = new Image(addImageFile.toURI().toString());
  private static File guestImageFile = new File("src/main/resources/users/guest.png");
  protected static Image guestImage = new Image(guestImageFile.toURI().toString());
  private static File userImageFile = new File("src/main/resources/users/happy.png");
  protected static Image userImage = new Image(userImageFile.toURI().toString());
  protected static int counter = 0;

  // Define the scaling in hovering
  private static final String IDLE_STYLE = "-fx-scale-x: 1; -fx-scale-y: 1";
  private static final String HOVERED_STYLE = "-fx-scale-x: 1.2; -fx-scale-y: 1.2";

  /**
   * setHBox sets the hbox's size and spacing
   *
   * @param hbox FXML HBox object
   */
  public static void setHbox(HBox hbox) {
    // Set the size of the hbox
    hbox.setPrefSize(2000, 800);
    hbox.setSpacing(100);
    ProfileBuilder.hbox = hbox;
  }

  // Update all the IDs
  public static void updateID() {
    int id = 0;
    // Reset all the ids
    for (ProfileBuilder profile : UsersController.profiles) {
      profile.deleteProfileButton.setId(String.format("deleteProfileButton%d", id));
      profile.imageView.setId(String.format("image%d", id));
      id++;
    }
  }

  // Define the types of users a profile can be
  public enum UserType {
    PLAYER, GUEST, ADD
  }

  // Declare all fields that a user profile will have
  protected ImageView imageView;
  protected Label userNameLabel;
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
    // Get the type of profile
    this.type = type;
    // Create all the UI
    createVertBox();
    createImageView();
    createUserNameLabel();
    createSelectedLabel();
    createDeleteProfileButton();

    // Set up profiles according to their type
    switch (this.type) {
      // Set up according to player type
      case PLAYER:
        userNameLabel.setText(Users.getUserList().get(counter));
        // Load in the user's profile picture
        imageView.setImage(Users.loadProfilePicture(userNameLabel.getText()));
        break;
      // Set up according to guest type
      case GUEST:
        imageView.setImage(guestImage);
        userNameLabel.setText("Guest");
        userSelectedLabel.setVisible(true);
        deleteProfileButton.setVisible(false);
        break;
      // Set up according to add new player type
      case ADD:
        imageView.setImage(addImage);
        userNameLabel.setText("Add New Player");
        deleteProfileButton.setVisible(false);
        counter = counter - 2;
        break;
    }
    // Increment the counter
    counter++;
  }

  /** createVertBox will create a vertical box for the UI to be placed in */
  private void createVertBox() {
    // Initialise a vbox
    vbox = new VBox();
    // Add the vbox to the hbox and centre it
    hbox.getChildren().add(vbox);
    vbox.setAlignment(Pos.CENTER);
  }

  /** createImageView will set up the image in our profile */
  private void createImageView() {
    // Creates new Image View
    imageView = new ImageView();
    vbox.getChildren().add(imageView);
    // Constrains the image view in the grid

    // Binds the height and width of the image in the grid
    imageView.setPreserveRatio(true);
    // Set the image size
    imageView.fitHeightProperty().bind(hbox.heightProperty().divide(7));
    imageView.fitWidthProperty().bind(hbox.widthProperty().divide(7));
    // Set the image id
    imageView.setId(String.format("image%d", counter));

    // Event for hovering on
    imageView.setOnMouseEntered(e -> {
      imageView.setStyle(HOVERED_STYLE);
      imageView.getScene().setCursor(Cursor.HAND);
    });

    // Event for hovering off
    imageView.setOnMouseExited(e -> {
      imageView.setStyle(IDLE_STYLE);
      if (imageView.getScene() != null) {
        imageView.getScene().setCursor(Cursor.DEFAULT);
      }
    });
  }

  /** createUserNameLabel will set up the username label to be displayed */
  private void createUserNameLabel() {
    // Create a new label
    userNameLabel = new Label();
    // Add the label into the grid
    vbox.getChildren().add(userNameLabel);
    // Set the alignment for the username label
    userNameLabel.setAlignment(Pos.BOTTOM_CENTER);
    userNameLabel.setPadding(new Insets(20, 0, 0, 0));
    userNameLabel.setVisible(true);
  }

  /** createSelectedLabel creates the "selected" label to indicate which user is selected */
  private void createSelectedLabel() {
    userSelectedLabel = new Label();
    vbox.getChildren().add(userSelectedLabel);
    userSelectedLabel.setAlignment(Pos.BOTTOM_CENTER);
    // Set the visibility off and the text to selected
    userSelectedLabel.setText("Selected");
    userSelectedLabel.setVisible(false);
  }

  /** createDeleteProfileButton will create the delete button for users to delete their profile */
  private void createDeleteProfileButton() {
    deleteProfileButton = new Button();
    vbox.getChildren().add(deleteProfileButton);
    deleteProfileButton.setText("Delete Profile");
    deleteProfileButton.setAlignment(Pos.BOTTOM_CENTER);
    // Set the ID for deletion profile button
    deleteProfileButton.setId(String.format("deleteProfileButton%d", counter));
  }

  /** deleteProfile will delete the user selected for deletion */
  public void deleteProfile() {
    // Remove's the vbox from the hbox
    vbox.getChildren().removeAll();
    hbox.getChildren().remove(vbox);
  }
}
