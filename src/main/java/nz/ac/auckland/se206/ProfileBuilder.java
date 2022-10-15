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
import javafx.scene.text.Font;

public class ProfileBuilder extends SoundsController {

  // Define the types of users a profile can be
  public enum UserType {
    PLAYER,
    GUEST,
    ADD
  }

  // Declare private static objects
  private static HBox hbox;
  // Load in the images for user profiles
  private static File addImageFile = new File("src/main/resources/users/add.png");
  private static Image addImage = new Image(addImageFile.toURI().toString());
  private static File guestImageFile = new File("src/main/resources/users/guest.png");
  private static Image guestImage = new Image(guestImageFile.toURI().toString());
  private static File userImageFile = new File("src/main/resources/users/happy.png");
  private static Image userImage = new Image(userImageFile.toURI().toString());
  private static int counter = 0;

  // Define the scaling in hovering
  private static final String IDLE_STYLE = "-fx-scale-x: 1; -fx-scale-y: 1";
  private static final String HOVERED_STYLE =
      "-fx-scale-x: 1.2; -fx-scale-y: 1.2;"
          + " -fx-effect: dropshadow(gaussian, #fff8f5, 20, 0.8, 0, 0);";
  private static final String MOUSE_DOWN_STYLE =
      "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"
          + " -fx-effect: dropshadow(gaussian, white, 50, 0.8, 0, 0);";

  private static final String HIGHLIGHT_STYLE =
      "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"
          + " -fx-effect: dropshadow(gaussian, #fff8f5, 10, 1, 0, 0);";
  // #bbff78
  // #a6ffcb

  /**
   * Get's the users current image
   *
   * @return return the image of the current user
   */
  public static Image getUserImage() {
    return userImage;
  }

  /** Decrement counter will decrease the counter by 1 */
  public static void decrementCounter() {
    counter--;
  }

  /**
   * setHBox sets the hbox's size and spacing
   *
   * @param hbox FXML HBox object
   */
  public static void setHbox(HBox hbox) {
    // Set the size of the hbox
    hbox.setPrefSize(0, 0);
    hbox.setSpacing(100);
    ProfileBuilder.hbox = hbox;
  }

  /** This method will update the Id of a user */
  public static void updateId() {
    int id = 0;
    // Reset all the ids
    for (ProfileBuilder profile : UsersController.profiles) {
      profile.deleteProfileButton.setId(String.format("deleteProfileButton%d", id));
      profile.userImageBox.setId(String.format("image%d", id));
      id++;
    }
  }

  // Declare all fields that a user profile will have
  protected ImageView imageView;
  protected VBox userImageBox;
  protected Label userNameLabel;
  protected Label userSelectedLabel;
  protected Button deleteProfileButton;
  protected VBox vbox;
  protected UserType type;
  private Font maybeNext =
      Font.loadFont(App.class.getResourceAsStream("/fonts/Maybe-Next.ttf"), 22);
  private boolean isSelected = false;

  /**
   * ProfileBuilder constructor will set up a profile inside the selection optinos
   *
   * @param type takes in the type of user being built
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
      case PLAYER:
        // Set up according to player type
        userNameLabel.setText(Users.getUserList().get(counter));
        // Load in the user's profile picture
        imageView.setImage(Users.loadProfilePicture(userNameLabel.getText()));
        break;
      case GUEST:
        // Set up according to guest type
        imageView.setImage(guestImage);
        userNameLabel.setText("Guest");
        userSelectedLabel.setVisible(true);
        deleteProfileButton.setVisible(false);
        break;
      case ADD:
        // Set up according to add new player type
        imageView.setImage(addImage);
        userNameLabel.setText("New Player");
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
    vbox.setSpacing(10);
  }

  /** createImageView will set up the image in our profile */
  private void createImageView() {
    // Creates new Image View
    imageView = new ImageView();

    // Ensures image is not stretched
    imageView.setPreserveRatio(true);
    // Fit height determines maximum dimensions of image
    imageView.fitHeightProperty().set(150);
    // Fit width determines horizontal scaling with window size
    imageView.fitWidthProperty().bind(hbox.widthProperty().divide(7));

    // Set up interactable box for image view
    userImageBox = new VBox();
    userImageBox.setAlignment(Pos.CENTER);
    userImageBox.getChildren().add(imageView);
    // Set the profile image box id as its position in the user list
    userImageBox.setId(String.format("image%d", counter));

    // Add the image view to the scene
    vbox.getChildren().add(userImageBox);
    VBox.setMargin(vbox.getChildren().get(0), new Insets(0, 0, 20, 0));

    // Event for hovering on
    userImageBox.setOnMouseEntered(
        e -> {
          if (!isSelected) {
            onButtonHover(null);
            imageView.setStyle(HOVERED_STYLE);
            if (imageView.getScene() != null) {
              imageView.getScene().setCursor(Cursor.HAND);
            }
          }
        });
    // Event for mouse down
    userImageBox.setOnMousePressed(
        e -> {
          if (!isSelected) {
            onButtonClick(null);
            imageView.setStyle(MOUSE_DOWN_STYLE);
          }
        });
    // Event for hovering off
    userImageBox.setOnMouseExited(
        e -> {
          if (!isSelected) {
            imageView.setStyle(IDLE_STYLE);
            if (imageView.getScene() != null) {
              imageView.getScene().setCursor(Cursor.DEFAULT);
            }
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
    userNameLabel.setFont(maybeNext);
    userNameLabel.setAlignment(Pos.BOTTOM_CENTER);
    userNameLabel.setPadding(new Insets(10, 0, 0, 0));
    userNameLabel.setVisible(true);
  }

  /** createSelectedLabel creates the "selected" label to indicate which user is selected */
  private void createSelectedLabel() {
    userSelectedLabel = new Label();
    vbox.getChildren().add(userSelectedLabel);
    userSelectedLabel.setFont(maybeNext);
    userSelectedLabel.setAlignment(Pos.BOTTOM_CENTER);
    // Set the visibility off and the text to selected
    userSelectedLabel.setText("(You)");
    userSelectedLabel.setVisible(false);
  }

  /** createDeleteProfileButton will create the delete button for users to delete their profile */
  private void createDeleteProfileButton() {
    // Creates a new vbox
    VBox deleteButtonVBox = new VBox();
    // Sets up the box
    deleteButtonVBox.setPrefSize(149, 47);
    deleteButtonVBox.setMinSize(149, 47);
    deleteButtonVBox.setAlignment(Pos.CENTER);
    deleteProfileButton = new Button();
    deleteProfileButton.setPrefSize(149, 47);
    deleteProfileButton.setAlignment(Pos.BOTTOM_RIGHT);
    deleteProfileButton.setText("");
    deleteProfileButton.getStyleClass().add("deleteProfileButton");
    deleteProfileButton.setOnMouseEntered(
        e -> {
          onButtonHover(e);
        });
    deleteProfileButton.setOnMouseClicked(
        e -> {
          onButtonClick(e);
        });
    deleteButtonVBox.getChildren().add(deleteProfileButton);
    vbox.getChildren().add(deleteButtonVBox);
    // Set the ID for deletion profile button
    deleteProfileButton.setId(String.format("deleteProfileButton%d", counter));
  }

  /** deleteProfile will delete the user selected for deletion */
  public void deleteProfile() {
    // Remove's the vbox from the hbox
    vbox.getChildren().removeAll();
    hbox.getChildren().remove(vbox);
  }

  /**
   * Highlights this profile by updating visible UI elements.
   *
   * @param selected if true this profile will be styled as highlighted
   */
  public void setSelected(boolean selected) {
    // Reset cursor
    if (imageView.getScene() != null) {
      imageView.getScene().setCursor(Cursor.DEFAULT);
    }
    if (selected) {
      // Update selected style
      isSelected = true;
      userSelectedLabel.setVisible(true);
      imageView.setStyle(HIGHLIGHT_STYLE);
      imageView.setEffect(null);
      if (!this.type.equals(UserType.GUEST)) deleteProfileButton.setVisible(true);
    } else {
      // Update non selected style
      isSelected = false;
      deleteProfileButton.setVisible(false);
      userSelectedLabel.setVisible(false);
      imageView.setStyle(IDLE_STYLE);
    }
  }
}
